package `in`.uprep.app.ui.webview

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.webkit.CookieManager
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import `in`.uprep.app.data.net.NetworkConfig

// Everything not yet built natively (CMDS admin, test-taking, notifications,
// profile, doubts, ...) falls back to the live web app in a WebView — same
// behavior as the original all-WebView app, just scoped to one screen instead
// of the whole shell. The native session's auth cookie is injected first so
// there's no second login here.
//
//   path == null            -> loads the site root (staff land on /cmds via
//                               the server's own post-login redirect logic)
//   path == "/test/123"      -> deep-links straight to a specific page
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewFallbackScreen(path: String?, cookieValue: String?) {
    val context = LocalContext.current
    var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        filePathCallback?.onReceiveValue(uris ?: emptyArray())
        filePathCallback = null
    }

    BackHandler(enabled = true) {
        val wv = webViewRef
        if (wv != null && wv.canGoBack()) wv.goBack()
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            // Share the native login session with this WebView — captured by
            // AppCookieJar from the same Set-Cookie the login API call returned.
            if (cookieValue != null) {
                CookieManager.getInstance().apply {
                    setAcceptCookie(true)
                    setCookie(NetworkConfig.BASE_URL, "${NetworkConfig.COOKIE_NAME}=$cookieValue; Path=/; Secure")
                    flush()
                }
            }

            WebView(ctx).apply {
                webViewRef = this
                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    databaseEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    javaScriptCanOpenWindowsAutomatically = true
                    mediaPlaybackRequiresUserGesture = false
                    allowFileAccess = true
                }

                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        val url = request.url
                        val host = url.host ?: return false
                        return if (host.contains(NetworkConfig.COOKIE_DOMAIN)) {
                            false
                        } else {
                            context.startActivity(Intent(Intent.ACTION_VIEW, url))
                            true
                        }
                    }
                }

                webChromeClient = object : WebChromeClient() {
                    override fun onShowFileChooser(
                        webView: WebView?,
                        callback: ValueCallback<Array<Uri>>?,
                        params: FileChooserParams?
                    ): Boolean {
                        filePathCallback?.onReceiveValue(null)
                        filePathCallback = callback
                        val intent = params?.createIntent()
                        if (intent == null) {
                            filePathCallback = null
                            return false
                        }
                        return try {
                            fileChooserLauncher.launch(intent)
                            true
                        } catch (e: Exception) {
                            filePathCallback = null
                            false
                        }
                    }
                }

                setDownloadListener { url, _, _, _, _ ->
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                }

                loadUrl(NetworkConfig.BASE_URL + (path ?: ""))
            }
        }
    )
}
