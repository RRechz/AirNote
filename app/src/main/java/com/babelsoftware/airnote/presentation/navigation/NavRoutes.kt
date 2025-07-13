package com.babelsoftware.airnote.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.babelsoftware.airnote.presentation.screens.settings.MainSettings
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.presentation.screens.settings.settings.AboutScreen
import com.babelsoftware.airnote.presentation.screens.settings.settings.CloudScreen
import com.babelsoftware.airnote.presentation.screens.settings.settings.ColorStylesScreen
import com.babelsoftware.airnote.presentation.screens.settings.settings.LanguageScreen
import com.babelsoftware.airnote.presentation.screens.settings.settings.MarkdownScreen
import com.babelsoftware.airnote.presentation.screens.settings.settings.PrivacyScreen
import com.babelsoftware.airnote.presentation.screens.settings.settings.SupportScreen
import com.babelsoftware.airnote.presentation.screens.settings.AirNoteAiSettingsScreen
import com.babelsoftware.airnote.presentation.screens.settings.settings.DesktopModeSettingsScreen
import com.babelsoftware.airnote.presentation.screens.settings.settings.ToolsScreen

enum class ActionType {
    PASSCODE,
    FINGERPRINT,
    PATTERN
}

sealed class NavRoutes(val route: String) {
    data object Home : NavRoutes("home")
    data object Edit : NavRoutes("edit_note_screen/{id}/{encrypted}?folderId={folderId}") {
        fun createRoute(id: Int, encrypted: Boolean, folderId: Long? = null): String {
            val baseRoute = "edit_note_screen/$id/$encrypted"
            return if (folderId != null) {
                "$baseRoute?folderId=$folderId"
            } else {
                baseRoute
            }
        }
    }
    data object Terms : NavRoutes("terms")
    data object Settings : NavRoutes("settings")
    data object ColorStyles : NavRoutes("settings/color_styles")
    data object Language : NavRoutes("settings/language")
    data object Cloud : NavRoutes("settings/cloud")
    data object Privacy : NavRoutes("settings/privacy")
    data object Markdown : NavRoutes("settings/markdown")
    data object Tools : NavRoutes("settings/tools")
    data object History : NavRoutes("settings/history")
    data object Widgets : NavRoutes("settings/widgets")
    data object About : NavRoutes("settings/about")
    data object Support : NavRoutes("settings/support")
    data object AirNoteAiSettings : NavRoutes("settings/ai_settings")
    data object DesktopMode : NavRoutes("settings/desktop_mode")
    data object DesktopModeSettings : NavRoutes("settings/desktop_mode_settings")
    data object LockScreen : NavRoutes("settings/lock/{type}") {
        fun createRoute(action: ActionType?) = "settings/lock/$action"
    }
}

val settingScreens = mapOf<String, @Composable (settingsViewModel: SettingsViewModel, navController : NavController) -> Unit>(
    NavRoutes.Settings.route to { settings, navController -> MainSettings(settings, navController) },
    NavRoutes.ColorStyles.route to { settings, navController ->
        ColorStylesScreen(
            navController,
            settings
        )
    },
    NavRoutes.Language.route to { settings, navController ->
        LanguageScreen(
            navController,
            settings
        )
    },
    NavRoutes.Cloud.route to { settings, navController -> CloudScreen(navController, settings) },
    NavRoutes.Privacy.route to { settings, navController -> PrivacyScreen(navController, settings) },
    NavRoutes.Markdown.route to { settings, navController ->
        MarkdownScreen(
            navController,
            settings
        )
    },
    NavRoutes.Tools.route to { settings, navController -> ToolsScreen(navController, settings) },
    NavRoutes.About.route to { settings, navController -> AboutScreen(navController, settings) },
    NavRoutes.Support.route to { settings, navController -> SupportScreen(navController, settings) },
    NavRoutes.AirNoteAiSettings.route to { settings, navController ->
        AirNoteAiSettingsScreen(navController, settings)
    },
    NavRoutes.DesktopModeSettings.route to { settings, navController ->
        DesktopModeSettingsScreen(navController, settings)
    }
)
