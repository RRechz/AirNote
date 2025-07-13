package com.babelsoftware.airnote.presentation.screens.settings.settings

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.DesktopWindows
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.babelsoftware.airnote.presentation.screens.settings.SettingsScaffold
import com.babelsoftware.airnote.presentation.screens.settings.model.IconResource
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.presentation.screens.settings.widgets.ActionType
import com.babelsoftware.airnote.presentation.screens.settings.widgets.SettingsBox

@Composable
fun DesktopModeSettingsScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = "Desktop Mode",
        onBackNavClicked = { navController.navigateUp() }
    ) {
        LazyColumn {
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = "Desktop UI Mode",
                    description = "Use the desktop interface in landscape mode on large screens.",
                    icon = IconResource.Vector(Icons.Rounded.DesktopWindows),
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isFirst = true
                    ),
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.desktopModeEnabled,
                    switchEnabled = { isEnabled ->
                        settingsViewModel.update(
                            settingsViewModel.settings.value.copy(
                                desktopModeEnabled = isEnabled
                            )
                        )
                    }
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = "Desktop AI Features",
                    description = "Activate the AI assistant and its features in desktop mode.",
                    icon = IconResource.Vector(Icons.Rounded.AutoAwesome),
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isLast = true
                    ),
                    actionType = ActionType.SWITCH,
                    isEnabled = settingsViewModel.settings.value.desktopModeEnabled,
                    variable = settingsViewModel.settings.value.desktopModeAiEnabled,
                    switchEnabled = { isEnabled ->
                        settingsViewModel.update(
                            settingsViewModel.settings.value.copy(
                                desktopModeAiEnabled = isEnabled
                            )
                        )
                    }
                )
            }
        }
    }
}