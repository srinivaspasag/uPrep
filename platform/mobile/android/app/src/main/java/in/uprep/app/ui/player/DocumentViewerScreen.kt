package `in`.uprep.app.ui.player

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import `in`.uprep.app.data.download.DownloadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

// Native, in-app PDF rendering (Android's built-in PdfRenderer — legacy used
// the much heavier MuPDF native library; PdfRenderer needs no extra
// dependency and is enough for paged viewing). Prefers an already-downloaded
// local copy (see FolderBrowseScreen's download button); otherwise fetches a
// temporary copy to the cache dir just for viewing.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentViewerScreen(
    contentId: String,
    name: String,
    remoteUrl: String,
    downloadRepository: DownloadRepository
) {
    val context = LocalContext.current
    var localFile by remember { mutableStateOf<File?>(null) }
    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(contentId) {
        val downloaded = downloadRepository.get(contentId)
        localFile = if (downloaded != null && downloaded.status.name == "COMPLETE") {
            downloadRepository.decryptToCache(downloaded)
        } else {
            fetchToCache(context.cacheDir, contentId, remoteUrl)
        }
        if (localFile == null) error = "Couldn't load this document"
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(name) })
        val file = localFile
        when {
            error != null -> DocumentError(error!!, remoteUrl, context)
            file == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            else -> PdfPager(file)
        }
    }
}

@Composable
private fun DocumentError(message: String, remoteUrl: String, context: android.content.Context) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message)
            TextButton(onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(remoteUrl)))
            }) {
                Text("Open externally instead")
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun PdfPager(file: File) {
    var pageCount by remember { mutableStateOf(0) }
    var renderer by remember { mutableStateOf<PdfRenderer?>(null) }
    var pfd by remember { mutableStateOf<ParcelFileDescriptor?>(null) }

    LaunchedEffect(file) {
        val descriptor = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
        val r = PdfRenderer(descriptor)
        pfd = descriptor
        renderer = r
        pageCount = r.pageCount
    }
    DisposableEffect(file) {
        onDispose {
            renderer?.close()
            pfd?.close()
        }
    }

    val currentRenderer = renderer
    if (currentRenderer == null || pageCount == 0) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }

    val pagerState = rememberPagerState(pageCount = { pageCount })
    Column(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState, modifier = Modifier.weight(1f)) { pageIndex ->
            var bitmap by remember(pageIndex) { mutableStateOf<Bitmap?>(null) }
            LaunchedEffect(pageIndex) {
                bitmap = renderPage(currentRenderer, pageIndex)
            }
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                bitmap?.let {
                    Image(bitmap = it.asImageBitmap(), contentDescription = "Page ${pageIndex + 1}")
                } ?: CircularProgressIndicator()
            }
        }
        Text(
            "Page ${pagerState.currentPage + 1} of $pageCount",
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

private suspend fun renderPage(renderer: PdfRenderer, index: Int): Bitmap = withContext(Dispatchers.IO) {
    synchronized(renderer) {
        renderer.openPage(index).use { page ->
            val bitmap = Bitmap.createBitmap(page.width * 2, page.height * 2, Bitmap.Config.ARGB_8888)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            bitmap
        }
    }
}

private suspend fun fetchToCache(cacheDir: File, contentId: String, url: String): File? =
    withContext(Dispatchers.IO) {
        try {
            val dest = File(cacheDir, "doc-$contentId.pdf")
            if (dest.exists() && dest.length() > 0) return@withContext dest
            OkHttpClient().newCall(Request.Builder().url(url).build()).execute().use { response ->
                if (!response.isSuccessful) return@withContext null
                val body = response.body ?: return@withContext null
                dest.outputStream().use { out -> body.byteStream().copyTo(out) }
            }
            dest
        } catch (e: Exception) {
            null
        }
    }
