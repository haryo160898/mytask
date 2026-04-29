package com.example.mytask.data.local.dao

import androidx.room.*
import com.example.mytask.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SettingsDao {
    @Query("SELECT * FROM user_settings WHERE user_id = :userId LIMIT 1")
    fun getSettingsByUserId(userId: Long): Flow<UserSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: UserSettingsEntity)

    @Update
    suspend fun updateSettings(settings: UserSettingsEntity)

    @Query("UPDATE user_settings SET biometric_enabled = :enabled WHERE user_id = :userId")
    suspend fun updateBiometricEnabled(userId: Long, enabled: Boolean)

    @Query("SELECT * FROM user_settings WHERE biometric_enabled = 1 LIMIT 1")
    suspend fun getBiometricEnabledSettings(): UserSettingsEntity?
}
