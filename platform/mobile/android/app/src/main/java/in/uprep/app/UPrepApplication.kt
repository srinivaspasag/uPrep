package `in`.uprep.app

import android.app.Application
import android.webkit.WebView
import `in`.uprep.app.data.net.AppContainer
import `in`.uprep.app.data.offline.SyncAttemptWorker

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
        // Safety net for offline test attempts stuck PENDING_SYNC — the
        // normal path is an immediate sync right after submit (see
        // OfflineTestRepository.finishAndSync), this just guarantees nothing
        // is ever permanently missed (e.g. app killed before that could run).
        SyncAttemptWorker.enqueuePeriodic(this)
    }
}
