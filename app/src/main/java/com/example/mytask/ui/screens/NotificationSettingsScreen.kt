package com.example.mytask.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytask.ui.theme.MyTaskBlue

@Composable
fun NotificationSettingsScreen(
    onBack: () -> Unit
) {
    var pushEnabled by remember { mutableStateOf(true) }
    var taskReminderEnabled by remember { mutableStateOf(true) }
    var soundEnabled by remember { mutableStateOf(true) }
    var vibrationEnabled by remember { mutableStateOf(false) }

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
                text = "Notification Settings",
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
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
                    .padding(24.dp)
            ) {
                Text(
                    text = "Preferences",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SettingSwitchItem(
                    title = "Push Notifications",
                    description = "Allow MyTask to send you notifications",
                    checked = pushEnabled,
                    onCheckedChange = { pushEnabled = it }
                )

                SettingSwitchItem(
                    title = "Task Reminders",
                    description = "Get notified about upcoming task deadlines",
                    checked = taskReminderEnabled,
                    onCheckedChange = { taskReminderEnabled = it }
                )

                SettingSwitchItem(
                    title = "Sound",
                    description = "Play sound for notifications",
                    checked = soundEnabled,
                    onCheckedChange = { soundEnabled = it }
                )

                SettingSwitchItem(
                    title = "Vibration",
                    description = "Vibrate on notifications",
                    checked = vibrationEnabled,
                    onCheckedChange = { vibrationEnabled = it }
                )
            }
        }
    }
}

@Composable
fun SettingSwitchItem(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Text(text = description, fontSize = 12.sp, color = Color.Gray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MyTaskBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}
