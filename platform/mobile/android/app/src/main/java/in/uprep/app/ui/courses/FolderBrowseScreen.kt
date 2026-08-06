package `in`.uprep.app.ui.courses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import `in`.uprep.app.data.api.ContentItem
import `in`.uprep.app.data.api.SubfolderInfo
import `in`.uprep.app.data.db.DownloadStatus
import `in`.uprep.app.data.download.DownloadRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderBrowseScreen(
    folderId: String,
    resolveUrl: (String) -> String,
    downloadRepository: DownloadRepository,
    viewModelFactory: FolderBrowseViewModel.Factory,
    onOpenSubfolder: (String) -> Unit,
    onOpenVideo: (ContentItem) -> Unit,
    onOpenDocument: (ContentItem) -> Unit,
    onOpenTest: (ContentItem) -> Unit
) {
    val viewModel: FolderBrowseViewModel = viewModel(
        key = folderId,
        factory = viewModelFactory
    )
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(state.folder?.name ?: "Course") })

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error ?: "Something went wrong")
            }
            state.subfolders.isEmpty() && state.items.isEmpty() -> Box(
                Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { Text("Nothing here yet") }
            else -> LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.subfolders) { sub: SubfolderInfo ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenSubfolder(sub.id) }
                    ) {
                        Text(
                            "📁  ${sub.name}",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                items(state.items) { item: ContentItem ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                when (item.type) {
                                    "VIDEO" -> onOpenVideo(item)
                                    "TEST" -> onOpenTest(item)
                                    "DOCUMENT" -> onOpenDocument(item)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 16.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${badge(item.type)}  ${item.name}",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f)
                            )
                            // Only direct files are downloadable — Vimeo/YouTube
                            // items are player pages, not fetchable video files.
                            val downloadable = item.type == "DOCUMENT" ||
                                (item.type == "VIDEO" && item.embedUrl.isNullOrEmpty())
                            if (downloadable) {
                                DownloadButton(
                                    downloadRepository = downloadRepository,
                                    item = item,
                                    resolveUrl = resolveUrl
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Per-item download control: idle -> tap to enqueue -> progress -> done/retry.
// A separate clickable region from the card body so it doesn't also navigate.
@Composable
private fun DownloadButton(
    downloadRepository: DownloadRepository,
    item: ContentItem,
    resolveUrl: (String) -> String
) {
    val entity by downloadRepository.observe(item.id).collectAsState(initial = null)

    when (entity?.status) {
        DownloadStatus.COMPLETE -> IconButton(onClick = {}) {
            Text("✓", color = MaterialTheme.colorScheme.primary)
        }
        DownloadStatus.DOWNLOADING, DownloadStatus.QUEUED -> Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            val e = entity
            val progress = if (e != null && e.totalBytes > 0) e.downloadedBytes.toFloat() / e.totalBytes else 0f
            CircularProgressIndicator(progress = { progress }, modifier = Modifier.size(24.dp))
        }
        else -> IconButton(onClick = {
            val url = item.url?.let(resolveUrl) ?: return@IconButton
            downloadRepository.enqueue(item.id, item.type, item.name, url)
        }) {
            Text("⬇")
        }
    }
}

private fun badge(type: String) = when (type) {
    "VIDEO" -> "🎬"
    "DOCUMENT" -> "📄"
    "TEST" -> "📝"
    else -> "•"
}
