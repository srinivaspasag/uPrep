package `in`.uprep.app.data.api

import retrofit2.Response
import retrofit2.http.GET
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

interface LearnApi {
    @GET("/api/learn/courses")
    suspend fun myCourses(): Response<LearnCoursesResponse>

    @GET("/api/learn/courses")
    suspend fun browseFolder(@Query("folderId") folderId: String): Response<LearnCoursesResponse>
}
