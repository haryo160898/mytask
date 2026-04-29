package com.example.mytask.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mytask.data.local.entity.TaskEntity
import com.example.mytask.data.repository.TaskRepository
import com.example.mytask.data.repository.UserRepository
import com.example.mytask.util.NotificationHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class TaskViewModel(
    val repository: TaskRepository,
    private val userRepository: UserRepository,
    private val context: Context
) : ViewModel() {

    private val notificationHelper = NotificationHelper(context)

    private val _tasks = MutableStateFlow<List<TaskEntity>>(emptyList())
    val tasks: StateFlow<List<TaskEntity>> = _tasks.asStateFlow()

    private val _statistics = MutableStateFlow<com.example.mytask.data.local.entity.StatisticsEntity?>(null)
    val statistics: StateFlow<com.example.mytask.data.local.entity.StatisticsEntity?> = _statistics.asStateFlow()

    private var loadJob: Job? = null

    fun loadTasks(userId: Long) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            launch {
                repository.getTasksByUserId(userId).collect {
                    _tasks.value = it
                }
            }
            launch {
                repository.getStatistics(userId).collect {
                    _statistics.value = it
                }
            }
        }
    }

    fun addTask(
        userId: Long,
        title: String,
        description: String,
        category: String,
        startDate: String,
        endDate: String,
        subtasks: List<String> = emptyList()
    ) {
        viewModelScope.launch {
            val taskId = repository.insertTask(TaskEntity(
                userId = userId,
                categoryId = if (category == "Priority Task") 1L else 2L,
                title = title,
                description = description,
                startDate = startDate,
                endDate = endDate,
                createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                updatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            ))
            
            // Schedule notification
            val settings = userRepository.getSettings(userId).firstOrNull()
            if (settings?.highPriorityNotification == true || settings == null) {
                notificationHelper.scheduleTaskReminder(taskId, title, description, endDate)
            }

            // Insert Subtasks
            subtasks.forEach { subtaskTitle ->
                if (subtaskTitle.isNotBlank()) {
                    repository.insertSubtask(com.example.mytask.data.local.entity.SubtaskEntity(
                        taskId = taskId,
                        title = subtaskTitle,
                        createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                    ))
                }
            }
        }
    }

    fun updateTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.updateTask(task)
            
            // Update notification
            val settings = userRepository.getSettings(task.userId).firstOrNull()
            if (settings?.highPriorityNotification == true || settings == null) {
                notificationHelper.scheduleTaskReminder(
                    task.id, 
                    task.title, 
                    task.description ?: "", 
                    task.endDate ?: ""
                )
            }
        }
    }

    fun deleteTask(task: TaskEntity) {
        viewModelScope.launch {
            repository.deleteTask(task)
            notificationHelper.cancelTaskReminder(task.id)
        }
    }

    fun toggleTaskCompletion(task: TaskEntity, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateTaskCompletion(task, isCompleted)
        }
    }

    fun updateTaskStatus(task: TaskEntity, status: String) {
        viewModelScope.launch {
            repository.updateTaskStatus(task, status)
        }
    }

    fun getSubtasks(taskId: Long): Flow<List<com.example.mytask.data.local.entity.SubtaskEntity>> {
        return repository.getSubtasks(taskId)
    }

    fun toggleSubtask(subtask: com.example.mytask.data.local.entity.SubtaskEntity, isDone: Boolean) {
        viewModelScope.launch {
            repository.updateSubtask(subtask.copy(isDone = isDone))
        }
    }

    fun addSubtask(taskId: Long, title: String) {
        viewModelScope.launch {
            if (title.isNotBlank()) {
                repository.insertSubtask(com.example.mytask.data.local.entity.SubtaskEntity(
                    taskId = taskId,
                    title = title,
                    createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                ))
            }
        }
    }

    fun deleteSubtask(subtask: com.example.mytask.data.local.entity.SubtaskEntity) {
        viewModelScope.launch {
            repository.deleteSubtask(subtask)
        }
    }
}
