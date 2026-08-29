package `in`.uprep.app.ui.courses

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import `in`.uprep.app.data.api.CourseSummary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListScreen(
    viewModel: CoursesViewModel = viewModel(),
    onOpenCourse: (CourseSummary) -> Unit,
    onOpenDownloads: () -> Unit,
    onOpenSdCard: () -> Unit,
    onLogout: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("My Courses") },
            actions = {
                IconButton(onClick = onOpenSdCard) { Text("💾") }
                IconButton(onClick = onOpenDownloads) { Text("⬇") }
                IconButton(onClick = onLogout) { Text("⎋") }
            }
        )

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            state.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(state.error ?: "Something went wrong")
            }
            state.courses.isEmpty() -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No courses assigned yet")
            }
            else -> {
                // Group courses under the Program that granted them (e.g.
                // "JEE XI") — previously this screen showed Physics/Chem/Math
                // as a flat list with no indication of which program they
                // belonged to, even though CMDS clearly assigns by program.
                val groupedIds = state.programGroups.flatMap { it.courseIds }.toSet()
                val ungrouped = state.courses.filter { it.id !in groupedIds }
                LazyColumn(
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    state.programGroups.forEach { group ->
                        val groupCourses = state.courses.filter { it.id in group.courseIds }
                        if (groupCourses.isNotEmpty()) {
                            item(key = "header_${group.id}") {
                                Text(
                                    group.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            items(groupCourses, key = { "${group.id}_${it.id}" }) { course ->
                                CourseCard(course, onOpenCourse)
                            }
                        }
                    }
                    if (ungrouped.isNotEmpty()) {
                        if (state.programGroups.isNotEmpty()) {
                            item(key = "header_ungrouped") {
                                Text("Other Courses", style = MaterialTheme.typography.titleSmall)
                            }
                        }
                        items(ungrouped, key = { "ungrouped_${it.id}" }) { course ->
                            CourseCard(course, onOpenCourse)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CourseCard(course: CourseSummary, onOpenCourse: (CourseSummary) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenCourse(course) }
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(course.name, style = MaterialTheme.typography.titleMedium)
            Text(
                "${course.chapterCount} chapters",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
