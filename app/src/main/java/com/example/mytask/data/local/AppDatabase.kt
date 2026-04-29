package com.example.mytask.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.mytask.data.local.dao.SettingsDao
import com.example.mytask.data.local.dao.StatisticsDao
import com.example.mytask.data.local.dao.TaskDao
import com.example.mytask.data.local.dao.UserDao
import com.example.mytask.data.local.entity.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        UserEntity::class,
        CategoryEntity::class,
        TaskEntity::class,
        SubtaskEntity::class,
        UserSettingsEntity::class,
        StatisticsEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun userDao(): UserDao
    abstract fun statisticsDao(): StatisticsDao
    abstract fun settingsDao(): SettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "mytask_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        // Pre-populate categories
                        CoroutineScope(Dispatchers.IO).launch {
                            db.execSQL("INSERT INTO categories (id, name) VALUES (1, 'Priority Task')")
                            db.execSQL("INSERT INTO categories (id, name) VALUES (2, 'Daily Task')")
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
