package `in`.uprep.app.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

// Mirrors GET /api/tests/{id}'s response shape exactly (see
// app/api/tests/[id]/route.ts) — question content + options only, never
// correct answers (those live in a separate `answers` Mongo collection,
// never joined into this response). Safe to cache on-device as-is.
data class RemoteQuestion(
    val id: String,
    val content: String,
    val type: String,
    val options: List<String>,
    val board: String?
)

data class TestDetailResponse(
    val id: String,
    val name: String,
    val durationMin: Int,
    val totalMarks: Int,
    val code: String?,
    val resultVisibility: String?,
    val questions: List<RemoteQuestion>,
    val alreadyAttempted: Boolean
)

// Matches submit/route.ts's IncomingAnswer + the offline-sync fields added
// alongside it (offline/deviceStartedAt/deviceEndedAt) — same payload shape
// the web client already sends when live, just accumulated locally first.
data class SubmitAnswer(
    val qId: String,
    val answerGiven: List<String>,
    val timeTaken: Int
)

data class SubmitRequest(
    val answers: List<SubmitAnswer>,
    val offline: Boolean,
    val deviceStartedAt: Long,
    val deviceEndedAt: Long
)

data class PerQuestionResult(val qId: String, val result: String)

data class SubmitResponse(
    val graded: Boolean,
    val attemptId: String,
    val total: Int,
    val answered: Int,
    val judgeable: Int,
    val correct: Int,
    val ungraded: Int,
    val perQuestion: List<PerQuestionResult>,
    val failedQIds: List<String>
)

interface TestsApi {
    @GET("/api/tests/{id}")
    suspend fun getTest(@Path("id") id: String): Response<TestDetailResponse>

    @POST("/api/tests/{id}/submit")
    suspend fun submit(@Path("id") id: String, @Body body: SubmitRequest): Response<SubmitResponse>
}
