package `in`.uprep.app.ui.offline

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
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
import `in`.uprep.app.data.offline.CachedTest
import `in`.uprep.app.ui.common.SyncStatusBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineTestsScreen(
    viewModel: OfflineTestsViewModel,
    onOpenTest: (CachedTest) -> Unit
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text("Offline Tests") })

        if (state.pendingSyncCount > 0) {
            SyncStatusBadge(pendingCount = state.pendingSyncCount)
        }

        if (state.tests.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No tests downloaded yet.\nOpen a test from a course and tap ⬇ to save it for offline use.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.tests, key = { it.testId }) { test ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenTest(test) }
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(test.name, style = MaterialTheme.typography.titleSmall)
                            Text(
                                "${test.durationMin} min · ${test.totalMarks} marks",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
