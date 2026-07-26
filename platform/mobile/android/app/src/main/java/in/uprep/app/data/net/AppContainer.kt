package `in`.uprep.app.data.net

import android.content.Context
import `in`.uprep.app.data.api.AuthApi
import `in`.uprep.app.data.api.LearnApi
import `in`.uprep.app.data.session.CookieStore
import `in`.uprep.app.data.session.SessionStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Manual DI container (no framework — the app is small enough that Hilt/Koin
// would be pure overhead). One instance lives on the Application, everything
// below is a cheap singleton built from it.
class AppContainer(context: Context) {
    val cookieStore = CookieStore(context)
    val sessionStore = SessionStore(context)
    val cookieJar = AppCookieJar(cookieStore)

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .cookieJar(cookieJar)
        .addInterceptor(
            HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC }
        )
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(NetworkConfig.BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val authApi: AuthApi = retrofit.create(AuthApi::class.java)
    val learnApi: LearnApi = retrofit.create(LearnApi::class.java)

    // Absolute URL for a possibly-relative content path (e.g. "/uploads/x.mp4").
    fun absoluteUrl(path: String): String =
        if (path.startsWith("http://") || path.startsWith("https://")) path
        else NetworkConfig.BASE_URL + path
}
