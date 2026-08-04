package `in`.uprep.app

import android.app.Application
import android.webkit.WebView
import `in`.uprep.app.data.net.AppContainer

class UPrepApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        // Lets chrome://inspect on a connected desktop Chrome see this app's
        // WebView content directly — real DOM/console/network state instead
        // of guessing from logcat. Harmless (only reachable via a USB
        // debugging session, same trust boundary as adb itself).
        WebView.setWebContentsDebuggingEnabled(true)
    }
}
