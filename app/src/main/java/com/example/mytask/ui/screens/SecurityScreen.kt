package com.example.mytask.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mytask.ui.theme.*
import com.example.mytask.ui.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecurityScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    val userSettings by authViewModel.userSettings.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = androidx.compose.ui.platform.LocalContext.current

    var showPasswordDialog by remember { mutableStateOf(false) }
    var showLoginActivityDialog by remember { mutableStateOf(false) }
    
    val biometricEnabled = userSettings?.biometricEnabled ?: false

    val biometricPrompt = remember {
        androidx.biometric.BiometricPrompt(
            context as androidx.fragment.app.FragmentActivity,
            androidx.core.content.ContextCompat.getMainExecutor(context),
            object : androidx.biometric.BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: androidx.biometric.BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    authViewModel.updateBiometric(true)
                    scope.launch { snackbarHostState.showSnackbar("Biometric enabled") }
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    scope.launch { snackbarHostState.showSnackbar("Authentication error: $errString") }
                }
            }
        )
    }

    val promptInfo = androidx.biometric.BiometricPrompt.PromptInfo.Builder()
        .setTitle("Biometric Authentication")
        .setSubtitle("Confirm to enable biometric lock")
        .setNegativeButtonText("Cancel")
        .build()

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
                text = "Security",
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
                SecurityItem(
                    icon = Icons.Default.Lock,
                    title = "Change Password",
                    description = "Update your account password",
                    onClick = { showPasswordDialog = true }
                )
                
                SecurityItem(
                    icon = Icons.Default.Visibility,
                    title = "Login Activity",
                    description = "View your recent login history",
                    onClick = { showLoginActivityDialog = true }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(MyTaskBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MyTaskBlue)
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "Biometric Lock", fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                        Text(text = "Use fingerprint or face ID", fontSize = 12.sp, color = Color.Gray)
                    }
                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                biometricPrompt.authenticate(promptInfo)
                            } else {
                                authViewModel.updateBiometric(false)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Biometric disabled")
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White, 
                            checkedTrackColor = MyTaskBlue,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.LightGray
                        )
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                Button(
                    onClick = { 
                        scope.launch {
                            snackbarHostState.showSnackbar("Account deactivation requested")
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red.copy(alpha = 0.1f)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text("Deactivate Account", color = Color.Red, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
            }
            
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }

    // Change Password Dialog
    if (showPasswordDialog) {
        var newPassword by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text("Change Password", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Please enter your new password below.", fontSize = 14.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text("New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.Black,
                            unfocusedTextColor = Color.Black,
                            focusedBorderColor = MyTaskBlue,
                            focusedLabelColor = MyTaskBlue
                        )
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        currentUser?.let {
                            authViewModel.updateUser(it.copy(password = newPassword))
                            scope.launch { snackbarHostState.showSnackbar("Password updated successfully") }
                        }
                        showPasswordDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MyTaskBlue,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) { Text("Update") }
            },
            dismissButton = {
                TextButton(onClick = { showPasswordDialog = false }) { 
                    Text("Cancel", color = Color.Gray) 
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Login Activity Dialog
    if (showLoginActivityDialog) {
        AlertDialog(
            onDismissRequest = { showLoginActivityDialog = false },
            title = { Text("Recent Login Activity") },
            text = {
                Column {
                    Text("• Asus ASUS_AI2401_D - Active Now", fontSize = 14.sp)
                    Text("  Jakarta, Indonesia", fontSize = 12.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Windows PC - 2 hours ago", fontSize = 14.sp)
                    Text("  Jakarta, Indonesia", fontSize = 12.sp, color = Color.Gray)
                }
            },
            confirmButton = {
                TextButton(onClick = { showLoginActivityDialog = false }) { Text("Close") }
            }
        )
    }
}

@Composable
fun SecurityItem(
    icon: ImageVector, 
    title: String, 
    description: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(MyTaskBlue.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = MyTaskBlue)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
            Text(text = description, fontSize = 12.sp, color = Color.Gray)
        }
        Icon(
            imageVector = Icons.Default.ArrowBackIosNew, 
            contentDescription = null, 
            tint = Color.LightGray, 
            modifier = Modifier.size(16.dp).graphicsLayer(rotationZ = 180f)
        )
    }
}
