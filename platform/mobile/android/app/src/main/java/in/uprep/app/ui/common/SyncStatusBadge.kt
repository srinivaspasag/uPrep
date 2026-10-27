package `in`.uprep.app.ui.common

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

// Small, visible-but-unobtrusive banner shown only on the offline tests list
// — not a global nav-bar indicator. Just enough for a student to trust that
// a finished offline attempt hasn't silently vanished: it's waiting for a
// connection, SyncAttemptWorker will pick it up automatically.
@Composable
fun SyncStatusBadge(pendingCount: Int) {
    if (pendingCount <= 0) return
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.secondaryContainer
    ) {
        Text(
            text = if (pendingCount == 1)
                "1 test result waiting to sync — will upload automatically once you're online."
            else
                "$pendingCount test results waiting to sync — will upload automatically once you're online.",
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}
