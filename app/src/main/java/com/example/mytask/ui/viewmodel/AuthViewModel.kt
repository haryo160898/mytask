package com.example.mytask.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mytask.data.local.entity.UserEntity
import com.example.mytask.data.local.entity.UserSettingsEntity
import com.example.mytask.data.repository.UserRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AuthViewModel(private val repository: UserRepository) : ViewModel() {

    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _userSettings = MutableStateFlow<UserSettingsEntity?>(null)
    val userSettings: StateFlow<UserSettingsEntity?> = _userSettings.asStateFlow()

    init {
        checkSession()
        viewModelScope.launch {
            _currentUser.collect { user ->
                if (user != null) {
                    repository.getSettings(user.id).collect { settings ->
                        if (settings == null) {
                            val newSettings = UserSettingsEntity(userId = user.id)
                            repository.insertSettings(newSettings)
                            _userSettings.value = newSettings
                        } else {
                            _userSettings.value = settings
                        }
                    }
                } else {
                    _userSettings.value = null
                }
            }
        }
    }

    private fun checkSession() {
        viewModelScope.launch {
            val userId = repository.getSessionUserId()
            if (userId != -1L) {
                repository.getUser(userId).firstOrNull()?.let { user ->
                    _currentUser.value = user
                    _authState.value = AuthState.Success(user)
                }
            }
        }
    }

    fun updateBiometric(enabled: Boolean) {
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                repository.updateBiometricEnabled(user.id, enabled)
            }
        }
    }

    fun loginWithBiometric() {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val settings = repository.getBiometricEnabledSettings()
            if (settings != null) {
                repository.getUser(settings.userId).collect { user ->
                    if (user != null) {
                        _currentUser.value = user
                        _authState.value = AuthState.Success(user)
                    } else {
                        _authState.value = AuthState.Error("User not found for biometric login")
                    }
                }
            } else {
                _authState.value = AuthState.Error("Biometric login not enabled for any user")
            }
        }
    }

    fun login(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            val user = repository.login(email, password)
            if (user != null) {
                repository.saveSession(user.id)
                _currentUser.value = user
                _authState.value = AuthState.Success(user)
            } else {
                _authState.value = AuthState.Error("Invalid email or password")
            }
        }
    }

    fun register(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val newUser = UserEntity(
                    name = name,
                    email = email,
                    password = password,
                    createdAt = System.currentTimeMillis().toString()
                )
                val id = repository.register(newUser)
                repository.saveSession(id)
                val registeredUser = newUser.copy(id = id)
                _currentUser.value = registeredUser
                _authState.value = AuthState.Success(registeredUser)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Registration failed: ${e.message}")
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun updateUser(user: UserEntity) {
        viewModelScope.launch {
            repository.updateUser(user)
            _currentUser.value = user
        }
    }

    fun updateProfile(name: String, avatarUri: android.net.Uri?) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch

            var avatarPath = user.avatar
            if (avatarUri != null && avatarUri.toString() != user.avatar) {
                // Jika ada foto baru, simpan ke lokal
                val localPath = repository.saveAvatarLocally(avatarUri)
                if (localPath != null) {
                    avatarPath = localPath
                }
            }

            val updatedUser = user.copy(
                name = name,
                avatar = avatarPath
            )
            repository.updateUser(updatedUser)
            _currentUser.value = updatedUser
        }
    }

    fun logout() {
        repository.clearSession()
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val user: UserEntity) : AuthState()
    data class Error(val message: String) : AuthState()
}
