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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FolderBrowseScreen(
    folderId: String,
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
                                    "DOCUMENT", "BOOK" -> onOpenDocument(item)
                                }
                            }
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "${badge(item.type)}  ${item.name}",
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun badge(type: String) = when (type) {
    "VIDEO" -> "🎬"
    "DOCUMENT" -> "📄"
    "BOOK" -> "📖"
    "TEST" -> "📝"
    else -> "•"
}
