package `in`.uprep.app.ui.doubts

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.uprep.app.data.api.DoubtSummary
import `in`.uprep.app.ui.common.relativeTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoubtsListScreen(
    viewModel: DoubtsListViewModel,
    onOpenDoubt: (String) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showNewDoubt by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newContent by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ask Aira") },
                navigationIcon = { IconButton(onClick = onBack) { Text("←") } },
                actions = { TextButton(onClick = { showNewDoubt = true }) { Text("+ New") } }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            TabRow(selectedTabIndex = if (state.filter == "open") 0 else 1) {
                Tab(selected = state.filter == "open", onClick = { viewModel.setFilter("open") }, text = { Text("Open") })
                Tab(selected = state.filter == "resolved", onClick = { viewModel.setFilter("resolved") }, text = { Text("Resolved") })
            }
            when {
                state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text(state.error ?: "") }
                state.doubts.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No doubts yet — tap + New to ask Aira something")
                }
                else -> LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.doubts, key = { it.id }) { doubt -> DoubtCard(doubt, onClick = { onOpenDoubt(doubt.id) }) }
                }
            }
        }
    }

    if (showNewDoubt) {
        AlertDialog(
            onDismissRequest = { showNewDoubt = false; viewModel.clearCreateError() },
            title = { Text("Ask Aira") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newTitle,
                        onValueChange = { newTitle = it },
                        label = { Text("Your question") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newContent,
                        onValueChange = { newContent = it },
                        label = { Text("Details (optional)") },
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    )
                    state.createError?.let {
                        Text(it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !state.creating,
                    onClick = {
                        viewModel.createDoubt(newTitle, newContent) { id ->
                            showNewDoubt = false
                            newTitle = ""
                            newContent = ""
                            onOpenDoubt(id)
                        }
                    }
                ) { Text(if (state.creating) "Asking..." else "Ask") }
            },
            dismissButton = {
                TextButton(onClick = { showNewDoubt = false; viewModel.clearCreateError() }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun DoubtCard(doubt: DoubtSummary, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Text(doubt.name, style = MaterialTheme.typography.titleMedium)
            doubt.subject?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "${doubt.answerCount} answer${if (doubt.answerCount == 1) "" else "s"} · ${relativeTime(doubt.timeCreated)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
