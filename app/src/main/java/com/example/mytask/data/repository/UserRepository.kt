package com.example.mytask.data.repository

import android.content.Context
import com.example.mytask.data.local.dao.SettingsDao
import com.example.mytask.data.local.dao.UserDao
import com.example.mytask.data.local.entity.UserEntity
import com.example.mytask.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(
    private val userDao: UserDao,
    private val settingsDao: SettingsDao,
    private val context: Context
) {
    private val sharedPreferences = context.getSharedPreferences("mytask_prefs", Context.MODE_PRIVATE)

    suspend fun register(user: UserEntity): Long = userDao.registerUser(user)
    suspend fun login(email: String, password: String): UserEntity? = userDao.login(email, password)
    fun getUser(userId: Long): Flow<UserEntity?> = userDao.getUserById(userId)
    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    fun getSettings(userId: Long): Flow<UserSettingsEntity?> = settingsDao.getSettingsByUserId(userId)
    suspend fun updateBiometricEnabled(userId: Long, enabled: Boolean) = settingsDao.updateBiometricEnabled(userId, enabled)
    suspend fun insertSettings(settings: UserSettingsEntity) = settingsDao.insertSettings(settings)
    suspend fun getBiometricEnabledSettings(): UserSettingsEntity? = settingsDao.getBiometricEnabledSettings()

    // Avatar Management
    fun saveAvatarLocally(uri: android.net.Uri): String? {
        return try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val fileName = "avatar_${java.util.UUID.randomUUID()}.jpg"
            val file = java.io.File(context.filesDir, fileName)
            val outputStream = java.io.FileOutputStream(file)

            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            file.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    // Session Management
    fun saveSession(userId: Long) {
        sharedPreferences.edit().putLong("logged_in_user_id", userId).apply()
    }

    fun getSessionUserId(): Long {
        return sharedPreferences.getLong("logged_in_user_id", -1L)
    }

    fun clearSession() {
        sharedPreferences.edit().remove("logged_in_user_id").apply()
    }
}
