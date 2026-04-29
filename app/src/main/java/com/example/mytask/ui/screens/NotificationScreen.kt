package com.example.mytask.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytask.ui.theme.*
import com.example.mytask.ui.viewmodel.TaskViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NotificationScreen(
    taskViewModel: TaskViewModel,
    onBack: () -> Unit
) {
    val tasks by taskViewModel.tasks.collectAsState()
    
    val deadlineNotifications = remember(tasks) {
        val sdf = SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault())
        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.time

        tasks.filter { it.status != "finish" && it.endDate != null }.mapNotNull { task ->
            try {
                val endDate = sdf.parse(task.endDate!!)
                if (endDate != null) {
                    val diff = endDate.time - now.time
                    val daysRemaining = (diff / (1000 * 60 * 60 * 24)).toInt()
                    
                    if (daysRemaining in 0..3) {
                        NotificationData(
                            title = "Deadline Approach!",
                            message = "Task \"${task.title}\" is due in $daysRemaining days (${task.endDate}).",
                            isUrgent = daysRemaining <= 1
                        )
                    } else if (daysRemaining < 0) {
                        NotificationData(
                            title = "Task Overdue",
                            message = "Task \"${task.title}\" was due on ${task.endDate}.",
                            isUrgent = true
                        )
                    } else null
                } else null
            } catch (e: Exception) {
                null
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .background(MyTaskLightGray.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                    .size(40.dp)
            ) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = MyTaskBlue)
            }
            Text(
                text = "Reminders",
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.width(40.dp))
        }

        if (deadlineNotifications.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Notifications, 
                        contentDescription = null, 
                        modifier = Modifier.size(64.dp), 
                        tint = Color.LightGray
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No reminders at the moment", color = Color.Gray)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(deadlineNotifications) { notification ->
                    NotificationItem(notification)
                }
            }
        }
    }
}

data class NotificationData(
    val title: String,
    val message: String,
    val isUrgent: Boolean
)

@Composable
fun NotificationItem(notification: NotificationData) {
    Surface(
        color = if (notification.isUrgent) Color(0xFFFFEBEE) else MyTaskLightGray.copy(alpha = 0.3f),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (notification.isUrgent) Icons.Default.Warning else Icons.Default.Notifications,
                contentDescription = null,
                tint = if (notification.isUrgent) Color.Red else MyTaskBlue,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = notification.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = if (notification.isUrgent) Color.Red else Color.Black
                )
                Text(
                    text = notification.message,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}
