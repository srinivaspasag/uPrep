package `in`.uprep.app.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: DownloadEntity)

    @Query("SELECT * FROM downloads WHERE contentId = :contentId")
    fun observe(contentId: String): Flow<DownloadEntity?>

    @Query("SELECT * FROM downloads WHERE contentId = :contentId")
    suspend fun get(contentId: String): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE status = :status ORDER BY name")
    fun observeByStatus(status: DownloadStatus): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads ORDER BY name")
    fun observeAll(): Flow<List<DownloadEntity>>

    @Query("DELETE FROM downloads WHERE contentId = :contentId")
    suspend fun delete(contentId: String)
}
