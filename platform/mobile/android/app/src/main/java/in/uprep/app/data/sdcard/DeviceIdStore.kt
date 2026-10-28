package `in`.uprep.app.data.sdcard

import android.content.Context
import android.provider.Settings
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.util.UUID

private val Context.deviceDataStore by preferencesDataStore(name = "uprep_device")
private val KEY_DEVICE_ID = stringPreferencesKey("device_id")

// The device identifier behind the /api/seller/verify device lock (an access
// code works only on the first device that activates it).
//
// Found live: this used to be a random UUID created on first run, which dies
// with the app's data — reinstalling the app or "Clear data" turned the same
// tablet into a "different device" and the code refused it ("already been
// used on another device"). New installs now use ANDROID_ID, which survives
// reinstalls on the same tablet (per signing key + user since Android 8).
// An ID already stored is kept, so tablets activated under the old scheme
// stay matched to their codes.
class DeviceIdStore(private val context: Context) {
    suspend fun get(): String {
        val existing = context.deviceDataStore.data.first()[KEY_DEVICE_ID]
        if (existing != null) return existing
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            ?.takeIf { it.isNotBlank() && it != "9774d56d682e549c" } // known-bad emulator/legacy value
        val fresh = androidId?.let { "android-$it" } ?: UUID.randomUUID().toString()
        context.deviceDataStore.edit { it[KEY_DEVICE_ID] = fresh }
        return fresh
    }
}
