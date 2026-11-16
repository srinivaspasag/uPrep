package `in`.uprep.app.data.net

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.Interceptor
import okhttp3.Response

// Server logins last 7 days (SESSION_MAX_AGE_SEC in platform/web), but the app
// keeps its saved session forever. Found live: a student who last signed in
// on the tablet 15 days earlier opened the app to a dead-end "Couldn't load
// your courses" — every call was getting 401 and nothing sent them back to
// sign in. MainActivity listens here and returns to the login screen.
object SessionExpiry {
    private val _events = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val events: SharedFlow<Unit> = _events.asSharedFlow()

    fun signal() {
        _events.tryEmit(Unit)
    }

    // Only a 401 on a request that actually carried our login cookie means
    // "your login expired". Login calls themselves answer 401 for a wrong
    // password, and a request with no cookie (offline-login fallback) has no
    // server login to expire — neither should kick the student out.
    val interceptor = Interceptor { chain ->
        val request = chain.request()
        val response: Response = chain.proceed(request)
        val hadCookie = request.header("Cookie")?.contains(NetworkConfig.COOKIE_NAME) == true
        if (response.code == 401 && hadCookie && !request.url.encodedPath.startsWith("/api/auth/")) signal()
        response
    }
}
