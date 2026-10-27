package `in`.uprep.app.data.offline

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import `in`.uprep.app.data.net.NetworkConfig
import `in`.uprep.app.data.session.CookieStore
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import `in`.uprep.app.data.api.TestsApi
import `in`.uprep.app.data.net.AppCookieJar
import java.util.concurrent.TimeUnit

// Finds every PENDING_SYNC attempt and tries to submit it — the
// NetworkType.CONNECTED constraint means WorkManager won't even start this
// worker while offline, so "try" here is really "try now that we know
// there's a connection." Never marks an attempt FAILED outright: a failure
// here just leaves it PENDING_SYNC (same status, lastSyncError updated) so
// the next periodic run or app start retries it — no attempt is ever
// silently dropped.
class SyncAttemptWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    // Built standalone rather than through AppContainer — WorkManager
    // constructs this class itself (no DI hook into the running
    // Application's container instance), so it wires just enough of the
    // same OkHttp/Retrofit/cookie stack directly. Reuses the identical
    // CookieStore-backed AppCookieJar, so it rides the same uprep_auth
    // session the rest of the app is already logged in with.
    private val repository: OfflineTestRepository by lazy {
        val cookieJar = AppCookieJar(CookieStore(applicationContext))
        val okHttpClient = OkHttpClient.Builder()
            .cookieJar(cookieJar)
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        OfflineTestRepository(AppDatabase.get(applicationContext).offlineTestDao(), retrofit.create(TestsApi::class.java))
    }

    override suspend fun doWork(): Result {
        val pending = repository.getPendingSyncAttempts()
        var anyFailed = false
        for (attempt in pending) {
            when (repository.syncAttempt(attempt.localId)) {
                is SyncResult.Success -> {}
                is SyncResult.Failed -> anyFailed = true
            }
        }
        // Retry (with WorkManager's own backoff) rather than fail outright —
        // a still-offline device or a transient server error both look the
        // same from here, and both deserve another attempt later.
        return if (anyFailed) Result.retry() else Result.success()
    }

    companion object {
        private const val ONE_TIME_WORK_NAME = "sync-offline-attempts-once"
        private const val PERIODIC_WORK_NAME = "sync-offline-attempts-periodic"

        private val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Queued right after a local submit that couldn't reach the server
        // immediately (see OfflineTestRepository.finishAndSync) — runs as
        // soon as WorkManager sees a connection, no need to wait for the
        // periodic sweep below.
        fun enqueueOneTime(context: Context) {
            val request = OneTimeWorkRequestBuilder<SyncAttemptWorker>()
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniqueWork(ONE_TIME_WORK_NAME, ExistingWorkPolicy.KEEP, request)
        }

        // Safety net registered once on app start (see UPrepApplication) —
        // catches anything the one-time enqueue above might have missed
        // (app killed before it could fire, etc.).
        fun enqueuePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<SyncAttemptWorker>(30, TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(PERIODIC_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }
    }
}
