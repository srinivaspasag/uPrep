package `in`.uprep.app.ui.player

import android.view.LayoutInflater
import android.webkit.WebView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
import `in`.uprep.app.data.db.DownloadStatus
import `in`.uprep.app.data.download.DownloadRepository
import `in`.uprep.app.data.net.NetworkConfig
import kotlinx.coroutines.flow.collectLatest
import java.io.File
import java.util.regex.Pattern

// Three playback paths, matching what platform/web's own player supports plus
// the legacy-parity native YouTube player:
// - Downloaded (local file present, see FolderBrowseScreen's download
//   button): ExoPlayer against the decrypted local copy — no network needed.
// - YouTube (provider == "YOUTUBE"): native playback via the actively
//   maintained android-youtube-player library (IFrame API under the hood,
//   but with correct fullscreen/lifecycle handling — legacy used Google's
//   now-deprecated YouTube Android Player SDK for the same native-controls
//   effect).
// - Everything else (Vimeo, direct uploads not yet downloaded): ExoPlayer for
//   a direct URL, or a WebView iframe embed for Vimeo — both already match
//   what the legacy app itself does for these two cases.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(
    contentId: String,
    name: String,
    directUrl: String?,
    embedUrl: String?,
    provider: String?,
    downloadRepository: DownloadRepository
) {
    var localFile by remember { mutableStateOf<File?>(null) }
    LaunchedEffect(contentId) {
        downloadRepository.observe(contentId).collectLatest { entity ->
            localFile = if (entity?.status == DownloadStatus.COMPLETE) {
                downloadRepository.decryptToCache(entity)
            } else null
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(name) })
        val downloaded = localFile
        val youTubeId = if (provider == "YOUTUBE") extractYouTubeId(embedUrl ?: directUrl) else null
        when {
            downloaded != null -> DirectPlayer(downloaded.toURI().toString())
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
            .height(240.dp)
    )
}

@Composable
private fun DirectPlayer(url: String) {
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
    AndroidView(
        factory = { ctx ->
            // Inflate from XML (not `PlayerView(ctx)`) — that's the only way
            // to reach the `surface_type="texture_view"` attribute; the bare
            // constructor always defaults to SurfaceView.
            (LayoutInflater.from(ctx).inflate(R.layout.player_view, null) as PlayerView).apply {
                player = exoPlayer
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    )
}

@Composable
private fun EmbedPlayer(embedUrl: String) {
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
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    )
}
