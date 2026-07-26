package `in`.uprep.app.data.session

import android.content.Context
import android.content.SharedPreferences

// Synchronous storage for the single `uprep_auth` session cookie, read/written
// by AppCookieJar on OkHttp's dispatcher thread (not the main thread, so a
// blocking SharedPreferences call here is fine — DataStore's async API would
// be awkward inside OkHttp's synchronous CookieJar interface).
class CookieStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("uprep_cookie_store", Context.MODE_PRIVATE)

    fun getCookie(): String? = prefs.getString(KEY_COOKIE, null)

    fun setCookie(rawSetCookieHeader: String?) {
        prefs.edit().putString(KEY_COOKIE, rawSetCookieHeader).apply()
    }

    fun clear() {
        prefs.edit().remove(KEY_COOKIE).apply()
    }

    companion object {
        private const val KEY_COOKIE = "cookie_value"
    }
}
