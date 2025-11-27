package com.example.movieapp.ui.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AccountSettingsViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val _settingsState = MutableStateFlow(AccountSettingsState())
    val settingsState: StateFlow<AccountSettingsState> = _settingsState.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _navigationEvent = MutableStateFlow<LogoutEvent?>(null)
    val navigationEvent: StateFlow<LogoutEvent?> = _navigationEvent.asStateFlow()

    private val _usernameUpdateSuccess = MutableStateFlow(false)
    val usernameUpdateSuccess: StateFlow<Boolean> = _usernameUpdateSuccess.asStateFlow()


    init {
        loadUserData()
    }

    private fun loadUserData() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            _settingsState.value = _settingsState.value.copy(
                username = currentUser.displayName ?: "User",
                email = currentUser.email ?: "",
                originalUsername = currentUser.displayName ?: "User"
            )

            loadUserDataFromFirestore(currentUser.uid)
        }
    }

    private fun loadUserDataFromFirestore(userId: String) {
        viewModelScope.launch {
            try {
                val userDoc = db.collection("users").document(userId).get().await()
                if (userDoc.exists()) {
                    val userData = userDoc.data
                    val username = userData?.get("username") as? String
                    if (!username.isNullOrEmpty()) {
                        _settingsState.value = _settingsState.value.copy(
                            username = username,
                            originalUsername = username
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateUsername(newUsername: String) {
        _settingsState.value = _settingsState.value.copy(
            username = newUsername,
            isUsernameChanged = newUsername.trim() != _settingsState.value.originalUsername.trim(),
            showUsernameSuccess = false
        )
        _usernameUpdateSuccess.value = false
    }

    fun saveUsername() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val userId = auth.currentUser?.uid ?: return@launch
                val newUsername = _settingsState.value.username.trim()

                // Update in Firestore
                db.collection("users").document(userId)
                    .update("username", newUsername)
                    .await()

                _settingsState.value = _settingsState.value.copy(
                    isUsernameChanged = false,
                    originalUsername = newUsername,
                    username = newUsername,
                    showUsernameSuccess = true
                )

                _usernameUpdateSuccess.value = true

                Log.d("AccountSettings", "Username updated successfully to: $newUsername")

            } catch (e: Exception) {
                e.printStackTrace()
                _settingsState.value = _settingsState.value.copy(
                    showUsernameSuccess = false
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearUsernameSuccess() {
        _usernameUpdateSuccess.value = false
        _settingsState.value = _settingsState.value.copy(
            showUsernameSuccess = false
        )
    }

    fun updateCurrentPassword(password: String) {
        _settingsState.value = _settingsState.value.copy(
            currentPassword = password,
            passwordError = null,
            isPasswordChanged = false
        )
    }

    fun updateNewPassword(password: String) {
        _settingsState.value = _settingsState.value.copy(
            newPassword = password,
            passwordError = null,
            isPasswordChanged = false
        )
    }

    fun updateConfirmPassword(password: String) {
        _settingsState.value = _settingsState.value.copy(
            confirmPassword = password,
            passwordError = null,
            isPasswordChanged = false
        )
    }

    fun savePassword() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val state = _settingsState.value
                val user = auth.currentUser ?: throw Exception("User not authenticated")
                val userEmail = user.email ?: throw Exception("User email not found")

                _settingsState.value = state.copy(passwordError = null)

                if (state.currentPassword.isBlank()) {
                    _settingsState.value = state.copy(
                        passwordError = "Current password is required",
                        isPasswordChanged = false
                    )
                    return@launch
                }

                if (state.newPassword.length < 6) {
                    _settingsState.value = state.copy(
                        passwordError = "Password must be at least 6 characters",
                        isPasswordChanged = false
                    )
                    return@launch
                }

                if (state.newPassword != state.confirmPassword) {
                    _settingsState.value = state.copy(
                        passwordError = "New passwords don't match",
                        isPasswordChanged = false
                    )
                    return@launch
                }

                val credential = EmailAuthProvider.getCredential(userEmail, state.currentPassword)
                user.reauthenticate(credential).await()
                user.updatePassword(state.newPassword).await()

                _settingsState.value = state.copy(
                    passwordError = null,
                    isPasswordChanged = true,
                    currentPassword = "",
                    newPassword = "",
                    confirmPassword = ""
                )

            } catch (e: Exception) {
                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        _settingsState.value = _settingsState.value.copy(
                            passwordError = "Current password is incorrect",
                            isPasswordChanged = false
                        )
                    }
                    else -> {
                        _settingsState.value = _settingsState.value.copy(
                            passwordError = "Failed to change password: ${e.message}",
                            isPasswordChanged = false
                        )
                    }
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                Log.d("AccountSettings", "Logout started")

                auth.signOut()

                Log.d("AccountSettings", "Firebase signOut completed")

                _settingsState.value = AccountSettingsState()

                _navigationEvent.value = LogoutEvent.Success

                Log.d("AccountSettings", "Logout event triggered")
            } catch (e: Exception) {
                Log.e("AccountSettings", "Logout error: ${e.message}")
                e.printStackTrace()
                _navigationEvent.value = LogoutEvent.Failure(e.message ?: "Logout failed")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun clearNavigationEvent() {
        _navigationEvent.value = null
    }
}

sealed class LogoutEvent {
    object Success : LogoutEvent()
    data class Failure(val message: String) : LogoutEvent()
}

data class AccountSettingsState(
    val username: String = "",
    val email: String = "",
    val originalUsername: String = "",
    val currentPassword: String = "",
    val newPassword: String = "",
    val confirmPassword: String = "",
    val passwordError: String? = null,
    val isUsernameChanged: Boolean = false,
    val isPasswordChanged: Boolean = false,
    val showUsernameSuccess: Boolean = false
)