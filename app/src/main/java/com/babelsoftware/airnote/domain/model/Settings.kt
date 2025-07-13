package com.babelsoftware.airnote.domain.model

import com.babelsoftware.airnote.presentation.navigation.NavRoutes

data class Settings(
    var defaultRouteType: String = NavRoutes.Home.route,
    var passcode: String? = null,
    var fingerprint: Boolean = false,
    var pattern: String? = null,
    val viewMode: Boolean = true,
    val automaticTheme: Boolean = true,
    val darkTheme: Boolean = false,
    var dynamicTheme: Boolean = false,
    var amoledTheme: Boolean = false,
    var minimalisticMode: Boolean = false,
    var extremeAmoledMode: Boolean = false,
    var isMarkdownEnabled: Boolean = true,
    var screenProtection: Boolean = false,
    var encryptBackup: Boolean = false,
    var sortDescending: Boolean = true,
    var vaultSettingEnabled: Boolean = false,
    var vaultEnabled: Boolean = false,
    var editMode: Boolean = false,
    var gallerySync: Boolean = true,
    var showOnlyTitle: Boolean = false,
    var termsOfService: Boolean = false,
    var useMonoSpaceFont: Boolean = false,
    var lockImmediately: Boolean = true,
    var cornerRadius: Int = 28,
    var disableSwipeInEditMode: Boolean = false,
    var makeSearchBarLonger: Boolean = false,
    var fontSize: Int = 13,
    val showFolderIndicator: Boolean = false,
    val useAirNoteApi: Boolean = false, // AirNote API will use your API by default
    val selectedModelName: String = "gemini-2.0-flash-001",
    val desktopModeEnabled: Boolean = true,
    val desktopModeAiEnabled: Boolean = true
)

