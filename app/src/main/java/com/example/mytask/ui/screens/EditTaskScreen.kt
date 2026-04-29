package com.example.mytask.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import androidx.lifecycle.viewModelScope
import com.example.mytask.ui.theme.MyTaskBlue
import com.example.mytask.ui.theme.MyTaskLightBlue
import com.example.mytask.ui.theme.MyTaskLightGray
import com.example.mytask.ui.viewmodel.TaskViewModel
import com.example.mytask.data.local.entity.TaskEntity
import com.example.mytask.data.local.entity.SubtaskEntity
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditTaskScreen(
    taskId: Long,
    viewModel: TaskViewModel,
    onBack: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsState()
    val task = tasks.find { it.id == taskId }
    
    val subtasksFlow = remember(taskId) { viewModel.getSubtasks(taskId) }
    val subtasks by subtasksFlow.collectAsState(initial = emptyList())

    if (task == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MyTaskBlue)
        }
        return
    }

    var title by remember { mutableStateOf(task.title) }
    var description by remember { mutableStateOf(task.description ?: "") }
    var selectedCategory by remember { mutableStateOf(if (task.categoryId == 1L) "Priority Task" else "Daily Task") }
    
    val dateFormatter = remember { SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault()) }
    var startDate by remember { mutableStateOf(task.startDate ?: dateFormatter.format(Date())) }
    var endDate by remember { mutableStateOf(task.endDate ?: dateFormatter.format(Date())) }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    var currentSubtask by remember { mutableStateOf("") }
    
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        startDate = dateFormatter.format(Date(it))
                    }
                    showStartDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        endDate = dateFormatter.format(Date(it))
                    }
                    showEndDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
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
                Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Back", tint = MyTaskBlue, modifier = Modifier.size(20.dp))
            }
            Text(
                text = "Edit Task",
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = {
                    viewModel.deleteTask(task)
                    onBack()
                },
                modifier = Modifier
                    .background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                    .size(40.dp)
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White, modifier = Modifier.size(20.dp))
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
                Row(modifier = Modifier.fillMaxWidth()) {
                    DateInputField(
                        label = "Start", 
                        date = startDate, 
                        modifier = Modifier.weight(1f).clickable { showStartDatePicker = true },
                        backgroundColor = MyTaskLightBlue.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    DateInputField(
                        label = "Ends", 
                        date = endDate, 
                        modifier = Modifier.weight(1f).clickable { showEndDatePicker = true },
                        backgroundColor = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Title", color = MyTaskBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Task title...", color = Color.Gray) },
                    textStyle = LocalTextStyle.current.copy(color = Color.Black, fontWeight = FontWeight.Medium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray,
                        unfocusedBorderColor = MyTaskLightGray,
                        focusedBorderColor = MyTaskBlue,
                        cursorColor = MyTaskBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Category", color = MyTaskBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    CategoryButton(
                        text = "Priority Task",
                        isSelected = selectedCategory == "Priority Task",
                        onClick = { selectedCategory = "Priority Task" },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    CategoryButton(
                        text = "Daily Task",
                        isSelected = selectedCategory == "Daily Task",
                        onClick = { selectedCategory = "Daily Task" },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "To do list", color = MyTaskBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = currentSubtask,
                        onValueChange = { currentSubtask = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Add subtask...", color = Color.Gray) },
                        textStyle = LocalTextStyle.current.copy(
                            color = Color.Black,
                            fontWeight = FontWeight.Medium
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color.White,
                            focusedPlaceholderColor = Color.Gray,
                            unfocusedPlaceholderColor = Color.Gray,
                            unfocusedBorderColor = MyTaskBlue.copy(alpha = 0.5f),
                            focusedBorderColor = MyTaskBlue,
                            cursorColor = MyTaskBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (currentSubtask.isNotBlank()) {
                                viewModel.addSubtask(task.id, currentSubtask)
                                currentSubtask = ""
                            }
                        },
                        modifier = Modifier
                            .background(MyTaskBlue, RoundedCornerShape(12.dp))
                            .size(56.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (subtasks.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                                .border(1.dp, MyTaskLightGray, RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No tasks in your to do list",
                                color = Color.Gray,
                                fontSize = 14.sp
                            )
                        }
                    } else {
                        subtasks.forEach { subtask ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color.White, RoundedCornerShape(12.dp))
                                    .border(1.dp, MyTaskBlue.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = subtask.title, 
                                    color = if (subtask.isDone) Color.Gray else Color.Black, 
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f),
                                    textDecoration = if (subtask.isDone) androidx.compose.ui.text.style.TextDecoration.LineThrough else null
                                )
                                IconButton(
                                    onClick = { viewModel.toggleSubtask(subtask, !subtask.isDone) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = if (subtask.isDone) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                        contentDescription = null,
                                        tint = MyTaskBlue,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { viewModel.deleteSubtask(subtask) },
                                    modifier = Modifier.size(24.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete subtask",
                                        tint = Color.Red.copy(alpha = 0.6f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "Description", color = MyTaskBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Task description...", color = Color.Gray) },
                    textStyle = LocalTextStyle.current.copy(color = Color.Black, fontWeight = FontWeight.Medium),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White,
                        focusedPlaceholderColor = Color.Gray,
                        unfocusedPlaceholderColor = Color.Gray,
                        unfocusedBorderColor = MyTaskLightGray,
                        focusedBorderColor = MyTaskBlue,
                        cursorColor = MyTaskBlue
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (title.isBlank()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Title cannot be empty")
                            }
                            return@Button
                        }
                        val updatedTask = task.copy(
                            title = title,
                            description = description,
                            categoryId = if (selectedCategory == "Priority Task") 1L else 2L,
                            startDate = startDate,
                            endDate = endDate,
                            updatedAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
                        )
                        viewModel.updateTask(updatedTask)
                        onBack()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MyTaskBlue)
                ) {
                    Text(text = "Update Task", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
            
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
