package com.example.mytask.data.repository

import com.example.mytask.data.local.dao.SettingsDao
import com.example.mytask.data.local.dao.StatisticsDao
import com.example.mytask.data.local.dao.TaskDao
import com.example.mytask.data.local.entity.StatisticsEntity
import com.example.mytask.data.local.entity.SubtaskEntity
import com.example.mytask.data.local.entity.TaskEntity
import com.example.mytask.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

class TaskRepository(
    private val taskDao: TaskDao,
    private val statisticsDao: StatisticsDao,
    private val settingsDao: SettingsDao
) {
    
    fun getTasksByUserId(userId: Long): Flow<List<TaskEntity>> = taskDao.getTasksByUserId(userId)

    fun getSettings(userId: Long): Flow<UserSettingsEntity?> = settingsDao.getSettingsByUserId(userId)

    suspend fun insertTask(task: TaskEntity): Long {
        val taskId = taskDao.insertTask(task)
        statisticsDao.initializeStatistics(task.userId)
        statisticsDao.incrementTotalTasks(task.userId)
        return taskId
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskDao.deleteTask(task)
    }

    suspend fun updateTask(task: TaskEntity) {
        taskDao.updateTask(task)
    }

    suspend fun updateTaskStatus(task: TaskEntity, newStatus: String) {
        val progress = when (newStatus) {
            "finish" -> 100
            "progress" -> 50
            else -> 0
        }
        val isCompleted = newStatus == "finish"
        val updatedTask = task.copy(status = newStatus, progress = progress, isCompleted = isCompleted)
        taskDao.updateTask(updatedTask)
        
        // Update statistics if status changed to/from finish
        if (task.status != "finish" && newStatus == "finish") {
            statisticsDao.updateCompletedTasks(task.userId, 1)
        } else if (task.status == "finish" && newStatus != "finish") {
            statisticsDao.updateCompletedTasks(task.userId, -1)
        }
    }

    suspend fun insertSubtask(subtask: SubtaskEntity) {
        taskDao.insertSubtask(subtask)
        // Recalculate task progress after subtask insertion
        updateTaskProgressBasedOnSubtasks(subtask.taskId)
    }

    suspend fun deleteSubtask(subtask: SubtaskEntity) {
        taskDao.deleteSubtask(subtask)
        // Recalculate task progress after subtask deletion
        updateTaskProgressBasedOnSubtasks(subtask.taskId)
    }

    fun getSubtasks(taskId: Long): Flow<List<SubtaskEntity>> = taskDao.getSubtasksForTask(taskId)

    suspend fun updateSubtask(subtask: SubtaskEntity) {
        taskDao.updateSubtask(subtask)
        // Recalculate task progress after subtask update
        updateTaskProgressBasedOnSubtasks(subtask.taskId)
    }

    private suspend fun updateTaskProgressBasedOnSubtasks(taskId: Long) {
        val task = taskDao.getTaskById(taskId) ?: return
        val subtasks = taskDao.getSubtasksForTaskSync(taskId)
        
        if (subtasks.isEmpty()) return

        val completedCount = subtasks.count { it.isDone }
        val progress = (completedCount.toFloat() / subtasks.size * 100).toInt()
        val isCompleted = progress == 100
        
        val updatedStatus = when {
            progress == 100 -> "finish"
            progress > 0 -> "progress"
            else -> "pending"
        }
        
        val wasCompleted = task.isCompleted
        val updatedTask = task.copy(
            progress = progress,
            isCompleted = isCompleted,
            status = updatedStatus
        )
        
        taskDao.updateTask(updatedTask)

        // Update statistics if completion status changed
        if (!wasCompleted && isCompleted) {
            statisticsDao.updateCompletedTasks(task.userId, 1)
        } else if (wasCompleted && !isCompleted) {
            statisticsDao.updateCompletedTasks(task.userId, -1)
        }
    }

    suspend fun updateTaskCompletion(task: TaskEntity, isCompleted: Boolean) {
        val progress = if (isCompleted) 100 else 0
        val status = if (isCompleted) "finish" else "pending"
        val updatedTask = task.copy(progress = progress, isCompleted = isCompleted, status = status)
        taskDao.updateTask(updatedTask)
        
        statisticsDao.initializeStatistics(task.userId)
        // Only update if it actually changed to/from completed to avoid double counting
        if (!task.isCompleted && isCompleted) {
            statisticsDao.updateCompletedTasks(task.userId, 1)
        } else if (task.isCompleted && !isCompleted) {
            statisticsDao.updateCompletedTasks(task.userId, -1)
        }
    }

    fun getStatistics(userId: Long): Flow<StatisticsEntity?> = statisticsDao.getStatisticsByUserId(userId)
}
