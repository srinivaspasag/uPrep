package `in`.uprep.app.ui.doubts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.uprep.app.data.api.DoubtAnswer
import `in`.uprep.app.ui.common.relativeTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoubtDetailScreen(viewModel: DoubtDetailViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()
    var reply by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.doubt?.name ?: "Doubt",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = { IconButton(onClick = onBack) { Text("←") } }
            )
        },
        bottomBar = {
            Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = reply,
                    onValueChange = { reply = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("Write a reply...") }
                )
                Spacer(Modifier.width(8.dp))
                Button(
                    enabled = reply.isNotBlank() && !state.sending,
                    onClick = { viewModel.sendAnswer(reply); reply = "" }
                ) { Text("Send") }
            }
        }
    ) { padding ->
        when {
            state.loading -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error ?: "")
            }
            else -> LazyColumn(
                modifier = Modifier.padding(padding).fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Card(Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(state.doubt?.name ?: "", style = MaterialTheme.typography.titleLarge)
                            if (!state.doubt?.content.isNullOrBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(state.doubt?.content ?: "")
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Asked ${relativeTime(state.doubt?.timeCreated ?: 0)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                items(state.answers, key = { it.id }) { answer -> AnswerBubble(answer) }
                if (state.aiPending) {
                    item { Text("Aira is reviewing this one...", style = MaterialTheme.typography.bodySmall) }
                }
                if (state.answers.none { it.isAi } && !state.aiPending) {
                    item {
                        TextButton(onClick = { viewModel.askAira() }, enabled = !state.askingAira) {
                            Text(if (state.askingAira) "Asking Aira..." else "✨ Ask Aira")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnswerBubble(answer: DoubtAnswer) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (answer.isAi) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(if (answer.isAi) "✨ Aira" else answer.userName, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            Text(answer.content)
            answer.steps?.let { steps ->
                Spacer(Modifier.height(8.dp))
                steps.forEachIndexed { i, step ->
                    Column(Modifier.padding(top = 6.dp)) {
                        Text("Step ${i + 1}: ${step.title}", style = MaterialTheme.typography.labelMedium)
                        Text(step.body, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(relativeTime(answer.timeCreated), style = MaterialTheme.typography.bodySmall)
        }
    }
}
