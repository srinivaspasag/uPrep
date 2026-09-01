package `in`.uprep.app.data.sdcard

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.documentfile.provider.DocumentFile
import com.google.gson.Gson
import `in`.uprep.app.data.api.SellerApi
import `in`.uprep.app.data.api.SellerVerifyRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.File

private val Context.sdCardDataStore by preferencesDataStore(name = "uprep_sdcard")
private val KEY_FOLDER_URI = stringPreferencesKey("folder_uri")
private fun keyPrefKey(groupId: String) = stringPreferencesKey("group_key_$groupId")
private fun expiryPrefKey(groupId: String) = longPreferencesKey("group_key_expiry_$groupId")

sealed class ActivateResult {
    object Success : ActivateResult()
    data class Failed(val message: String) : ActivateResult()
}

// Everything the SD-card reader needs on top of a picked Storage Access
// Framework tree: persisting which folder was picked, persisting the
// per-group AES key once /api/seller/verify hands it over, reading
// manifest.json off the card, and decrypting one content item into the
// app's cache for playback. One online call in the whole class — verify();
// everything else is local file I/O, matching how legacy's tablet app only
// ever phoned home once per access code too (see AccessCodeManager parity
// note on the server's /api/seller/verify route).
class SdCardRepository(private val context: Context) {
    private val gson = Gson()

    companion object {
        private const val TAG = "SdCardRepository"
    }

    suspend fun savedFolderUri(): Uri? =
        context.sdCardDataStore.data.first()[KEY_FOLDER_URI]?.let { Uri.parse(it) }

    suspend fun saveFolderUri(uri: Uri) {
        context.sdCardDataStore.edit { it[KEY_FOLDER_URI] = uri.toString() }
    }

    suspend fun hasKeyFor(groupId: String): Boolean =
        context.sdCardDataStore.data.first()[keyPrefKey(groupId)] != null

    private suspend fun keyFor(groupId: String): String? =
        context.sdCardDataStore.data.first()[keyPrefKey(groupId)]

    // Access codes are valid for 1 year from generation (see the server's
    // lib/expiry.ts). The expiry handed back by /api/seller/verify is
    // cached alongside the key so this check works with zero connectivity —
    // the whole point of activating once and playing offline forever until
    // then.
    suspend fun isExpired(groupId: String): Boolean {
        val expiresAt = context.sdCardDataStore.data.first()[expiryPrefKey(groupId)] ?: return false
        return System.currentTimeMillis() > expiresAt
    }

    // Reads manifest.json from the previously-picked SAF tree. Returns null
    // if no folder is picked yet, or the folder doesn't look like a card
    // packaged by the Seller Dashboard (no manifest.json at its root).
    suspend fun readManifest(): SdCardManifest? = withContext(Dispatchers.IO) {
        val treeUri = savedFolderUri() ?: return@withContext null
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext null
        val manifestFile = root.findFile("manifest.json") ?: return@withContext null
        val text = context.contentResolver.openInputStream(manifestFile.uri)?.use { it.readBytes().decodeToString() }
            ?: return@withContext null
        try {
            gson.fromJson(text, SdCardManifest::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // The one online call — redeems an access code for this device+user and
    // caches the returned AES-256-GCM key locally, keyed by groupId so a
    // tablet can hold keys for more than one card at once. Everything after
    // this succeeds works with zero connectivity.
    suspend fun activate(
        sellerApi: SellerApi,
        groupId: String,
        code: String,
        email: String,
        deviceId: String,
        userId: String
    ): ActivateResult = withContext(Dispatchers.IO) {
        try {
            val resp = sellerApi.verify(SellerVerifyRequest(code = code, email = email, deviceId = deviceId, userId = userId))
            val body = resp.body()
            if (!resp.isSuccessful || body?.encryptionKey == null) {
                return@withContext ActivateResult.Failed(body?.error ?: "Couldn't verify this code — check it and try again.")
            }
            context.sdCardDataStore.edit {
                it[keyPrefKey(groupId)] = body.encryptionKey
                if (body.expiresAt != null) it[expiryPrefKey(groupId)] = body.expiresAt
            }
            ActivateResult.Success
        } catch (e: Exception) {
            ActivateResult.Failed("No internet right now — activation needs one connected moment, try again once you have signal.")
        }
    }

    // Decrypts (or, for a "plain" packaged card, just copies) one manifest
    // item's file into the app cache dir, ready for ExoPlayer/PdfRenderer —
    // same destination shape as DownloadRepository.decryptToCache() uses for
    // the network-download feature, so the existing player composables can
    // be pointed at either one interchangeably.
    suspend fun decryptToCache(groupId: String, item: SdCardManifestItem): File? = withContext(Dispatchers.IO) {
        val treeUri = savedFolderUri() ?: return@withContext null.also { Log.e(TAG, "decryptToCache: no saved folder uri") }
        val root = DocumentFile.fromTreeUri(context, treeUri) ?: return@withContext null.also { Log.e(TAG, "decryptToCache: fromTreeUri returned null for $treeUri") }
        val contentDir = root.findFile("content") ?: return@withContext null.also { Log.e(TAG, "decryptToCache: no content/ dir under $treeUri; children=${root.listFiles().map { it.name }}") }

        val encryptedName = item.encryptedFileName
        val plainName = item.fileName
        val cacheFile = File(context.cacheDir, "sdcard-${item.id}")
        if (cacheFile.exists() && cacheFile.length() > 0) return@withContext cacheFile

        if (isExpired(groupId)) {
            return@withContext null.also { Log.e(TAG, "decryptToCache: access for group $groupId has expired") }
        }

        try {
            if (encryptedName != null) {
                val key = keyFor(groupId) ?: return@withContext null.also { Log.e(TAG, "decryptToCache: no cached key for group $groupId") }
                val source = contentDir.findFile(encryptedName) ?: return@withContext null.also { Log.e(TAG, "decryptToCache: '$encryptedName' not found; siblings=${contentDir.listFiles().map { it.name }}") }
                context.contentResolver.openInputStream(source.uri)?.use { input ->
                    cacheFile.outputStream().use { output -> SdCardCrypto.decrypt(input, key, output) }
                } ?: return@withContext null.also { Log.e(TAG, "decryptToCache: openInputStream returned null for ${source.uri}") }
            } else if (plainName != null) {
                val source = contentDir.findFile(plainName) ?: return@withContext null.also { Log.e(TAG, "decryptToCache: '$plainName' not found; siblings=${contentDir.listFiles().map { it.name }}") }
                context.contentResolver.openInputStream(source.uri)?.use { input ->
                    cacheFile.outputStream().use { output -> input.copyTo(output) }
                } ?: return@withContext null.also { Log.e(TAG, "decryptToCache: openInputStream returned null for ${source.uri}") }
            } else {
                return@withContext null.also { Log.e(TAG, "decryptToCache: item has neither encryptedFileName nor fileName") }
            }
            cacheFile
        } catch (e: Exception) {
            Log.e(TAG, "decryptToCache: failed for ${item.name}", e)
            cacheFile.delete()
            null
        }
    }
}
