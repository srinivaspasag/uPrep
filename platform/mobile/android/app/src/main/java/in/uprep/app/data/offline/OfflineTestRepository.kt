package `in`.uprep.app.data.offline

import android.content.Context
import com.google.gson.Gson
import `in`.uprep.app.data.api.SubmitAnswer
import `in`.uprep.app.data.api.SubmitRequest
import `in`.uprep.app.data.api.TestsApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

sealed class DownloadResult {
    object Success : DownloadResult()
    data class Failed(val message: String) : DownloadResult()
}

sealed class SyncResult {
    object Success : SyncResult()
    data class Failed(val message: String) : SyncResult()
}

// Everything the native offline test-taking screens need: downloading a
// test while online (GET /api/tests/{id}, cached verbatim — see
// TestsApi.kt's comment on why that response is already safe to store),
// recording answers locally during an attempt, and syncing a finished
// attempt back (POST /api/tests/{id}/submit) once connectivity allows —
// called both right after a live finish and, if that fails or the device
// was offline, later by SyncAttemptWorker. One network surface
// (TestsApi); everything else is local Room I/O, same shape as
// SdCardRepository's "one online call, everything else local" design.
class OfflineTestRepository(
    private val dao: OfflineTestDao,
    private val testsApi: TestsApi,
    // Only needed to schedule a retry (SyncAttemptWorker) when an immediate
    // sync attempt fails — null when this repository is itself constructed
    // from inside SyncAttemptWorker, which has no reason to re-enqueue
    // itself mid-run.
    private val appContext: Context? = null
) {
    private val gson = Gson()

    fun observeDownloadedTests(): Flow<List<CachedTest>> = dao.observeDownloadedTests()

    fun observePendingSyncCount(): Flow<Int> = dao.observePendingSyncCount()

    suspend fun downloadTest(testId: String): DownloadResult = withContext(Dispatchers.IO) {
        try {
            val resp = testsApi.getTest(testId)
            val body = resp.body()
            if (!resp.isSuccessful || body == null) {
                return@withContext DownloadResult.Failed("Couldn't download this test — try again once you're online.")
            }
            val test = CachedTest(
                testId = body.id,
                name = body.name,
                durationMin = body.durationMin,
                totalMarks = body.totalMarks,
                code = body.code,
                downloadedAt = System.currentTimeMillis()
            )
            val questions = body.questions.mapIndexed { index, q ->
                CachedQuestion(
                    id = q.id,
                    testId = body.id,
                    content = q.content,
                    type = q.type,
                    board = q.board,
                    orderIndex = index,
                    optionsJson = gson.toJson(q.options)
                )
            }
            dao.saveDownloadedTest(test, questions)
            DownloadResult.Success
        } catch (e: Exception) {
            DownloadResult.Failed("No internet right now — downloading a test needs one connected moment.")
        }
    }

    suspend fun getTestWithQuestions(testId: String): CachedTestWithQuestions? =
        withContext(Dispatchers.IO) { dao.getTestWithQuestions(testId) }

    fun optionsFor(question: CachedQuestion): List<String> =
        gson.fromJson(question.optionsJson, Array<String>::class.java).toList()

    // Resumes an already-in-progress local attempt for this test if one
    // exists (e.g. the app was killed mid-test), otherwise starts a fresh
    // one. There is no server-side "resume" to reconcile against — every
    // submit call creates a brand-new backend attempt (see submit/route.ts),
    // so everything before a successful sync only ever lives here.
    suspend fun startOrResumeAttempt(testId: String): Long = withContext(Dispatchers.IO) {
        val existing = dao.getPendingSyncAttempts() // cheap table scan; local db is tiny
        existing.firstOrNull { it.testId == testId }?.let { return@withContext it.localId }
        dao.insertAttempt(
            LocalAttempt(
                testId = testId,
                status = AttemptStatus.IN_PROGRESS,
                startedAt = System.currentTimeMillis(),
                endedAt = null,
                lastSyncError = null,
                remoteAttemptId = null
            )
        )
    }

    suspend fun recordAnswer(attemptLocalId: Long, qId: String, answerGiven: List<String>, timeTakenSeconds: Int, marked: Boolean) =
        withContext(Dispatchers.IO) {
            dao.upsertAnswer(
                LocalAnswer(
                    attemptLocalId = attemptLocalId,
                    qId = qId,
                    answerGivenJson = gson.toJson(answerGiven),
                    timeTakenSeconds = timeTakenSeconds,
                    marked = marked
                )
            )
        }

    suspend fun getAnswers(attemptLocalId: Long): List<LocalAnswer> =
        withContext(Dispatchers.IO) { dao.getAnswers(attemptLocalId) }

    // Marks the attempt finished locally and immediately tries to sync it —
    // succeeds silently if online, falls back to PENDING_SYNC (picked up
    // later by SyncAttemptWorker) if not. Called once, right when the
    // student taps Submit.
    suspend fun finishAndSync(attemptLocalId: Long): SyncResult = withContext(Dispatchers.IO) {
        dao.setEndedAt(attemptLocalId, System.currentTimeMillis())
        dao.updateAttemptStatus(attemptLocalId, AttemptStatus.PENDING_SYNC)
        val result = syncAttempt(attemptLocalId)
        if (result is SyncResult.Failed) {
            appContext?.let { SyncAttemptWorker.enqueueOneTime(it) }
        }
        result
    }

    // Idempotent to call repeatedly — a SYNCED attempt is a no-op, a
    // not-yet-ended one is skipped (only finishAndSync should ever put an
    // attempt into PENDING_SYNC in the first place). Used by both
    // finishAndSync above and SyncAttemptWorker's periodic sweep.
    suspend fun syncAttempt(attemptLocalId: Long): SyncResult = withContext(Dispatchers.IO) {
        val attempt = dao.getAttempt(attemptLocalId)
            ?: return@withContext SyncResult.Failed("Attempt not found locally")
        if (attempt.status == AttemptStatus.SYNCED) return@withContext SyncResult.Success
        if (attempt.endedAt == null) return@withContext SyncResult.Failed("Attempt not finished yet")

        val answers = dao.getAnswers(attemptLocalId)
        val payload = SubmitRequest(
            answers = answers.map {
                SubmitAnswer(
                    qId = it.qId,
                    answerGiven = gson.fromJson(it.answerGivenJson, Array<String>::class.java).toList(),
                    timeTaken = it.timeTakenSeconds
                )
            },
            offline = true,
            deviceStartedAt = attempt.startedAt,
            deviceEndedAt = attempt.endedAt
        )

        try {
            val resp = testsApi.submit(attempt.testId, payload)
            val body = resp.body()
            if (!resp.isSuccessful || body == null) {
                val msg = "Sync failed (HTTP ${resp.code()}) — will retry automatically."
                dao.updateAttemptStatus(attemptLocalId, AttemptStatus.PENDING_SYNC, msg)
                return@withContext SyncResult.Failed(msg)
            }
            dao.setRemoteAttemptId(attemptLocalId, body.attemptId)
            dao.updateAttemptStatus(attemptLocalId, AttemptStatus.SYNCED)
            SyncResult.Success
        } catch (e: Exception) {
            val msg = "No connection — will retry automatically once you're back online."
            dao.updateAttemptStatus(attemptLocalId, AttemptStatus.PENDING_SYNC, msg)
            SyncResult.Failed(msg)
        }
    }

    suspend fun getPendingSyncAttempts(): List<LocalAttempt> =
        withContext(Dispatchers.IO) { dao.getPendingSyncAttempts() }
}
