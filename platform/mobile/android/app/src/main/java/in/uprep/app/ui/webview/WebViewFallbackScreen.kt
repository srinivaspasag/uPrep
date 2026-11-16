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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import `in`.uprep.app.data.net.NetworkConfig
import `in`.uprep.app.data.net.SessionExpiry
import `in`.uprep.app.data.session.UserSession
import org.json.JSONObject

// Everything not yet built natively (CMDS admin, test-taking, notifications,
// profile, doubts, ...) falls back to the live web app in a WebView — same
// behavior as the original all-WebView app, just scoped to one screen instead
// of the whole shell. The native session's auth cookie is injected first so
// there's no second login here.
//
//   path == null            -> loads the site root (staff land on /cmds via
//                               the server's own post-login redirect logic)
//   path == "/test/123"      -> deep-links straight to a specific page
// Web pages behind LmsShell (Doubts, Analytics, Activity, ...) gate on a
// client-side `sessionStorage["uprep_session"]` marker (see lib/session.ts),
// completely separate from the uprep_auth cookie — it's only ever populated
// by the web app's own browser login flow calling setSession(). The native
// app's login never runs that JS, so every fresh WebView load here would
// redirect straight to /login despite a perfectly valid cookie. Fix: mirror
// the already-durable native UserSession into that same sessionStorage key,
// injected in onPageStarted so it lands before the page's own React code
// hydrates and reads it.
private fun sessionInjectionScript(session: UserSession): String {
    val json = JSONObject().apply {
        put("id", session.id)
        put("firstName", session.firstName)
        put("lastName", session.lastName)
        put("memberId", session.memberId)
        put("profile", session.profile)
        put("isSuperAdmin", session.isSuperAdmin)
    }
    // JSONObject.toString() already produces valid, escaped JS-string-safe JSON.
    return "sessionStorage.setItem('uprep_session', '${json.toString().replace("\\", "\\\\").replace("'", "\\'")}');"
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebViewFallbackScreen(
    path: String?,
    cookieValue: String?,
    session: UserSession? = null,
    onLogout: () -> Unit,
    // Leaves this screen for the native one underneath (folder / course list).
    onClose: () -> Unit = {}
) {
    val context = LocalContext.current
    // Opened for a test (from a native folder or the course list): once the
    // test page navigates on to any /learn/ page — Back to Library/Folder,
    // Pause & exit, the logo — the test is over, so close this screen and
    // return to the native screen the student came from. Found live: those
    // exits loaded the website's Digital Library (subjects grouped by program,
    // with a Programs sidebar) inside this WebView, stranding students in the
    // web app instead of back on their dashboard.
    val closeWhenTestEnds = path?.startsWith("/test/") == true
    var closed by remember { mutableStateOf(false) }
    val close = {
        if (!closed) {
            closed = true
            onClose()
        }
    }
    var filePathCallback by remember { mutableStateOf<ValueCallback<Array<Uri>>?>(null) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }

    val fileChooserLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val uris = WebChromeClient.FileChooserParams.parseResult(result.resultCode, result.data)
        filePathCallback?.onReceiveValue(uris ?: emptyArray())
        filePathCallback = null
    }

    // Step back inside the web page while it has history; once it doesn't,
    // leave this screen. Before, Back was swallowed here with nothing to go
    // back to, so the hardware Back button did nothing at all.
    BackHandler(enabled = true) {
        val wv = webViewRef
        if (wv != null && wv.canGoBack()) wv.goBack() else close()
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    override fun onPageStarted(view: WebView, url: String?, favicon: android.graphics.Bitmap?) {
                        super.onPageStarted(view, url, favicon)
                        if (session != null) {
                            view.evaluateJavascript(sessionInjectionScript(session), null)
                        }
                    }

                    // Fires for full page loads AND the web app's in-page
                    // (pushState/replaceState) navigations, which never reach
                    // shouldOverrideUrlLoading.
                    override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
                        super.doUpdateVisitedHistory(view, url, isReload)
                        if (url == null) return
                        val p = Uri.parse(url).path.orEmpty()
                        // The web app sends a signed-out visitor to /login —
                        // inside the app that means the saved login expired.
                        if (p == "/login" || p.startsWith("/login/")) {
                            SessionExpiry.signal()
                            return
                        }
                        if (!closeWhenTestEnds) return
                        if (p.startsWith("/learn")) close()
                    }

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

    // Small overlay so there's always a way back to the native login/account
    // switcher — this screen has no app bar of its own (full-bleed WebView).
    IconButton(
        onClick = onLogout,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .statusBarsPadding()
            .padding(8.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
    ) {
        Text("⎋")
    }
    }
}
