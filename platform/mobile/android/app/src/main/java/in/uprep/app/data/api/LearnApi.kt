package `in`.uprep.app.data.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

data class CourseSummary(val id: String, val name: String, val chapterCount: Int, val folderCount: Int)
data class FolderInfo(val id: String, val name: String, val parentId: String?)
data class SubfolderInfo(val id: String, val name: String, val type: String)

// Which Program (e.g. "JEE XI") granted a set of courses — bug found live:
// a student assigned to a Program saw its courses (Physics, Chem, Math)
// with no indication of the Program itself anywhere in the app.
data class ProgramGroup(val id: String, val name: String, val courseIds: List<String>)

// type: VIDEO | DOCUMENT | TEST. url/embedUrl are relative paths for uploaded
// files (e.g. "/uploads/xxx.mp4") — must be prefixed with NetworkConfig.BASE_URL
// before use. embedUrl+provider are only set for YouTube/Vimeo videos added by
// URL (see platform/web/lib/video.ts) — those play via the WebView fallback,
// not ExoPlayer.
data class ContentItem(
    val id: String,
    val name: String,
    val type: String,
    val url: String?,
    val embedUrl: String?,
    val provider: String?
)

// One endpoint, two shapes depending on whether folderId was passed — course
// list fields are null on a folder-browse call and vice versa. Matches
// platform/web/app/api/learn/courses/route.ts exactly.
data class LearnCoursesResponse(
    val courses: List<CourseSummary>?,
    val staff: Boolean?,
    val programGroups: List<ProgramGroup>?,
    val folder: FolderInfo?,
    val courseRootId: String?,
    val subfolders: List<SubfolderInfo>?,
    val items: List<ContentItem>?,
    val error: String?
)

// --- Doubts ("Ask Aira") — matches app/api/learn/doubts/**/route.ts exactly ---
data class DoubtSummary(
    val id: String,
    val name: String,
    val content: String,
    val subject: String?,
    val answerCount: Int,
    val state: String,
    val timeCreated: Long
)
data class DoubtsListResponse(val items: List<DoubtSummary> = emptyList(), val error: String? = null)

data class CreateDoubtBody(val name: String, val content: String, val subject: String? = null, val mode: String = "guided")
data class CreateDoubtResponse(val id: String?, val name: String?, val error: String? = null)

data class RelatedContentItem(
    val id: String, val type: String, val name: String,
    val url: String?, val embedUrl: String?, val provider: String?
)
data class DoubtDetail(
    val id: String, val name: String, val content: String, val userId: String?, val userName: String,
    val subject: String?, val upVotes: Int, val views: Int, val state: String, val timeCreated: Long
)
data class AnswerStep(val title: String, val body: String)
data class DoubtAnswer(
    val id: String, val content: String, val userId: String?, val userName: String,
    val timeCreated: Long, val isAi: Boolean, val steps: List<AnswerStep>?
)
data class DoubtDetailResponse(
    val doubt: DoubtDetail? = null,
    val answers: List<DoubtAnswer> = emptyList(),
    val relatedContent: List<RelatedContentItem> = emptyList(),
    val aiPending: Boolean = false,
    val error: String? = null
)

data class PostAnswerBody(val content: String)
data class PostAnswerResponse(val id: String?, val content: String?, val userName: String?, val timeCreated: Long?, val error: String? = null)

data class AiAnswerResponse(
    val id: String?, val content: String?, val steps: List<AnswerStep>?, val userName: String?,
    val timeCreated: Long?, val confidence: String?, val pending: Boolean?, val error: String? = null
)

// --- Analytics — matches app/api/learn/analytics/route.ts exactly ---
data class TestResult(val entityId: String, val name: String, val score: Int, val totalMarks: Int, val attemptedAt: Long)
data class TrendPoint(val date: Long, val pct: Int)
data class SubjectAccuracy(val name: String, val accuracy: Int, val total: Int)
data class TypeAccuracy(val type: String, val accuracy: Int, val total: Int)
data class AnalyticsSummary(val testsAttempted: Int, val avgScore: Double, val accuracy: Int, val questionsAnswered: Int)
data class AnalyticsResponse(
    val results: List<TestResult> = emptyList(),
    val trend: List<TrendPoint> = emptyList(),
    val subjects: List<SubjectAccuracy> = emptyList(),
    val types: List<TypeAccuracy> = emptyList(),
    val summary: AnalyticsSummary? = null,
    val error: String? = null
)

// --- Recent Activity — matches app/api/learn/activity/route.ts exactly ---
data class ActivityFeedItem(val type: String, val title: String, val detail: String?, val at: Long)
data class ActivityResponse(val feed: List<ActivityFeedItem> = emptyList(), val error: String? = null)

interface LearnApi {
    @GET("/api/learn/courses")
    suspend fun myCourses(): Response<LearnCoursesResponse>

    @GET("/api/learn/courses")
    suspend fun browseFolder(@Query("folderId") folderId: String): Response<LearnCoursesResponse>

    @GET("/api/learn/doubts")
    suspend fun myDoubts(@Query("state") state: String = ""): Response<DoubtsListResponse>

    @POST("/api/learn/doubts")
    suspend fun createDoubt(@Body body: CreateDoubtBody): Response<CreateDoubtResponse>

    @GET("/api/learn/doubts/{id}")
    suspend fun doubtDetail(@Path("id") id: String): Response<DoubtDetailResponse>

    @POST("/api/learn/doubts/{id}")
    suspend fun postAnswer(@Path("id") id: String, @Body body: PostAnswerBody): Response<PostAnswerResponse>

    @POST("/api/learn/doubts/{id}/ai-answer")
    suspend fun askAira(@Path("id") id: String): Response<AiAnswerResponse>

    @GET("/api/learn/analytics")
    suspend fun analytics(): Response<AnalyticsResponse>

    @GET("/api/learn/activity")
    suspend fun activity(): Response<ActivityResponse>
}
