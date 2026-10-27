package `in`.uprep.app.data.offline

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import kotlinx.coroutines.flow.Flow

class Converters {
    @TypeConverter
    fun fromAttemptStatus(s: AttemptStatus): String = s.name

    @TypeConverter
    fun toAttemptStatus(s: String): AttemptStatus = AttemptStatus.valueOf(s)
}

data class CachedTestWithQuestions(
    val test: CachedTest,
    val questions: List<CachedQuestion>
)

@Dao
@TypeConverters(Converters::class)
interface OfflineTestDao {

    // Re-downloading a test (e.g. content updated) replaces its cached copy
    // wholesale — questions are deleted and reinserted rather than diffed,
    // since the whole download is already one atomic network response.
    @Transaction
    suspend fun saveDownloadedTest(test: CachedTest, questions: List<CachedQuestion>) {
        insertTest(test)
        deleteQuestionsForTest(test.testId)
        insertQuestions(questions)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTest(test: CachedTest)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<CachedQuestion>)

    @Query("DELETE FROM cached_questions WHERE testId = :testId")
    suspend fun deleteQuestionsForTest(testId: String)

    @Query("SELECT * FROM cached_tests ORDER BY downloadedAt DESC")
    fun observeDownloadedTests(): Flow<List<CachedTest>>

    @Query("SELECT * FROM cached_tests WHERE testId = :testId")
    suspend fun getTest(testId: String): CachedTest?

    @Query("SELECT * FROM cached_questions WHERE testId = :testId ORDER BY orderIndex ASC")
    suspend fun getQuestions(testId: String): List<CachedQuestion>

    @Transaction
    suspend fun getTestWithQuestions(testId: String): CachedTestWithQuestions? {
        val test = getTest(testId) ?: return null
        return CachedTestWithQuestions(test, getQuestions(testId))
    }

    @Insert
    suspend fun insertAttempt(attempt: LocalAttempt): Long

    @Query("UPDATE local_attempts SET status = :status, lastSyncError = :error WHERE localId = :localId")
    suspend fun updateAttemptStatus(localId: Long, status: AttemptStatus, error: String? = null)

    @Query("UPDATE local_attempts SET remoteAttemptId = :remoteAttemptId WHERE localId = :localId")
    suspend fun setRemoteAttemptId(localId: Long, remoteAttemptId: String)

    @Query("UPDATE local_attempts SET endedAt = :endedAt WHERE localId = :localId")
    suspend fun setEndedAt(localId: Long, endedAt: Long)

    @Query("SELECT * FROM local_attempts WHERE localId = :localId")
    suspend fun getAttempt(localId: Long): LocalAttempt?

    @Query("SELECT * FROM local_attempts WHERE status = 'PENDING_SYNC'")
    suspend fun getPendingSyncAttempts(): List<LocalAttempt>

    @Query("SELECT COUNT(*) FROM local_attempts WHERE status = 'PENDING_SYNC'")
    fun observePendingSyncCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAnswer(answer: LocalAnswer)

    @Query("SELECT * FROM local_answers WHERE attemptLocalId = :attemptLocalId")
    suspend fun getAnswers(attemptLocalId: Long): List<LocalAnswer>
}
