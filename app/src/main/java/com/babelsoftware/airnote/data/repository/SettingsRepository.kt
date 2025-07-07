package com.babelsoftware.airnote.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.babelsoftware.airnote.domain.model.Settings
import com.babelsoftware.airnote.widget.NotesWidgetReceiver
import com.babelsoftware.airnote.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


private const val PREFERENCES_NAME = "settingsupdated"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = PREFERENCES_NAME,
    produceMigrations = { context -> listOf(SharedPreferencesMigration(context, PREFERENCES_NAME)) }
)

private object PreferencesKeys {
    val USE_AIRNOTE_API = booleanPreferencesKey("use_airnote_api")
    val SHOW_FOLDER_INDICATOR = booleanPreferencesKey("show_folder_indicator")
    val SELECTED_MODEL_NAME = stringPreferencesKey("selected_model_name")
}

class SettingsRepositoryImpl (private val context: Context) : SettingsRepository {

    override val settings: Flow<Settings> = context.dataStore.data
        .map { preferences ->
            Settings(
                // Settings.kt içeriğinin hepsi
                useAirNoteApi = preferences[PreferencesKeys.USE_AIRNOTE_API] ?: true,
                selectedModelName = preferences[PreferencesKeys.SELECTED_MODEL_NAME] ?: "gemini-1.5-flash",
                showFolderIndicator = preferences[PreferencesKeys.SHOW_FOLDER_INDICATOR] ?: false
            )
        }

    override suspend fun update(settings: Settings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.USE_AIRNOTE_API] = settings.useAirNoteApi
            preferences[PreferencesKeys.SHOW_FOLDER_INDICATOR] = settings.showFolderIndicator
            preferences[PreferencesKeys.SELECTED_MODEL_NAME] = settings.selectedModelName
        }
    }

    override suspend fun getPreferences(): Preferences {
        return context.dataStore.data.first()
    }

    override suspend fun putString(key: String, value: String) {
        val preferencesKey = stringPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override suspend fun putInt(key: String, value: Int) {
        val preferencesKey = intPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override suspend fun getString(key: String): String? {
        val preferencesKey = stringPreferencesKey(key)
        val preferences = context.dataStore.data.first()
        return preferences[preferencesKey]
    }

    override suspend fun getInt(key: String): Int? {
        val preferencesKey = intPreferencesKey(key)
        val preferences = context.dataStore.data.first()
        return preferences[preferencesKey]
    }

    override suspend fun putBoolean(key: String, value: Boolean) {
        val preferencesKey = booleanPreferencesKey(key)
        context.dataStore.edit { preferences ->
            preferences[preferencesKey] = value
        }
    }

    override suspend fun getBoolean(key: String): Boolean? {
        val preferencesKey = booleanPreferencesKey(key)
        val preferences = context.dataStore.data.first()
        return preferences[preferencesKey]
    }

    override suspend fun getEveryNotesWidget(): List<Pair<Int, Int>> {
        val preferences = context.dataStore.data.first()
        val widgetPairs = mutableListOf<Pair<Int, Int>>()

        preferences.asMap().forEach { entry ->
            val key = entry.key.name

            if (entry.key.name.startsWith(NotesWidgetReceiver.Companion.WIDGET_PREFERENCE)) {
                val widgetId = key.substringAfter(NotesWidgetReceiver.Companion.WIDGET_PREFERENCE).toIntOrNull()
                if (widgetId != null) {
                    val value = entry.value as? Int ?: 0
                    widgetPairs.add(widgetId to value)
                }
            }
        }
        return widgetPairs
    }
}