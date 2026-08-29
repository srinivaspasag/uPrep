package `in`.uprep.app.ui.sdcard

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import `in`.uprep.app.ui.player.DirectPlayer
import `in`.uprep.app.ui.player.PdfPager
import java.io.File

// Thin wrappers around the same DirectPlayer/PdfPager composables the
// network-download player screens use — the only difference for SD-card
// playback is that the file is already sitting decrypted in the cache dir
// (see SdCardRepository.decryptToCache), so there's no remote-URL/download-
// status juggling to do here at all.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SdCardVideoScreen(name: String, file: File) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(name) })
        DirectPlayer(file.toURI().toString())
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SdCardDocumentScreen(name: String, file: File) {
    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(title = { Text(name) })
        PdfPager(file)
    }
}
