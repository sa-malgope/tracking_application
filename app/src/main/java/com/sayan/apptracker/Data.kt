package com.sayan.apptracker

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

/** One visit to one app: when it started, and when it ended (null = still open). */
@Entity(tableName = "sessions")
data class AppSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val startTime: Long,
    val endTime: Long? = null
)

@Dao
interface SessionDao {
    @Insert
    suspend fun insert(s: AppSession): Long

    @Query("UPDATE sessions SET endTime = :end WHERE packageName = :pkg AND startTime = :start AND endTime IS NULL")
    suspend fun close(pkg: String, start: Long, end: Long)

    @Query("SELECT * FROM sessions WHERE endTime IS NULL ORDER BY startTime DESC LIMIT 1")
    suspend fun openSession(): AppSession?

    @Query("SELECT * FROM sessions ORDER BY startTime DESC LIMIT 500")
    fun recent(): Flow<List<AppSession>>

    @Query("SELECT * FROM sessions WHERE startTime >= :since ORDER BY startTime DESC")
    fun since(since: Long): Flow<List<AppSession>>

    @Query("DELETE FROM sessions")
    suspend fun clear()
}

@Database(entities = [AppSession::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): SessionDao

    companion object {
        @Volatile private var inst: AppDatabase? = null
        fun get(ctx: Context): AppDatabase = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(
                ctx.applicationContext, AppDatabase::class.java, "tracker.db"
            ).build().also { inst = it }
        }
    }
}
