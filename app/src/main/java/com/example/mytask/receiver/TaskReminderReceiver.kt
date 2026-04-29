package com.example.mytask.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.mytask.util.NotificationHelper

class TaskReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title = intent.getStringExtra("task_title") ?: "Task Reminder"
        val description = intent.getStringExtra("task_description") ?: "You have a task due soon!"
        
        val notificationHelper = NotificationHelper(context)
        notificationHelper.showNotification(title, description)
    }
}
