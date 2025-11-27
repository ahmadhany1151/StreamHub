package com.example.movieapp.ui.settings

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountSettingsScreen(
    viewModel: AccountSettingsViewModel = viewModel(),
    onLogout: () -> Unit = {},
    onBackClick: () -> Unit = {}
) {
    val settingsState by viewModel.settingsState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val navigationEvent by viewModel.navigationEvent.collectAsState()
    val usernameUpdateSuccess by viewModel.usernameUpdateSuccess.collectAsState()

    LaunchedEffect(navigationEvent) {
        when (navigationEvent) {
            is LogoutEvent.Success -> {
                Log.d("AccountSettingsScreen", "Logout success received, calling onLogout")
                viewModel.clearNavigationEvent()
                onLogout() // This should trigger MainActivity's handleLogout()
            }
            is LogoutEvent.Failure -> {
                Log.e("AccountSettingsScreen", "Logout failure: ${(navigationEvent as LogoutEvent.Failure).message}")
                viewModel.clearNavigationEvent()
            }
            null -> {}
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            viewModel.clearUsernameSuccess()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Account Settings", color = Color.White) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Black
                ),
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                }
            )
        },
        containerColor = Color.Black
    ) { paddingValues ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color.White)
            }
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 30.dp)
        ) {
            // Username Section
            Column {
                Text(
                    text = "Username",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = settingsState.username,
                    onValueChange = viewModel::updateUsername,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Username") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray,
                        cursorColor = Color.White
                    )
                )
                if (usernameUpdateSuccess || settingsState.showUsernameSuccess) {
                    Text(
                        text = "Username updated successfully!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Green,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Button(
                    onClick = { viewModel.saveUsername() },
                    enabled = settingsState.isUsernameChanged && settingsState.username.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Save Username")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Password Change Section
            Column {
                Text(
                    text = "Change Password",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = settingsState.currentPassword,
                    onValueChange = viewModel::updateCurrentPassword,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Current Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray,
                        cursorColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = settingsState.newPassword,
                    onValueChange = viewModel::updateNewPassword,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray,
                        cursorColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = settingsState.confirmPassword,
                    onValueChange = viewModel::updateConfirmPassword,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Confirm New Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.Gray,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray,
                        cursorColor = Color.White
                    )
                )
                settingsState.passwordError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Red,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                if (settingsState.isPasswordChanged) {
                    Text(
                        text = "Password updated successfully!",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Green,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                Button(
                    onClick = { viewModel.savePassword() },
                    enabled = settingsState.currentPassword.isNotBlank() &&
                            settingsState.newPassword.isNotBlank() &&
                            settingsState.confirmPassword.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                ) {
                    Text("Save Password")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Logout Button
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = {
                        Log.d("AccountSettingsScreen", "Logout button clicked")
                        viewModel.logout()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Red,
                        contentColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth(0.8f)
                ) {
                    Text("Logout")
                }
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}