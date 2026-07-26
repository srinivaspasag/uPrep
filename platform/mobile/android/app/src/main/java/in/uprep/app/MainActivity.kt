package `in`.uprep.app

import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
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
import `in`.uprep.app.ui.login.LoginScreen
import `in`.uprep.app.ui.login.LoginViewModel
import `in`.uprep.app.ui.player.VideoPlayerScreen
import `in`.uprep.app.ui.theme.UPrepTheme
import `in`.uprep.app.ui.webview.WebViewFallbackScreen
import kotlinx.coroutines.flow.first

private object Routes {
    const val LOGIN = "login"
    const val COURSES = "courses"
    const val FOLDER = "folder/{folderId}"
    const val PLAYER = "player?name={name}&url={url}&embedUrl={embedUrl}"
    const val WEBVIEW = "webview?path={path}"

    fun folder(id: String) = "folder/$id"
    fun player(name: String, url: String?, embedUrl: String?) =
        "player?name=${Uri.encode(name)}&url=${Uri.encode(url ?: "")}&embedUrl=${Uri.encode(embedUrl ?: "")}"
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

                    // Resolve any existing session before deciding the start
                    // screen — null while we haven't checked yet, then either
                    // a UserSession or explicit "none".
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
                                    factory = LoginViewModel.Factory(container.authApi, container.sessionStore)
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
                                onOpenCourse = { course -> navController.navigate(Routes.folder(course.id)) }
                            )
                        }

                        composable(
                            route = Routes.FOLDER,
                            arguments = listOf(navArgument("folderId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val folderId = backStackEntry.arguments?.getString("folderId") ?: return@composable
                            FolderBrowseScreen(
                                folderId = folderId,
                                resolveUrl = container::absoluteUrl,
                                viewModelFactory = FolderBrowseViewModel.Factory(container.learnApi, folderId),
                                onOpenSubfolder = { id -> navController.navigate(Routes.folder(id)) },
                                onOpenVideo = { item ->
                                    navController.navigate(
                                        Routes.player(
                                            name = item.name,
                                            url = item.url?.let(container::absoluteUrl),
                                            embedUrl = item.embedUrl
                                        )
                                    )
                                },
                                onOpenTest = { item -> navController.navigate(Routes.webview("/test/${item.id}")) }
                            )
                        }

                        composable(
                            route = Routes.PLAYER,
                            arguments = listOf(
                                navArgument("name") { type = NavType.StringType; defaultValue = "" },
                                navArgument("url") { type = NavType.StringType; defaultValue = "" },
                                navArgument("embedUrl") { type = NavType.StringType; defaultValue = "" }
                            )
                        ) { backStackEntry ->
                            val name = backStackEntry.arguments?.getString("name").orEmpty()
                            val url = backStackEntry.arguments?.getString("url")?.takeIf { it.isNotEmpty() }
                            val embedUrl = backStackEntry.arguments?.getString("embedUrl")?.takeIf { it.isNotEmpty() }
                            VideoPlayerScreen(name = name, directUrl = url, embedUrl = embedUrl)
                        }

                        composable(
                            route = Routes.WEBVIEW,
                            arguments = listOf(
                                navArgument("path") { type = NavType.StringType; defaultValue = "" }
                            )
                        ) { backStackEntry ->
                            val path = backStackEntry.arguments?.getString("path")?.takeIf { it.isNotEmpty() }
                            WebViewFallbackScreen(path = path, cookieValue = container.cookieStore.getCookie())
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
