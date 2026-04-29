package com.example.mytask.data.local.dao

import androidx.room.*
import com.example.mytask.data.local.entity.StatisticsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StatisticsDao {
    @Query("SELECT * FROM statistics WHERE user_id = :userId")
    fun getStatisticsByUserId(userId: Long): Flow<StatisticsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateStatistics(statistics: StatisticsEntity)

    @Query("UPDATE statistics SET total_tasks = total_tasks + 1 WHERE user_id = :userId")
    suspend fun incrementTotalTasks(userId: Long)

    @Query("UPDATE statistics SET completed_tasks = completed_tasks + :delta WHERE user_id = :userId")
    suspend fun updateCompletedTasks(userId: Long, delta: Int)
    
    @Query("INSERT OR IGNORE INTO statistics (user_id, total_tasks, completed_tasks, total_time_minutes) VALUES (:userId, 0, 0, 0)")
    suspend fun initializeStatistics(userId: Long)
}
