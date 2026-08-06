package `in`.uprep.app

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import `in`.uprep.app.data.session.UserSession
import `in`.uprep.app.ui.courses.CourseListScreen
import `in`.uprep.app.ui.courses.CoursesViewModel
import `in`.uprep.app.ui.courses.FolderBrowseScreen
import `in`.uprep.app.ui.courses.FolderBrowseViewModel
import `in`.uprep.app.ui.downloads.DownloadsScreen
import `in`.uprep.app.ui.login.LoginScreen
import `in`.uprep.app.ui.login.LoginViewModel
import `in`.uprep.app.ui.player.DocumentViewerScreen
import `in`.uprep.app.ui.player.VideoPlayerScreen
import `in`.uprep.app.ui.theme.UPrepTheme
import `in`.uprep.app.ui.webview.WebViewFallbackScreen
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

// Full URLs (https://vimeo.com/12345, containing multiple internal "/")
// passed as nav-graph query args get silently truncated at an internal
// slash by AndroidX Navigation's route matching — found live: a video's
// url arrived at VideoPlayerScreen as bare "https://vimeo.com" with the
// video id lost, even though Uri.encode() was already applied. Base64
// (URL-safe alphabet, no "/" or "+") sidesteps the whole class of bug —
// the encoded token has no characters Navigation's route parser treats
// specially.
private fun b64encode(s: String): String = Base64.encodeToString(s.toByteArray(), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
private fun b64decode(s: String): String = String(Base64.decode(s, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING))

private object Routes {
    const val LOGIN = "login"
    const val COURSES = "courses"
    const val DOWNLOADS = "downloads"
    const val FOLDER = "folder/{folderId}"
    const val PLAYER = "player?contentId={contentId}&name={name}&url={url}&embedUrl={embedUrl}&provider={provider}"
    const val DOCUMENT = "document?contentId={contentId}&name={name}&url={url}"
    const val WEBVIEW = "webview?path={path}"

    fun folder(id: String) = "folder/$id"
    fun player(contentId: String, name: String, url: String?, embedUrl: String?, provider: String?) =
        "player?contentId=${Uri.encode(contentId)}&name=${Uri.encode(name)}" +
            "&url=${b64encode(url ?: "")}&embedUrl=${b64encode(embedUrl ?: "")}" +
            "&provider=${Uri.encode(provider ?: "")}"
    fun document(contentId: String, name: String, url: String) =
        "document?contentId=${Uri.encode(contentId)}&name=${Uri.encode(name)}&url=${b64encode(url)}"
    fun webview(path: String? = null) = "webview?path=${Uri.encode(path ?: "")}"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val container = (application as UPrepApplication).container

        setContent {
            UPrepTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val scope = rememberCoroutineScope()

                    // Clears the cached session/cookie and returns to login —
                    // the only way to switch accounts on the same device,
                    // since a saved session otherwise auto-resumes on every
                    // launch (see initialSession below).
                    val logout: () -> Unit = {
                        scope.launch {
                            container.sessionStore.clear()
                            container.cookieJar.clear()
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(navController.graph.findStartDestination().id) { inclusive = true }
                            }
                        }
                    }

                    // Resolve any existing session before deciding the start
                    // screen — null while we haven't checked yet, then either
                    // a UserSession or explicit "none". This is a pure local
                    // DataStore read, so it works with no connectivity too.
                    var initialSession by remember { mutableStateOf<UserSession?>(null) }
                    var resolved by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        initialSession = container.sessionStore.session.first()
                        resolved = true
                    }

                    if (!resolved) return@Surface

                    NavHost(
                        navController = navController,
                        startDestination = if (initialSession != null) routeFor(initialSession!!) else Routes.LOGIN
                    ) {
                        composable(Routes.LOGIN) {
                            LoginScreen(
                                viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                                    factory = LoginViewModel.Factory(
                                        applicationContext, container.authApi, container.sessionStore
                                    )
                                ),
                                onLoginSuccess = { session ->
                                    navController.navigate(routeFor(session)) {
                                        popUpTo(Routes.LOGIN) { inclusive = true }
                                    }
                                }
                            )
                        }

                        composable(Routes.COURSES) {
                            CourseListScreen(
                                viewModel = androidx.lifecycle.viewmodel.compose.viewModel(
                                    factory = CoursesViewModel.Factory(container.learnApi)
                                ),
                                onOpenCourse = { course -> navController.navigate(Routes.folder(course.id)) },
                                onOpenDownloads = { navController.navigate(Routes.DOWNLOADS) },
                                onLogout = logout
                            )
                        }

                        composable(Routes.DOWNLOADS) {
                            DownloadsScreen(downloadRepository = container.downloadRepository)
                        }

                        composable(
                            route = Routes.FOLDER,
                            arguments = listOf(navArgument("folderId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val folderId = backStackEntry.arguments?.getString("folderId") ?: return@composable
                            FolderBrowseScreen(
                                folderId = folderId,
                                resolveUrl = container::absoluteUrl,
                                downloadRepository = container.downloadRepository,
                                viewModelFactory = FolderBrowseViewModel.Factory(container.learnApi, folderId),
                                onOpenSubfolder = { id -> navController.navigate(Routes.folder(id)) },
                                onOpenVideo = { item ->
                                    navController.navigate(
                                        Routes.player(
                                            contentId = item.id,
                                            name = item.name,
                                            url = item.url?.let(container::absoluteUrl),
                                            embedUrl = item.embedUrl,
                                            provider = item.provider
                                        )
                                    )
                                },
                                onOpenDocument = { item ->
                                    val url = item.url?.let(container::absoluteUrl) ?: return@FolderBrowseScreen
                                    navController.navigate(Routes.document(item.id, item.name, url))
                                },
                                onOpenTest = { item -> navController.navigate(Routes.webview("/test/${item.id}")) }
                            )
                        }

                        composable(
                            route = Routes.PLAYER,
                            arguments = listOf(
                                navArgument("contentId") { type = NavType.StringType; defaultValue = "" },
                                navArgument("name") { type = NavType.StringType; defaultValue = "" },
                                navArgument("url") { type = NavType.StringType; defaultValue = "" },
                                navArgument("embedUrl") { type = NavType.StringType; defaultValue = "" },
                                navArgument("provider") { type = NavType.StringType; defaultValue = "" }
                            )
                        ) { backStackEntry ->
                            val args = backStackEntry.arguments
                            VideoPlayerScreen(
                                contentId = args?.getString("contentId").orEmpty(),
                                name = args?.getString("name").orEmpty(),
                                directUrl = args?.getString("url")?.let(::b64decode)?.takeIf { it.isNotEmpty() },
                                embedUrl = args?.getString("embedUrl")?.let(::b64decode)?.takeIf { it.isNotEmpty() },
                                provider = args?.getString("provider")?.takeIf { it.isNotEmpty() },
                                downloadRepository = container.downloadRepository
                            )
                        }

                        composable(
                            route = Routes.DOCUMENT,
                            arguments = listOf(
                                navArgument("contentId") { type = NavType.StringType; defaultValue = "" },
                                navArgument("name") { type = NavType.StringType; defaultValue = "" },
                                navArgument("url") { type = NavType.StringType; defaultValue = "" }
                            )
                        ) { backStackEntry ->
                            val args = backStackEntry.arguments
                            DocumentViewerScreen(
                                contentId = args?.getString("contentId").orEmpty(),
                                name = args?.getString("name").orEmpty(),
                                remoteUrl = args?.getString("url")?.let(::b64decode).orEmpty(),
                                downloadRepository = container.downloadRepository
                            )
                        }

                        composable(
                            route = Routes.WEBVIEW,
                            arguments = listOf(
                                navArgument("path") { type = NavType.StringType; defaultValue = "" }
                            )
                        ) { backStackEntry ->
                            val path = backStackEntry.arguments?.getString("path")?.takeIf { it.isNotEmpty() }
                            WebViewFallbackScreen(
                                path = path,
                                cookieValue = container.cookieStore.getCookie(),
                                onLogout = logout
                            )
                        }
                    }
                }
            }
        }
    }
}

// Staff (MANAGER/TEACHER/EDITOR/SALESPERSON) go to the WebView fallback and
// CMDS — native screens are student-only in this pass. Students go to the
// native course list.
private fun routeFor(session: UserSession): String =
    if (session.isStaff) "webview?path=${Uri.encode("/cmds")}" else Routes.COURSES
