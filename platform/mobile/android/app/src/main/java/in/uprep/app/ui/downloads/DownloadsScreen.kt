package `in`.uprep.app.ui.downloads

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.uprep.app.data.db.DownloadEntity
import `in`.uprep.app.data.db.DownloadStatus
import `in`.uprep.app.data.download.DownloadRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(downloadRepository: DownloadRepository) {
    val downloads by downloadRepository.observeAll().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Downloads") })

        if (downloads.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Nothing downloaded yet — tap ⬇ on a video or document to save it for offline use.")
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(downloads) { entity: DownloadEntity ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(entity.name, style = MaterialTheme.typography.titleSmall)
                                Text(statusText(entity), style = MaterialTheme.typography.bodySmall)
                            }
                            IconButton(onClick = {
                                scope.launch { downloadRepository.remove(entity.contentId, entity.localPath) }
                            }) {
                                Text("✕")
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun statusText(e: DownloadEntity): String = when (e.status) {
    DownloadStatus.COMPLETE -> "Downloaded — ${e.totalBytes / (1024 * 1024)} MB"
    DownloadStatus.DOWNLOADING -> {
        val pct = if (e.totalBytes > 0) (e.downloadedBytes * 100 / e.totalBytes) else 0
        "Downloading… $pct%"
    }
    DownloadStatus.QUEUED -> "Queued"
    DownloadStatus.PAUSED -> "Paused"
    DownloadStatus.FAILED -> "Failed — will retry"
}
