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
import com.example.mytask.ui.theme.MyTaskBlue
import com.example.mytask.ui.theme.MyTaskLightBlue
import com.example.mytask.ui.theme.MyTaskLightGray
import com.example.mytask.ui.theme.MyTaskTheme
import com.example.mytask.ui.viewmodel.TaskViewModel
import com.example.mytask.ui.viewmodel.AuthViewModel
import androidx.compose.ui.tooling.preview.Preview
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddTaskScreen(
    viewModel: TaskViewModel,
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    AddTaskContent(
        onAddTask = { title, description, category, startDate, endDate, subtasks ->
            currentUser?.let { user ->
                viewModel.addTask(
                    userId = user.id,
                    title = title,
                    description = description,
                    category = category,
                    startDate = startDate,
                    endDate = endDate,
                    subtasks = subtasks
                )
                onBack()
            }
        },
        onBack = onBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTaskContent(
    onAddTask: (String, String, String, String, String, List<String>) -> Unit,
    onBack: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Priority Task") }
    
    val dateFormatter = remember { SimpleDateFormat("MMM-dd-yyyy", Locale.getDefault()) }
    var startDate by remember { mutableStateOf(dateFormatter.format(Date())) }
    var endDate by remember { mutableStateOf(dateFormatter.format(Date())) }
    
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val subtasks = remember { mutableStateListOf<String>() }
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
                Icon(
                    imageVector = Icons.Default.ArrowBackIosNew,
                    contentDescription = "Back",
                    tint = MyTaskBlue,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = "Add Task",
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(40.dp))
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
                            unfocusedBorderColor = MyTaskLightGray,
                            focusedBorderColor = MyTaskBlue,
                            cursorColor = MyTaskBlue
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (currentSubtask.isNotBlank()) {
                                subtasks.add(currentSubtask)
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
                    subtasks.forEach { subtask ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White, RoundedCornerShape(12.dp))
                                .border(1.dp, MyTaskLightGray, RoundedCornerShape(12.dp))
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = subtask,
                                color = Color.Black,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                imageVector = Icons.Default.RadioButtonUnchecked,
                                contentDescription = null,
                                tint = MyTaskBlue,
                                modifier = Modifier.size(24.dp)
                            )
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
                        onAddTask(
                            title,
                            description,
                            selectedCategory,
                            startDate,
                            endDate,
                            subtasks.toList()
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MyTaskBlue,
                        contentColor = Color.White
                    )
                ) {
                    Text(text = "Create Task", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
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

@Composable
fun DateInputField(
    label: String, 
    date: String, 
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color.White
) {
    Column(modifier = modifier) {
        Text(text = label, color = MyTaskBlue, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(backgroundColor, RoundedCornerShape(12.dp))
                .border(1.dp, MyTaskLightGray, RoundedCornerShape(12.dp))
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth, 
                contentDescription = null, 
                tint = MyTaskBlue, 
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = date, color = Color.Gray, fontSize = 14.sp)
        }
    }
}

@Composable
fun CategoryButton(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MyTaskBlue else Color.White,
        border = if (!isSelected) androidx.compose.foundation.BorderStroke(1.dp, MyTaskLightGray) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = text, color = if (isSelected) Color.White else MyTaskBlue, fontWeight = FontWeight.Medium, fontSize = 14.sp)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AddTaskScreenPreview() {
    MyTaskTheme {
        AddTaskContent(
            onAddTask = { _, _, _, _, _, _ -> },
            onBack = {}
        )
    }
}
