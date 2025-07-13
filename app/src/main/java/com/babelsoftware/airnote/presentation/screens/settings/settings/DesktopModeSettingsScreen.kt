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
    // Mevcut `SettingsScaffold` yapınızı kullanarak tutarlı bir görünüm elde ediyoruz.
    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = "Desktop Mode",
        onBackNavClicked = { navController.navigateUp() }
    ) {
        LazyColumn {
            item {
                // Bu, masaüstü modunu tamamen açıp kapatan anahtar
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = "Masaüstü Modunu Etkinleştir",
                    description = "Geniş ekranlarda yatay modda masaüstü arayüzünü kullan.",
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
                // Bu, sadece masaüstü modundaki AI özelliklerini kontrol eden anahtar
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = "Yapay Zeka Özellikleri",
                    description = "Masaüstü modunda yapay zeka asistanını ve özelliklerini etkinleştir.",
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