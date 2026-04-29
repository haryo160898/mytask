package com.example.mytask.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytask.data.local.entity.TaskEntity
import com.example.mytask.ui.theme.*
import com.example.mytask.ui.viewmodel.TaskViewModel

@Composable
fun TaskDetailScreen(
    taskId: Long,
    viewModel: TaskViewModel,
    onBack: () -> Unit,
    onEditTask: (Long) -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val subtasks by viewModel.getSubtasks(taskId).collectAsState(initial = emptyList())
    val task = remember(tasks, taskId) { tasks.find { it.id == taskId } }

    if (task == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Task not found")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MyTaskBlue)
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
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .size(40.dp)
            ) {
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = MyTaskBlue)
            }
            Text(
                text = "Task Detail",
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = { onEditTask(taskId) },
                modifier = Modifier
                    .background(Color.White, RoundedCornerShape(12.dp))
                    .size(40.dp)
            ) {
                Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MyTaskBlue)
            }
        }

        // Content Area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White, RoundedCornerShape(topStart = 40.dp, topEnd = 40.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp)
            ) {
                Text(
                    text = task.title,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = MyTaskBlue
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Surface(
                    color = if (task.categoryId == 1L) MyTaskBlue.copy(alpha = 0.1f) else Color.Green.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = if (task.categoryId == 1L) "Priority Task" else "Daily Task",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        color = if (task.categoryId == 1L) MyTaskBlue else Color(0xFF388E3C),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Status", fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val statusColor = when (task.status) {
                        "finish" -> Color(0xFF4CAF50)
                        "progress" -> Color(0xFFFF9800)
                        else -> Color.Gray
                    }
                    val statusIcon = when (task.status) {
                        "finish" -> Icons.Default.CheckCircle
                        "progress" -> Icons.Default.HourglassTop
                        else -> Icons.Default.AccessTime
                    }
                    
                    Icon(statusIcon, contentDescription = null, tint = statusColor, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = task.status.replaceFirstChar { it.uppercase() },
                        color = statusColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    InfoItem(
                        label = "Start Date",
                        value = task.startDate ?: "-",
                        icon = Icons.Default.CalendarMonth,
                        modifier = Modifier.weight(1f)
                    )
                    InfoItem(
                        label = "End Date",
                        value = task.endDate ?: "-",
                        icon = Icons.Default.CalendarMonth,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Description", fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = task.description ?: "No description provided.",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                Text(text = "Progress", fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LinearProgressIndicator(
                        progress = { 
                            if (subtasks.isNotEmpty()) {
                                subtasks.count { it.isDone }.toFloat() / subtasks.size
                            } else {
                                task.progress / 100f
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(CircleShape),
                        color = MyTaskBlue,
                        trackColor = MyTaskLightGray
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = if (subtasks.isNotEmpty()) {
                            "${(subtasks.count { it.isDone }.toFloat() / subtasks.size * 100).toInt()}%"
                        } else {
                            "${task.progress}%"
                        },
                        fontWeight = FontWeight.Bold,
                        color = MyTaskBlue
                    )
                }

                if (subtasks.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(text = "To do list", fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        subtasks.forEach { subtask ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(1.dp, MyTaskLightGray, RoundedCornerShape(12.dp))
                                    .clickable {
                                        viewModel.toggleSubtask(subtask, !subtask.isDone)
                                    }
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = subtask.title,
                                    color = if (subtask.isDone) Color.Gray else Color.Black,
                                    fontSize = 14.sp,
                                    fontWeight = if (subtask.isDone) FontWeight.Normal else FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                    style = if (subtask.isDone) androidx.compose.ui.text.TextStyle(textDecoration = androidx.compose.ui.text.style.TextDecoration.LineThrough) else androidx.compose.ui.text.TextStyle.Default
                                )
                                Icon(
                                    imageVector = if (subtask.isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (subtask.isDone) MyTaskBlue else Color.LightGray,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Manual status change for tasks without subtasks
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(text = "Update Status", fontWeight = FontWeight.Bold, color = Color.Black)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusButton(
                            text = "Pending",
                            isSelected = task.status == "pending",
                            icon = Icons.Default.AccessTime,
                            color = Color.Gray,
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.updateTaskStatus(task, "pending") }
                        )
                        StatusButton(
                            text = "In Progress",
                            isSelected = task.status == "progress",
                            icon = Icons.Default.HourglassTop,
                            color = Color(0xFFFF9800),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.updateTaskStatus(task, "progress") }
                        )
                        StatusButton(
                            text = "Finish",
                            isSelected = task.status == "finish",
                            icon = Icons.Default.CheckCircle,
                            color = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f),
                            onClick = { viewModel.updateTaskStatus(task, "finish") }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
                
                // Remove the old button since we have StatusButtons now
            }
        }
    }
}

@Composable
fun StatusButton(
    text: String,
    isSelected: Boolean,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color else color.copy(alpha = 0.1f),
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                tint = if (isSelected) Color.White else color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = text, 
                color = if (isSelected) Color.White else color, 
                fontWeight = FontWeight.Bold, 
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun InfoItem(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        Text(text = label, color = Color.Gray, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = MyTaskBlue, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
        }
    }
}
