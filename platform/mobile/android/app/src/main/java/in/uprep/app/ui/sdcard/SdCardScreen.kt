package `in`.uprep.app.ui.sdcard

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import `in`.uprep.app.data.sdcard.SdCardCourseGroup
import `in`.uprep.app.data.sdcard.SdCardFolder
import `in`.uprep.app.data.sdcard.SdCardManifestItem
import `in`.uprep.app.data.sdcard.SdCardRepository
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SdCardScreen(
    viewModel: SdCardViewModel,
    repository: SdCardRepository,
    onOpenVideo: (name: String, file: File) -> Unit,
    onOpenDocument: (name: String, file: File) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var decryptingId by remember { mutableStateOf<String?>(null) }
    // Null = showing the course list (or the root folder browse, for older
    // cards with no course grouping); non-null = drilled into one course.
    var selectedCourse by remember { mutableStateOf<SdCardCourseGroup?>(null) }
    // Path of folder ids drilled into below the current course (or below
    // root, when there's no course grouping at all) — empty means "showing
    // this course's/root's direct chapters," matching FolderBrowseScreen's
    // per-level semantics but as local state instead of a nav-graph, since
    // the whole manifest is already loaded in memory (no per-level fetch
    // needed for an offline reader).
    var folderStack by remember { mutableStateOf<List<SdCardFolder>>(emptyList()) }

    val pickFolder = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            context.contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )
            viewModel.onFolderPicked(uri)
        }
    }

    fun openItem(item: SdCardManifestItem) {
        val groupId = state.manifest?.groupId ?: return
        decryptingId = item.id
        scope.launch {
            val file = repository.decryptToCache(groupId, item)
            decryptingId = null
            if (file != null) {
                val name = item.name ?: "Content"
                if (item.type == "VIDEO") onOpenVideo(name, file) else onOpenDocument(name, file)
            }
        }
    }

    val courseGroups = state.manifest?.courseGroups.orEmpty()
    val hasCourseGrouping = courseGroups.isNotEmpty()
    val allFolders = state.manifest?.folders.orEmpty()
    // Root of the current drill-down: the selected course's own id (its
    // direct children are that course's top-level chapters), or null when
    // there's no course grouping at all (root-level folders, parentId==null).
    val rootFolderId = selectedCourse?.courseId
    val currentFolderId = folderStack.lastOrNull()?.id ?: rootFolderId
    val hasFolderTree = allFolders.isNotEmpty()

    fun selectCourse(course: SdCardCourseGroup?) {
        selectedCourse = course
        folderStack = emptyList()
    }

    fun goBack() {
        if (folderStack.isNotEmpty()) {
            folderStack = folderStack.dropLast(1)
        } else if (selectedCourse != null) {
            selectCourse(null)
        }
    }

    // Without this, the system/hardware back button bypasses goBack()
    // entirely and pops the whole screen off the nav graph (straight back to
    // the course list), even mid-drill-down — only the in-app "←" toolbar
    // icon was wired to goBack(). Disabled at the top level (course list /
    // root) so back there still exits the screen as expected.
    BackHandler(enabled = selectedCourse != null || folderStack.isNotEmpty()) { goBack() }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    when {
                        folderStack.isNotEmpty() -> folderStack.last().name ?: "Folder"
                        selectedCourse != null -> selectedCourse!!.courseName ?: "Course"
                        hasCourseGrouping -> state.manifest?.programName ?: "Offline via SD Card"
                        else -> "Offline via SD Card"
                    }
                )
            },
            navigationIcon = {
                if (selectedCourse != null || folderStack.isNotEmpty()) {
                    IconButton(onClick = { goBack() }) { Text("←") }
                }
            }
        )

        when {
            state.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            !state.folderPicked -> EmptyState(
                message = "Insert the SD card, then pick its folder — the one containing manifest.json.",
                buttonLabel = "Select SD card folder",
                onClick = { pickFolder.launch(null) }
            )

            state.manifest == null -> EmptyState(
                message = "This folder doesn't look like a packaged SD card — no manifest.json found inside it.",
                buttonLabel = "Pick a different folder",
                onClick = { pickFolder.launch(null) }
            )

            !state.activated -> ActivateForm(
                groupName = state.manifest?.groupName ?: "this card",
                activating = state.activating,
                error = state.activateError,
                expiredNotice = if (state.expired)
                    "Your access to this card expired 1 year after it was issued. Ask your institute for a new access code to keep using it."
                else null,
                onActivate = { code, email -> viewModel.activate(code, email) }
            )

            hasCourseGrouping && selectedCourse == null -> {
                // Program → Courses view.
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(courseGroups, key = { it.courseId ?: it.hashCode().toString() }) { course ->
                        CourseRow(course = course, onClick = { selectCourse(course) })
                    }
                }
            }

            // Course → Chapter → ... → session-wise content, when this card
            // carries a real folder tree — mirrors the online Learn app's
            // FolderBrowseScreen, but as in-memory local state (no per-level
            // network fetch needed, the whole manifest's already loaded).
            hasFolderTree && (selectedCourse != null || !hasCourseGrouping) -> {
                val subfolders = allFolders.filter { it.parentId == currentFolderId }
                val allItems = state.manifest?.items.orEmpty()
                val here = allItems.filter { it.folderId == currentFolderId }
                val playable = here.filter { it.includedAsFile == true }
                val excluded = here.filter { it.includedAsFile != true }
                if (subfolders.isEmpty() && here.isEmpty()) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Nothing here yet") }
                } else {
                    LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(subfolders, key = { it.id ?: it.hashCode().toString() }) { folder ->
                            FolderRow(folder = folder, onClick = { folderStack = folderStack + folder })
                        }
                        items(playable, key = { it.id ?: it.hashCode().toString() }) { item ->
                            ContentRow(item = item, busy = decryptingId == item.id, onClick = { openItem(item) })
                        }
                        if (excluded.isNotEmpty()) {
                            item { Text("Needs an online sync", style = MaterialTheme.typography.titleSmall) }
                            items(excluded, key = { (it.id ?: it.hashCode().toString()) + "_x" }) { item ->
                                Card(modifier = Modifier.fillMaxWidth()) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(item.name ?: "(untitled)", style = MaterialTheme.typography.titleSmall)
                                        Text(item.reason ?: "Not available on this card", style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            else -> {
                // Flat fallback — a card packaged before the folder-tree
                // field existed (or one course-scoped grouping with no
                // folders at all). Every item in this scope, one list.
                val allItems = state.manifest?.items.orEmpty()
                val scoped = selectedCourse?.itemIds?.toSet()
                val playable = allItems.filter { it.includedAsFile == true && (scoped == null || scoped.contains(it.id)) }
                val excluded = allItems.filter { it.includedAsFile != true && (scoped == null || scoped.contains(it.id)) }
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(playable, key = { it.id ?: it.hashCode().toString() }) { item ->
                        ContentRow(item = item, busy = decryptingId == item.id, onClick = { openItem(item) })
                    }
                    if (excluded.isNotEmpty()) {
                        item { Text("Needs an online sync", style = MaterialTheme.typography.titleSmall) }
                        items(excluded, key = { (it.id ?: it.hashCode().toString()) + "_x" }) { item ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(item.name ?: "(untitled)", style = MaterialTheme.typography.titleSmall)
                                    Text(item.reason ?: "Not available on this card", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(message: String, buttonLabel: String, onClick: () -> Unit) {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Text(message, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
            Button(onClick = onClick, modifier = Modifier.padding(top = 16.dp)) { Text(buttonLabel) }
        }
    }
}

@Composable
private fun ActivateForm(
    groupName: String,
    activating: Boolean,
    error: String?,
    expiredNotice: String?,
    onActivate: (code: String, email: String) -> Unit
) {
    var code by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(24.dp)) {
        Text("Activate \"$groupName\"", style = MaterialTheme.typography.titleMedium)
        if (expiredNotice != null) {
            Text(
                expiredNotice,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        Text(
            "One-time only, and needs internet right now — after this, the card works with no connection at all.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = code,
            onValueChange = { code = it },
            label = { Text("Access code") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        if (error != null) {
            Text(error, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
        }
        Button(
            onClick = { onActivate(code, email) },
            enabled = !activating && code.isNotBlank() && email.isNotBlank(),
            modifier = Modifier.padding(top = 16.dp)
        ) {
            Text(if (activating) "Activating…" else "Activate")
        }
    }
}

// Program-level card, same visual weight as CourseCard on the online
// CourseListScreen — the whole point of this redesign is that a student
// recognizes this as "my courses," not a generic file browser.
@Composable
private fun CourseRow(course: SdCardCourseGroup, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Text(course.courseName ?: "(untitled course)", style = MaterialTheme.typography.titleMedium)
            val count = course.itemIds?.size ?: 0
            Text("$count item${if (count == 1) "" else "s"}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

// Chapter/session node — same 📁-prefixed styling as the online
// FolderBrowseScreen's subfolder rows, for visual consistency between the
// SD-card reader and the network browser.
@Composable
private fun FolderRow(folder: SdCardFolder, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Text(
            "📁  ${folder.name ?: "(untitled folder)"}",
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun ContentRow(item: SdCardManifestItem, busy: Boolean, onClick: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth().clickable(enabled = !busy, onClick = onClick)) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name ?: "(untitled)", style = MaterialTheme.typography.titleSmall)
                Text(item.type ?: "", style = MaterialTheme.typography.bodySmall)
            }
            if (busy) CircularProgressIndicator(modifier = Modifier.padding(start = 8.dp))
        }
    }
}
