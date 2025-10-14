package com.babelsoftware.airnote.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.babelsoftware.airnote.domain.model.Settings
import com.babelsoftware.airnote.domain.repository.SettingsRepository
import com.babelsoftware.airnote.widget.NotesWidgetReceiver
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map


private const val PREFERENCES_NAME = "settingsupdated"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = PREFERENCES_NAME)

private object PreferencesKeys {
    val DEFAULT_ROUTE_TYPE = stringPreferencesKey("default_route_type")
    val PASSCODE = stringPreferencesKey("passcode")
    val FINGERPRINT = booleanPreferencesKey("fingerprint")
    val PATTERN = stringPreferencesKey("pattern")
    val VIEW_MODE = booleanPreferencesKey("view_mode")
    val AUTOMATIC_THEME = booleanPreferencesKey("automatic_theme")
    val DARK_THEME = booleanPreferencesKey("dark_theme")
    val DYNAMIC_THEME = booleanPreferencesKey("dynamic_theme")
    val AMOLED_THEME = booleanPreferencesKey("amoled_theme")
    val MINIMALISTIC_MODE = booleanPreferencesKey("minimalistic_mode")
    val EXTREME_AMOLED_MODE = booleanPreferencesKey("extreme_amoled_mode")
    val IS_MARKDOWN_ENABLED = booleanPreferencesKey("is_markdown_enabled")
    val SCREEN_PROTECTION = booleanPreferencesKey("screen_protection")
    val ENCRYPT_BACKUP = booleanPreferencesKey("encrypt_backup")
    val SORT_DESCENDING = booleanPreferencesKey("sort_descending")
    val VAULT_SETTING_ENABLED = booleanPreferencesKey("vault_setting_enabled")
    val VAULT_ENABLED = booleanPreferencesKey("vault_enabled")
    val EDIT_MODE = booleanPreferencesKey("edit_mode")
    val GALLERY_SYNC = booleanPreferencesKey("gallery_sync")
    val SHOW_ONLY_TITLE = booleanPreferencesKey("show_only_title")
    val TERMS_OF_SERVICE = booleanPreferencesKey("terms_of_service")
    val USE_MONO_SPACE_FONT = booleanPreferencesKey("use_mono_space_font")
    val LOCK_IMMEDIATELY = booleanPreferencesKey("lock_immediately")
    val CORNER_RADIUS = intPreferencesKey("corner_radius")
    val DISABLE_SWIPE_IN_EDIT_MODE = booleanPreferencesKey("disable_swipe_in_edit_mode")
    val MAKE_SEARCH_BAR_LONGER = booleanPreferencesKey("make_search_bar_longer")
    val FONT_SIZE = intPreferencesKey("font_size")
    val SHOW_FOLDER_INDICATOR = booleanPreferencesKey("show_folder_indicator")
    val USE_AIRNOTE_API = booleanPreferencesKey("use_airnote_api")
    val SELECTED_MODEL_NAME = stringPreferencesKey("selected_model_name")
    val DESKTOP_MODE_ENABLED = booleanPreferencesKey("desktop_mode_enabled")
    val DESKTOP_MODE_AI_ENABLED = booleanPreferencesKey("desktop_mode_ai_enabled")
    val OPEN_TO_LAST_USED_FOLDER = booleanPreferencesKey("open_to_last_used_folder")
    val LAST_USED_FOLDER_ID = longPreferencesKey("last_used_folder_id")
}

class SettingsRepositoryImpl(private val context: Context) : SettingsRepository {

    override val settings: Flow<Settings> = context.dataStore.data.map { preferences ->
        Settings(
            defaultRouteType = preferences[PreferencesKeys.DEFAULT_ROUTE_TYPE] ?: "Home",
            passcode = preferences[PreferencesKeys.PASSCODE],
            fingerprint = preferences[PreferencesKeys.FINGERPRINT] ?: false,
            pattern = preferences[PreferencesKeys.PATTERN],
            viewMode = preferences[PreferencesKeys.VIEW_MODE] ?: true,
            automaticTheme = preferences[PreferencesKeys.AUTOMATIC_THEME] ?: true,
            darkTheme = preferences[PreferencesKeys.DARK_THEME] ?: false,
            dynamicTheme = preferences[PreferencesKeys.DYNAMIC_THEME] ?: false,
            amoledTheme = preferences[PreferencesKeys.AMOLED_THEME] ?: false,
            minimalisticMode = preferences[PreferencesKeys.MINIMALISTIC_MODE] ?: false,
            extremeAmoledMode = preferences[PreferencesKeys.EXTREME_AMOLED_MODE] ?: false,
            isMarkdownEnabled = preferences[PreferencesKeys.IS_MARKDOWN_ENABLED] ?: true,
            screenProtection = preferences[PreferencesKeys.SCREEN_PROTECTION] ?: false,
            encryptBackup = preferences[PreferencesKeys.ENCRYPT_BACKUP] ?: false,
            sortDescending = preferences[PreferencesKeys.SORT_DESCENDING] ?: true,
            vaultSettingEnabled = preferences[PreferencesKeys.VAULT_SETTING_ENABLED] ?: false,
            vaultEnabled = preferences[PreferencesKeys.VAULT_ENABLED] ?: false,
            editMode = preferences[PreferencesKeys.EDIT_MODE] ?: false,
            gallerySync = preferences[PreferencesKeys.GALLERY_SYNC] ?: true,
            showOnlyTitle = preferences[PreferencesKeys.SHOW_ONLY_TITLE] ?: false,
            termsOfService = preferences[PreferencesKeys.TERMS_OF_SERVICE] ?: false,
            useMonoSpaceFont = preferences[PreferencesKeys.USE_MONO_SPACE_FONT] ?: false,
            lockImmediately = preferences[PreferencesKeys.LOCK_IMMEDIATELY] ?: true,
            cornerRadius = preferences[PreferencesKeys.CORNER_RADIUS] ?: 28,
            disableSwipeInEditMode = preferences[PreferencesKeys.DISABLE_SWIPE_IN_EDIT_MODE] ?: false,
            makeSearchBarLonger = preferences[PreferencesKeys.MAKE_SEARCH_BAR_LONGER] ?: false,
            fontSize = preferences[PreferencesKeys.FONT_SIZE] ?: 13,
            showFolderIndicator = preferences[PreferencesKeys.SHOW_FOLDER_INDICATOR] ?: false,
            useAirNoteApi = preferences[PreferencesKeys.USE_AIRNOTE_API] ?: false,
            selectedModelName = preferences[PreferencesKeys.SELECTED_MODEL_NAME] ?: "gemini-2.0-flash-001",
            desktopModeEnabled = preferences[PreferencesKeys.DESKTOP_MODE_ENABLED] ?: true,
            desktopModeAiEnabled = preferences[PreferencesKeys.DESKTOP_MODE_AI_ENABLED] ?: true,
            openToLastUsedFolder = preferences[PreferencesKeys.OPEN_TO_LAST_USED_FOLDER] ?: false,
            lastUsedFolderId = preferences[PreferencesKeys.LAST_USED_FOLDER_ID]
        )
    }

    override suspend fun update(settings: Settings) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_ROUTE_TYPE] = settings.defaultRouteType
            settings.passcode?.let { preferences[PreferencesKeys.PASSCODE] = it } ?: preferences.remove(PreferencesKeys.PASSCODE)
            preferences[PreferencesKeys.FINGERPRINT] = settings.fingerprint
            settings.pattern?.let { preferences[PreferencesKeys.PATTERN] = it } ?: preferences.remove(PreferencesKeys.PATTERN)
            preferences[PreferencesKeys.VIEW_MODE] = settings.viewMode
            preferences[PreferencesKeys.AUTOMATIC_THEME] = settings.automaticTheme
            preferences[PreferencesKeys.DARK_THEME] = settings.darkTheme
            preferences[PreferencesKeys.DYNAMIC_THEME] = settings.dynamicTheme
            preferences[PreferencesKeys.AMOLED_THEME] = settings.amoledTheme
            preferences[PreferencesKeys.MINIMALISTIC_MODE] = settings.minimalisticMode
            preferences[PreferencesKeys.EXTREME_AMOLED_MODE] = settings.extremeAmoledMode
            preferences[PreferencesKeys.IS_MARKDOWN_ENABLED] = settings.isMarkdownEnabled
            preferences[PreferencesKeys.SCREEN_PROTECTION] = settings.screenProtection
            preferences[PreferencesKeys.ENCRYPT_BACKUP] = settings.encryptBackup
            preferences[PreferencesKeys.SORT_DESCENDING] = settings.sortDescending
            preferences[PreferencesKeys.VAULT_SETTING_ENABLED] = settings.vaultSettingEnabled
            preferences[PreferencesKeys.VAULT_ENABLED] = settings.vaultEnabled
            preferences[PreferencesKeys.EDIT_MODE] = settings.editMode
            preferences[PreferencesKeys.GALLERY_SYNC] = settings.gallerySync
            preferences[PreferencesKeys.SHOW_ONLY_TITLE] = settings.showOnlyTitle
            preferences[PreferencesKeys.TERMS_OF_SERVICE] = settings.termsOfService
            preferences[PreferencesKeys.USE_MONO_SPACE_FONT] = settings.useMonoSpaceFont
            preferences[PreferencesKeys.LOCK_IMMEDIATELY] = settings.lockImmediately
            preferences[PreferencesKeys.CORNER_RADIUS] = settings.cornerRadius
            preferences[PreferencesKeys.DISABLE_SWIPE_IN_EDIT_MODE] = settings.disableSwipeInEditMode
            preferences[PreferencesKeys.MAKE_SEARCH_BAR_LONGER] = settings.makeSearchBarLonger
            preferences[PreferencesKeys.FONT_SIZE] = settings.fontSize
            preferences[PreferencesKeys.SHOW_FOLDER_INDICATOR] = settings.showFolderIndicator
            preferences[PreferencesKeys.USE_AIRNOTE_API] = settings.useAirNoteApi
            preferences[PreferencesKeys.SELECTED_MODEL_NAME] = settings.selectedModelName
            preferences[PreferencesKeys.DESKTOP_MODE_ENABLED] = settings.desktopModeEnabled
            preferences[PreferencesKeys.DESKTOP_MODE_AI_ENABLED] = settings.desktopModeAiEnabled
            preferences[PreferencesKeys.OPEN_TO_LAST_USED_FOLDER] = settings.openToLastUsedFolder
            settings.lastUsedFolderId?.let { preferences[PreferencesKeys.LAST_USED_FOLDER_ID] = it } ?: preferences.remove(PreferencesKeys.LAST_USED_FOLDER_ID)
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