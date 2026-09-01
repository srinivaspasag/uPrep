package `in`.uprep.app.ui.analytics

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
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import `in`.uprep.app.data.api.TestResult
import `in`.uprep.app.ui.common.relativeTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnalyticsScreen(viewModel: AnalyticsViewModel, onBack: () -> Unit) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Analytics") },
                navigationIcon = { IconButton(onClick = onBack) { Text("←") } }
            )
        }
    ) { padding ->
        val data = state.data
        when {
            state.loading -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error ?: "")
            }
            data?.summary == null -> Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Attempt a test to see your analytics here")
            }
            else -> {
                val summary = data.summary!!
                LazyColumn(
                    modifier = Modifier.padding(padding).fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            SummaryStat("Tests", summary.testsAttempted.toString(), Modifier.weight(1f))
                            SummaryStat("Avg Score", "${summary.avgScore}%", Modifier.weight(1f))
                            SummaryStat("Accuracy", "${summary.accuracy}%", Modifier.weight(1f))
                        }
                    }
                    if (data.subjects.isNotEmpty()) {
                        item { Text("By subject", style = MaterialTheme.typography.titleMedium) }
                        items(data.subjects, key = { it.name }) { AccuracyRow(it.name, it.accuracy, it.total) }
                    }
                    if (data.types.isNotEmpty()) {
                        item { Text("By question type", style = MaterialTheme.typography.titleMedium) }
                        items(data.types, key = { it.type }) { AccuracyRow(it.type, it.accuracy, it.total) }
                    }
                    if (data.results.isNotEmpty()) {
                        item { Text("Recent tests", style = MaterialTheme.typography.titleMedium) }
                        items(data.results, key = { it.entityId + it.attemptedAt }) { TestResultRow(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun SummaryStat(label: String, value: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(value, style = MaterialTheme.typography.titleLarge)
            Text(label, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun AccuracyRow(name: String, accuracy: Int, total: Int) {
    Column(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(name)
            Text("$accuracy% ($total)", style = MaterialTheme.typography.bodySmall)
        }
        LinearProgressIndicator(
            progress = accuracy / 100f,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
        )
    }
}

@Composable
private fun TestResultRow(result: TestResult) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Column {
            Text(result.name)
            Text(relativeTime(result.attemptedAt), style = MaterialTheme.typography.bodySmall)
        }
        Text("${result.score}/${result.totalMarks}")
    }
}
