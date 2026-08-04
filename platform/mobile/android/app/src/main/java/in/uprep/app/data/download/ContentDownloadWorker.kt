package `in`.uprep.app.data.download

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import `in`.uprep.app.data.db.DownloadDatabase
import `in`.uprep.app.data.db.DownloadEntity
import `in`.uprep.app.data.db.DownloadStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

// Downloads a content item to app-private storage for offline use, resumable
// across retries via HTTP Range, then re-encrypts the completed file at rest
// (EncryptedFile/Jetpack Security) so it isn't directly readable outside the
// app — the modern equivalent of legacy's custom passphrase encryption.
class ContentDownloadWorker(appContext: Context, params: WorkerParameters) :
    CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val contentId = inputData.getString(KEY_CONTENT_ID) ?: return@withContext Result.failure()
        val type = inputData.getString(KEY_TYPE) ?: return@withContext Result.failure()
        val name = inputData.getString(KEY_NAME) ?: return@withContext Result.failure()
        val url = inputData.getString(KEY_URL) ?: return@withContext Result.failure()

        val dao = DownloadDatabase.get(applicationContext).downloadDao()
        val downloadsDir = File(applicationContext.filesDir, "downloads").apply { mkdirs() }
        val tempFile = File(downloadsDir, "$contentId.part")
        val finalFile = File(downloadsDir, "$contentId.enc")

        dao.upsert(
            DownloadEntity(contentId, type, name, url, null, DownloadStatus.DOWNLOADING, tempFile.length(), 0)
        )

        try {
            val client = OkHttpClient()
            val resumeFrom = if (tempFile.exists()) tempFile.length() else 0L
            val request = Request.Builder().url(url).apply {
                if (resumeFrom > 0) addHeader("Range", "bytes=$resumeFrom-")
            }.build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("HTTP ${response.code}")
                val body = response.body ?: throw IOException("Empty response body")

                // Only trust the resume offset if the server actually honored the
                // Range request (206); otherwise it sent the full file from byte 0.
                val startByte = if (resumeFrom > 0 && response.code == 206) resumeFrom else 0L
                val totalBytes = startByte + body.contentLength().coerceAtLeast(0)

                FileOutputStream(tempFile, startByte > 0).use { out ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(8192)
                        var downloaded = startByte
                        var lastUpdate = 0L
                        while (true) {
                            val read = input.read(buffer)
                            if (read == -1) break
                            out.write(buffer, 0, read)
                            downloaded += read
                            val now = System.currentTimeMillis()
                            if (now - lastUpdate > 500) {
                                dao.upsert(
                                    DownloadEntity(
                                        contentId, type, name, url, null,
                                        DownloadStatus.DOWNLOADING, downloaded, totalBytes
                                    )
                                )
                                lastUpdate = now
                            }
                        }
                    }
                }
            }

            if (finalFile.exists()) finalFile.delete()
            val masterKey = MasterKey.Builder(applicationContext)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()
            val encryptedFile = EncryptedFile.Builder(
                applicationContext, finalFile, masterKey,
                EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
            encryptedFile.openFileOutput().use { encOut ->
                FileInputStream(tempFile).use { plainIn -> plainIn.copyTo(encOut) }
            }
            val finishedBytes = tempFile.length()
            tempFile.delete()

            dao.upsert(
                DownloadEntity(
                    contentId, type, name, url, finalFile.absolutePath,
                    DownloadStatus.COMPLETE, finishedBytes, finishedBytes
                )
            )
            Result.success()
        } catch (e: Exception) {
            dao.upsert(
                DownloadEntity(contentId, type, name, url, null, DownloadStatus.FAILED, tempFile.length(), 0)
            )
            Result.retry()
        }
    }

    companion object {
        const val KEY_CONTENT_ID = "contentId"
        const val KEY_TYPE = "type"
        const val KEY_NAME = "name"
        const val KEY_URL = "url"
    }
}
