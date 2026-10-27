package `in`.uprep.app.ui.offline

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import `in`.uprep.app.data.offline.CachedQuestion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NativeTestScreen(
    viewModel: NativeTestViewModel,
    onFinished: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var confirmSubmitOpen by remember { mutableStateOf(false) }

    when {
        state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(state.error ?: "Something went wrong")
        }
        state.submitted -> SubmittedScreen(syncWarning = state.submitError, onDone = onFinished)
        else -> {
            val question = state.questions.getOrNull(state.currentIndex)
            Column(Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text(state.test?.name ?: "Test") },
                    actions = { Text(formatTime(state.remainingSeconds), modifier = Modifier.padding(end = 16.dp)) }
                )

                QuestionPalette(
                    questions = state.questions,
                    currentIndex = state.currentIndex,
                    answers = state.answers,
                    marked = state.marked,
                    visited = state.visited,
                    onJump = viewModel::goTo
                )

                if (question != null) {
                    Column(
                        Modifier
                            .weight(1f)
                            .padding(16.dp)
                    ) {
                        Text(
                            "Q${state.currentIndex + 1}. ${question.content}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(16.dp))
                        val selected = state.answers[question.id] ?: emptyList()
                        val options = optionsOf(question)
                        options.forEachIndexed { index, optionText ->
                            val value = index.toString()
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { viewModel.selectOption(question.id, value) }
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (question.type.equals("SCQ", ignoreCase = true)) {
                                    RadioButton(selected = selected.contains(value), onClick = { viewModel.selectOption(question.id, value) })
                                } else {
                                    Checkbox(checked = selected.contains(value), onCheckedChange = { viewModel.selectOption(question.id, value) })
                                }
                                Text(optionText, modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                } else {
                    Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No questions in this test")
                    }
                }

                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    OutlinedButton(onClick = viewModel::previous, enabled = state.currentIndex > 0) { Text("Previous") }
                    TextButton(onClick = { question?.let { viewModel.toggleMark(it.id) } }) {
                        Text(if (question != null && state.marked.contains(question.id)) "Unmark" else "Mark for review")
                    }
                    if (state.currentIndex < state.questions.size - 1) {
                        Button(onClick = viewModel::next) { Text("Next") }
                    } else {
                        Button(onClick = { confirmSubmitOpen = true }, enabled = !state.submitting) { Text("Submit") }
                    }
                }
            }

            if (confirmSubmitOpen) {
                AlertDialog(
                    onDismissRequest = { confirmSubmitOpen = false },
                    title = { Text("Submit test?") },
                    text = {
                        val answered = state.answers.count { it.value.isNotEmpty() }
                        Text("You've answered $answered of ${state.questions.size} questions. This can't be undone.")
                    },
                    confirmButton = {
                        TextButton(onClick = { confirmSubmitOpen = false; viewModel.submit() }) { Text("Submit") }
                    },
                    dismissButton = {
                        TextButton(onClick = { confirmSubmitOpen = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}

// Options are stored pre-serialized on CachedQuestion (see
// OfflineTestRepository's comment on why) — this just decodes them for
// display; kept as a free function rather than ViewModel state since it's
// pure derived data, not something that changes independently.
private fun optionsOf(question: CachedQuestion): List<String> =
    runCatching {
        com.google.gson.Gson().fromJson(question.optionsJson, Array<String>::class.java).toList()
    }.getOrDefault(emptyList())

@Composable
private fun QuestionPalette(
    questions: List<CachedQuestion>,
    currentIndex: Int,
    answers: Map<String, List<String>>,
    marked: Set<String>,
    visited: Set<String>,
    onJump: (Int) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(questions.size) { index ->
            val q = questions[index]
            val color = when {
                marked.contains(q.id) -> Color(0xFF9C27B0)
                !(answers[q.id] ?: emptyList()).isEmpty() -> Color(0xFF2E7D32)
                visited.contains(q.id) -> Color(0xFFC62828)
                else -> Color(0xFF9E9E9E)
            }
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(if (index == currentIndex) MaterialTheme.colorScheme.primary else color)
                    .clickable { onJump(index) },
                contentAlignment = Alignment.Center
            ) {
                Text("${index + 1}", color = Color.White, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
private fun SubmittedScreen(syncWarning: String?, onDone: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Test submitted", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(8.dp))
        Text(
            syncWarning ?: "Your answers have been recorded.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onDone) { Text("Done") }
    }
}

private fun formatTime(totalSeconds: Int): String {
    val m = totalSeconds / 60
    val s = totalSeconds % 60
    return "%02d:%02d".format(m, s)
}
