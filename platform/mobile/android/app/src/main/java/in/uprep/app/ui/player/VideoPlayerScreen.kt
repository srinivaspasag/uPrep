package `in`.uprep.app.ui.player

import android.annotation.SuppressLint
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

// Two playback paths, matching what platform/web's own course player does:
// - Uploaded files (embedUrl == null): direct ExoPlayer against the resolved
//   absolute MP4 URL.
// - YouTube/Vimeo (embedUrl set, see platform/web/lib/video.ts): rendered via
//   the provider's iframe embed in a WebView — ExoPlayer can't play those
//   without an extra extractor library, which is follow-up work, not v1.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VideoPlayerScreen(name: String, directUrl: String?, embedUrl: String?) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(name) })
        when {
            embedUrl != null -> EmbedPlayer(embedUrl)
            directUrl != null -> DirectPlayer(directUrl)
            else -> Text("This video can't be played")
        }
    }
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
        factory = { PlayerView(it).apply { player = exoPlayer } },
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    )
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
private fun EmbedPlayer(embedUrl: String) {
    AndroidView(
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                loadUrl(embedUrl)
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
    )
}
