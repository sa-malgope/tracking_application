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

@Entity(tableName = "events")
data class AppEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val packageName: String,
    val screen: String?,
    val timestamp: Long
)

@Dao
interface EventDao {
    @Insert
    suspend fun insert(e: AppEvent)

    @Query("SELECT * FROM events ORDER BY timestamp DESC LIMIT 500")
    fun recent(): Flow<List<AppEvent>>

    @Query("DELETE FROM events")
    suspend fun clear()
}

@Database(entities = [AppEvent::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun dao(): EventDao

    companion object {
        @Volatile private var inst: AppDatabase? = null
        fun get(ctx: Context): AppDatabase = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(
                ctx.applicationContext, AppDatabase::class.java, "tracker.db"
            ).build().also { inst = it }
        }
    }
}
