package `in`.uprep.app.ui.player

import android.view.LayoutInflater
import android.webkit.WebView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import `in`.uprep.app.R
import `in`.uprep.app.data.net.NetworkConfig
import java.util.regex.Pattern

// Two playback paths, matching what platform/web's own player supports plus
// the legacy-parity native YouTube player:
// - YouTube (provider == "YOUTUBE"): native playback via the actively
//   maintained android-youtube-player library (IFrame API under the hood,
//   but with correct fullscreen/lifecycle handling — legacy used Google's
//   now-deprecated YouTube Android Player SDK for the same native-controls
//   effect).
// - Everything else (Vimeo, direct uploads): ExoPlayer for a direct URL, or
//   a WebView iframe embed for Vimeo — both already match what the legacy
//   app itself does for these two cases.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    contentId: String,
    name: String,
    directUrl: String?,
    embedUrl: String?,
    provider: String?
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(name) })
        val youTubeId = if (provider == "YOUTUBE") extractYouTubeId(embedUrl ?: directUrl) else null
        when {
            youTubeId != null -> NativeYouTubePlayer(youTubeId)
            embedUrl != null -> EmbedPlayer(embedUrl)
            directUrl != null -> DirectPlayer(directUrl)
            else -> Text("This video can't be played")
        }
    }
}

// Handles both the /embed/{id} form (what platform/web sends for YouTube) and
// a plain /watch?v={id} URL, so this works from either field.
private fun extractYouTubeId(url: String?): String? {
    if (url.isNullOrBlank()) return null
    val patterns = listOf(
        Pattern.compile("embed/([A-Za-z0-9_-]{6,})"),
        Pattern.compile("[?&]v=([A-Za-z0-9_-]{6,})"),
        Pattern.compile("youtu\\.be/([A-Za-z0-9_-]{6,})")
    )
    for (p in patterns) {
        val m = p.matcher(url)
        if (m.find()) return m.group(1)
    }
    return null
}

@Composable
private fun NativeYouTubePlayer(videoId: String) {
    val lifecycleOwner = LocalLifecycleOwner.current
    AndroidView(
        factory = { ctx ->
            val playerView = YouTubePlayerView(ctx)
            lifecycleOwner.lifecycle.addObserver(playerView)
            playerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    youTubePlayer.loadVideo(videoId, 0f)
                }
            })
            playerView
        },
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
    )
}

// internal, not private: reused directly by ui.sdcard's local-file player
// screen (a freshly-decrypted SD-card file is played the same way a
// downloaded one is — this composable only ever needed a URL string).
@Composable
internal fun DirectPlayer(url: String) {
    val context = LocalContext.current
    val exoPlayer = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(Unit) {
        onDispose { exoPlayer.release() }
    }
    // Media3's PlayerView ships a fullscreen/enlarge button in its default
    // control overlay, but it only becomes visible once a fullscreen-mode
    // listener is registered — without this call the button never renders
    // at all, which is exactly why tapping the player never enlarged it.
    // Also: a fixed 240dp box (the old modifier) stays small on any screen
    // regardless of size; a width-filling 16:9 box scales with the device
    // instead, and the fullscreen toggle takes it the rest of the way.
    var fullscreen by remember { mutableStateOf(false) }
    Box(
        modifier = if (fullscreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth().aspectRatio(16f / 9f)
    ) {
        AndroidView(
            factory = { ctx ->
                // Inflate from XML (not `PlayerView(ctx)`) — that's the only way
                // to reach the `surface_type="texture_view"` attribute; the bare
                // constructor always defaults to SurfaceView.
                (LayoutInflater.from(ctx).inflate(R.layout.player_view, null) as PlayerView).apply {
                    player = exoPlayer
                    setControllerOnFullScreenModeChangedListener { isFullScreen ->
                        fullscreen = isFullScreen
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Composable
private fun EmbedPlayer(embedUrl: String) {
    // Vimeo's own control overlay (rewind/forward/gear/fullscreen — all
    // rendered by Vimeo's JS inside the page, not by us) already has a
    // fullscreen button, but tapping it did nothing: it calls the browser's
    // HTML5 Fullscreen API, which a WebView only honors if the host app
    // implements onShowCustomView/onHideCustomView — without them the
    // request silently fails and the player just stays boxed at its normal
    // size. `customView` holds whatever View the WebView hands us for the
    // duration of fullscreen; when present we show that instead of the
    // WebView and let it fill the screen.
    var customView by remember { mutableStateOf<android.view.View?>(null) }
    var customViewCallback by remember { mutableStateOf<android.webkit.WebChromeClient.CustomViewCallback?>(null) }
    // Belt-and-suspenders: Vimeo's own fullscreen button calls the page's
    // JS-side pseudo-fullscreen, which — inside a WebView boxed to a fixed
    // aspect ratio — has no bigger box to expand into even when the
    // Fullscreen API path above does fire; found live, tapping it produced
    // no visible change. This app-level toggle is guaranteed to work
    // regardless of what the embedded page does internally.
    var manualFullscreen by remember { mutableStateOf(false) }
    val fullscreen = customView != null || manualFullscreen

    Box(
        modifier = if (fullscreen) Modifier.fillMaxSize() else Modifier.fillMaxWidth().aspectRatio(16f / 9f)
    ) {
        AndroidView(
            factory = { ctx ->
                WebView(ctx).apply {
                    // Explicit hardware layer — WebView's default layer handling
                    // conflicts with Compose's own compositing here, and without
                    // this the WebView renders as an opaque black rectangle that
                    // covers the whole screen (found live on Android 14 /
                    // WebView 113), not just its box. Tried all three layer-type
                    // states (none/hardware/software) against Android 16 /
                    // WebView 133 — the Vimeo player never paints on that
                    // combination regardless, so this stays on for the Android
                    // 14 case it demonstrably fixes without making 16 any worse.
                    setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null)
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.mediaPlaybackRequiresUserGesture = false
                    webChromeClient = object : android.webkit.WebChromeClient() {
                        // Without this override, a <video> with no explicit
                        // poster falls back to WebView's built-in
                        // `android-webview-video-poster:` internal fetch, which
                        // fails (CORS-blocked, then an internal "Pipe closed"
                        // IOException generating its placeholder bitmap — found
                        // live on Android 16 / WebView 133). Returning a real
                        // bitmap directly here bypasses that broken internal
                        // path entirely.
                        override fun getDefaultVideoPoster(): android.graphics.Bitmap =
                            android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888)

                        override fun onShowCustomView(view: android.view.View, callback: CustomViewCallback) {
                            customView = view
                            customViewCallback = callback
                        }

                        override fun onHideCustomView() {
                            customView = null
                            customViewCallback?.onCustomViewHidden()
                            customViewCallback = null
                        }
                    }
                    // Vimeo (and similarly-configured providers) restrict
                    // embedding to a whitelisted referring domain — normally
                    // satisfied automatically in a browser tab because the page
                    // has a real origin, but a WebView loading the embed URL
                    // directly sends no Referer at all otherwise. Found live:
                    // Vimeo's own response without this was "Because of its
                    // privacy settings, this video cannot be played here."
                    loadUrl(embedUrl, mapOf("Referer" to "${NetworkConfig.BASE_URL}/"))
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        // Stacked on top of (not instead of) the WebView, covering it
        // entirely — simpler and more robust than trying to resize the
        // WebView itself, and the WebView is left running underneath
        // exactly as it was.
        customView?.let { view ->
            AndroidView(factory = { view }, modifier = Modifier.fillMaxSize())
        }
        // Only shown when the page hasn't already taken over via
        // onShowCustomView — no point stacking two enlarge controls.
        if (customView == null) {
            IconButton(
                onClick = { manualFullscreen = !manualFullscreen },
                modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
            ) {
                Text(
                    if (manualFullscreen) "⤡" else "⤢",
                    color = Color.White,
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                )
            }
        }
    }
}
