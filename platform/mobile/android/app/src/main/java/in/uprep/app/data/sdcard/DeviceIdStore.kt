package `in`.uprep.app.data.sdcard

import android.content.Context
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import java.util.UUID

private val Context.deviceDataStore by preferencesDataStore(name = "uprep_device")
private val KEY_DEVICE_ID = stringPreferencesKey("device_id")

// A stable per-install identifier for the /api/seller/verify device lock —
// legacy locked an access code to a device the same way, just using a
// different identifier scheme we don't have access to. A random UUID
// persisted on first use is the simplest equivalent: nothing else in this
// app tracks a device identifier today (confirmed: no ANDROID_ID/FCM usage
// anywhere), and this only ever needs to be stable for THIS install, not
// globally unique/hardware-tied.
class DeviceIdStore(private val context: Context) {
    suspend fun get(): String {
        val existing = context.deviceDataStore.data.first()[KEY_DEVICE_ID]
        if (existing != null) return existing
        val fresh = UUID.randomUUID().toString()
        context.deviceDataStore.edit { it[KEY_DEVICE_ID] = fresh }
        return fresh
    }
}
