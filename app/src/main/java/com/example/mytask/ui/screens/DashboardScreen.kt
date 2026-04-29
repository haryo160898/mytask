package com.example.mytask.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytask.ui.theme.MyTaskGray
import com.example.mytask.ui.theme.MyTaskBlue
import com.example.mytask.ui.theme.MyTaskLightGray
import com.example.mytask.ui.theme.MyTaskDarkBlue
import com.example.mytask.ui.viewmodel.TaskViewModel
import com.example.mytask.ui.viewmodel.AuthViewModel
import coil.compose.rememberAsyncImagePainter

import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: TaskViewModel,
    authViewModel: AuthViewModel,
    onAddTaskClick: () -> Unit,
    onProfileClick: () -> Unit,
    onNotificationClick: () -> Unit,
    onTaskClick: (Long) -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    // Load tasks when screen is first displayed
    LaunchedEffect(currentUser?.id) {
        currentUser?.let {
            viewModel.loadTasks(userId = it.id)
        }
    }

    val priorityTasks = remember(tasks) { tasks.filter { it.categoryId == 1L } }
    val dailyTasks = remember(tasks) { tasks.filter { it.categoryId == 2L } }

    val currentTime = remember {
        val sdf = SimpleDateFormat("EEEE, MMM dd yyyy", Locale.getDefault())
        sdf.format(Date())
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.White,
        topBar = {
            Surface(
                modifier = Modifier.fillMaxWidth().statusBarsPadding(),
                color = Color.White,
                shadowElevation = 0.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = currentTime,
                            color = MyTaskGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Hello, ${currentUser?.name ?: "User"}!",
                            color = Color.Black,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            onClick = onNotificationClick,
                            shape = CircleShape,
                            color = MyTaskBlue.copy(alpha = 0.1f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = "Notifications",
                                    tint = MyTaskBlue,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        
                        Surface(
                            onClick = onProfileClick,
                            shape = CircleShape,
                            color = MyTaskLightGray.copy(alpha = 0.5f),
                            modifier = Modifier.size(44.dp)
                        ) {
                            if (currentUser?.avatar != null) {
                                Image(
                                    painter = rememberAsyncImagePainter(currentUser?.avatar),
                                    contentDescription = "Profile",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            } else {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Profile",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddTaskClick,
                modifier = Modifier
                    .padding(bottom = 16.dp, end = 8.dp)
                    .navigationBarsPadding(),
                containerColor = MyTaskBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Task",
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Priority Tasks Section
            Text(
                text = "My Priority Task",
                modifier = Modifier.padding(horizontal = 24.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (priorityTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .padding(horizontal = 24.dp)
                        .background(MyTaskLightGray.copy(alpha = 0.2f), RoundedCornerShape(24.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No priority tasks yet",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(priorityTasks) { task ->
                        val daysLeftText = remember(task.endDate) {
                            if (task.endDate == null) "No Date"
                            else {
                                try {
                                    val sdf = SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault())
                                    val endDate = sdf.parse(task.endDate)
                                    val now = Calendar.getInstance().apply {
                                        set(Calendar.HOUR_OF_DAY, 0)
                                        set(Calendar.MINUTE, 0)
                                        set(Calendar.SECOND, 0)
                                        set(Calendar.MILLISECOND, 0)
                                    }.time
                                    
                                    if (endDate != null) {
                                        val diff = endDate.time - now.time
                                        val days = (diff / (1000 * 60 * 60 * 24)).toInt()
                                        when {
                                            days < 0 -> "Overdue"
                                            days == 0 -> "Today"
                                            days == 1 -> "Tomorrow"
                                            else -> "$days Days"
                                        }
                                    } else "Pending"
                                } catch (e: Exception) {
                                    "Pending"
                                }
                            }
                        }
                        PriorityTaskCard(
                            title = task.title,
                            progress = task.progress / 100f,
                            daysLeft = daysLeftText,
                            color = MyTaskBlue,
                            onClick = { onTaskClick(task.id) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Daily Tasks Section
            Text(
                text = "Daily Task",
                modifier = Modifier.padding(horizontal = 24.dp),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (dailyTasks.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No daily tasks yet",
                        color = Color.Gray,
                        fontSize = 14.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(dailyTasks) { task ->
                        DailyTaskItem(
                            title = task.title,
                            progress = task.progress,
                            dueDate = task.endDate ?: "No Date",
                            onClick = { onTaskClick(task.id) }
                        )
                    }
                }
            }
        }
    }


}

@Composable
fun PriorityTaskCard(title: String, progress: Float, daysLeft: String, color: Color, onClick: () -> Unit = {}) {
    Card(
        modifier = Modifier
            .width(160.dp)
            .height(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = color)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = daysLeft,
                        color = Color.White,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
            
            Column {
                Text(
                    text = title,
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Progress",
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.3f),
                    strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        color = Color.White,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DailyTaskItem(
    title: String,
    progress: Int,
    dueDate: String,
    onClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, MyTaskLightGray.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .background(MyTaskBlue.copy(alpha = 0.1f), RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Assignment,
                    contentDescription = null,
                    tint = MyTaskBlue,
                    modifier = Modifier.size(26.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    color = Color.Black,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MyTaskGray,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Due: $dueDate",
                        color = MyTaskGray,
                        fontSize = 12.sp
                    )
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "$progress%",
                    color = MyTaskBlue,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { progress / 100f },
                    modifier = Modifier
                        .width(60.dp)
                        .height(6.dp)
                        .clip(CircleShape),
                    color = MyTaskBlue,
                    trackColor = MyTaskLightGray
                )
            }
        }
    }
}
