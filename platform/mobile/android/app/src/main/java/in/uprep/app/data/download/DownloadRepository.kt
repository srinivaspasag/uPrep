package `in`.uprep.app.data.download

import android.content.Context
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import `in`.uprep.app.data.db.DownloadDao
import `in`.uprep.app.data.db.DownloadDatabase
import `in`.uprep.app.data.db.DownloadEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class DownloadRepository(private val context: Context) {
    private val dao: DownloadDao = DownloadDatabase.get(context).downloadDao()
    private val workManager = WorkManager.getInstance(context)

    fun observe(contentId: String): Flow<DownloadEntity?> = dao.observe(contentId)
    fun observeAll(): Flow<List<DownloadEntity>> = dao.observeAll()

    suspend fun get(contentId: String): DownloadEntity? = dao.get(contentId)

    fun enqueue(contentId: String, type: String, name: String, url: String) {
        val request = OneTimeWorkRequestBuilder<ContentDownloadWorker>()
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .setInputData(
                Data.Builder()
                    .putString(ContentDownloadWorker.KEY_CONTENT_ID, contentId)
                    .putString(ContentDownloadWorker.KEY_TYPE, type)
                    .putString(ContentDownloadWorker.KEY_NAME, name)
                    .putString(ContentDownloadWorker.KEY_URL, url)
                    .build()
            )
            .build()
        // Unique per contentId — tapping download twice resumes/retries the same
        // job instead of starting a duplicate.
        workManager.enqueueUniqueWork(uniqueName(contentId), ExistingWorkPolicy.KEEP, request)
    }

    suspend fun remove(contentId: String, localPath: String?) {
        workManager.cancelUniqueWork(uniqueName(contentId))
        localPath?.let { java.io.File(it).delete() }
        dao.delete(contentId)
    }

    private fun uniqueName(contentId: String) = "download-$contentId"

    // Decrypts a completed download to a plaintext cache file for playback/
    // rendering (ExoPlayer and PdfRenderer both need a plain local file, not an
    // encrypted stream). Cache files are cleaned up by Android's normal cache
    // eviction — not deleted immediately since the same content may be reopened.
    suspend fun decryptToCache(entity: DownloadEntity): File? = withContext(Dispatchers.IO) {
        val encryptedPath = entity.localPath ?: return@withContext null
        val cacheFile = File(context.cacheDir, "playback-${entity.contentId}")
        if (cacheFile.exists() && cacheFile.length() > 0) return@withContext cacheFile
        try {
            val masterKey = MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build()
            val encryptedFile = EncryptedFile.Builder(
                context, File(encryptedPath), masterKey, EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
            ).build()
            encryptedFile.openFileInput().use { input ->
                cacheFile.outputStream().use { output -> input.copyTo(output) }
            }
            cacheFile
        } catch (e: Exception) {
            null
        }
    }
}
