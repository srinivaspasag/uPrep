package `in`.uprep.app.data.net

import `in`.uprep.app.data.session.CookieStore
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

// Persists only the `uprep_auth` session cookie (httpOnly on the server side,
// which restricts browser JS — not a native HTTP client). Captured from
// Set-Cookie on login and replayed as Cookie on every subsequent request, so
// the whole existing cookie-based auth model works from Android with zero
// backend changes.
class AppCookieJar(private val store: CookieStore) : CookieJar {

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val auth = cookies.firstOrNull { it.name == NetworkConfig.COOKIE_NAME } ?: return
        store.setCookie(auth.value)
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val value = store.getCookie() ?: return emptyList()
        val cookie = Cookie.Builder()
            .name(NetworkConfig.COOKIE_NAME)
            .value(value)
            .domain(NetworkConfig.COOKIE_DOMAIN)
            .path("/")
            .secure()
            .build()
        return listOf(cookie)
    }

    fun clear() = store.clear()
}
