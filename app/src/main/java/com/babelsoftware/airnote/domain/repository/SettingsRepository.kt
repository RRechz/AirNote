package com.babelsoftware.airnote.domain.repository

import androidx.datastore.preferences.core.Preferences
import com.babelsoftware.airnote.domain.model.Settings
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<Settings>

    suspend fun update(settings: Settings)

    suspend fun getPreferences(): Preferences
    suspend fun putString(key: String, value: String)
    suspend fun putInt(key: String, value: Int)
    suspend fun getString(key: String): String?
    suspend fun getInt(key: String): Int?
    suspend fun putBoolean(key: String, value: Boolean)
    suspend fun getBoolean(key: String): Boolean?
    suspend fun getEveryNotesWidget(): List<Pair<Int, Int>>
}