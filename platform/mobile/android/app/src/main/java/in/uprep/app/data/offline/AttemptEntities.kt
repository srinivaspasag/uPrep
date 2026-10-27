package `in`.uprep.app.data.offline

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AttemptStatus {
    IN_PROGRESS,
    PENDING_SYNC,
    SYNCED,
    FAILED
}

// One attempt at a downloaded test. `remoteAttemptId` is only ever set after
// a successful sync — POST /api/tests/{id}/submit creates a brand-new
// backend attempt on every call (see submit/route.ts), so there is nothing
// to "resume" server-side; everything before a successful sync lives only
// here.
@Entity(tableName = "local_attempts")
data class LocalAttempt(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val testId: String,
    val status: AttemptStatus,
    val startedAt: Long,
    val endedAt: Long?,
    val lastSyncError: String?,
    val remoteAttemptId: String?
)

// One row per question answered (or visited) within an attempt —
// `answerGivenJson` is the same `string[]` shape the web client already
// sends to /submit (option index as a string; empty = unanswered).
// `timeTakenSeconds` mirrors the web client's qTimeRef accumulation
// (app/test/[id]/page.tsx), ported in OfflineTestRepository.
@Entity(tableName = "local_answers", primaryKeys = ["attemptLocalId", "qId"])
data class LocalAnswer(
    val attemptLocalId: Long,
    val qId: String,
    val answerGivenJson: String,
    val timeTakenSeconds: Int,
    val marked: Boolean
)
