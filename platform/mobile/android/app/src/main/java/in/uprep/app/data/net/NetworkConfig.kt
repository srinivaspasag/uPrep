package `in`.uprep.app.data.net

// Single source of truth for the deployed backend origin — shared by the
// Retrofit client (native screens) and the WebView fallback screen, so both
// always point at the same host and the shared auth cookie stays valid for
// both.
object NetworkConfig {
    const val BASE_URL = "https://65.2.108.70.sslip.io"
    const val COOKIE_NAME = "uprep_auth"
    const val COOKIE_DOMAIN = "65.2.108.70.sslip.io"
}
