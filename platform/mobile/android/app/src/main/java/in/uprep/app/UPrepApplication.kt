package `in`.uprep.app

import android.app.Application
import `in`.uprep.app.data.net.AppContainer

class UPrepApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
