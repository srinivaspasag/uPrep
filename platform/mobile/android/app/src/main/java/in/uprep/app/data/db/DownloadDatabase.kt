package `in`.uprep.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [DownloadEntity::class], version = 1, exportSchema = false)
abstract class DownloadDatabase : RoomDatabase() {
    abstract fun downloadDao(): DownloadDao

    companion object {
        @Volatile private var instance: DownloadDatabase? = null

        fun get(context: Context): DownloadDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    DownloadDatabase::class.java,
                    "uprep_downloads.db"
                ).build().also { instance = it }
            }
    }
}
