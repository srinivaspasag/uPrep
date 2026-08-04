package `in`.uprep.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class DownloadStatus { QUEUED, DOWNLOADING, PAUSED, COMPLETE, FAILED }

// One row per downloadable content item (video/document). localPath points at
// the encrypted file on disk (see ContentDownloadWorker) once COMPLETE.
@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey val contentId: String,
    val type: String, // "VIDEO" | "DOCUMENT"
    val name: String,
    val remoteUrl: String,
    val localPath: String?,
    val status: DownloadStatus,
    val downloadedBytes: Long,
    val totalBytes: Long
)
