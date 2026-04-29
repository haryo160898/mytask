package com.example.mytask.data.local.dao

import androidx.room.*
import com.example.mytask.data.local.entity.TaskEntity
import com.example.mytask.data.local.entity.SubtaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE user_id = :userId ORDER BY created_at DESC")
    fun getTasksByUserId(userId: Long): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE user_id = :userId AND category_id = (SELECT id FROM categories WHERE name = :categoryName)")
    fun getTasksByCategory(userId: Long, categoryName: String): Flow<List<TaskEntity>>

    // Subtasks
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubtask(subtask: SubtaskEntity)

    @Update
    suspend fun updateSubtask(subtask: SubtaskEntity)

    @Delete
    suspend fun deleteSubtask(subtask: SubtaskEntity)

    @Query("SELECT * FROM subtasks WHERE task_id = :taskId")
    fun getSubtasksForTask(taskId: Long): Flow<List<SubtaskEntity>>

    @Query("SELECT * FROM subtasks WHERE task_id = :taskId")
    suspend fun getSubtasksForTaskSync(taskId: Long): List<SubtaskEntity>
}
