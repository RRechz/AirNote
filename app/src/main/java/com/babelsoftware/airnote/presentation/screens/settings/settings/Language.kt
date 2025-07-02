package com.babelsoftware.airnote.presentation.screens.settings.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FontDownload
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import androidx.navigation.NavController
import com.babelsoftware.airnote.presentation.screens.settings.SettingsScaffold
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.presentation.screens.settings.widgets.ActionType
import com.babelsoftware.airnote.presentation.screens.settings.widgets.ListDialog
import com.babelsoftware.airnote.presentation.screens.settings.widgets.SettingsBox
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.presentation.screens.settings.model.IconResource

@Composable
fun LanguageScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = stringResource(id = R.string.language),
        onBackNavClicked = { navController.navigateUp() }
    ) {
        LazyColumn {
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.language),
                    description = stringResource(id = R.string.language_description),
                    icon = IconResource.Vector(Icons.Rounded.Translate),
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isBoth = true
                    ),
                    actionType = ActionType.CUSTOM,
                    customAction = { onExit ->
                        OnLanguageClicked(settingsViewModel) { onExit() }
                    }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.monospace_font),
                    icon = IconResource.Vector(Icons.Rounded.FontDownload),
                    description = stringResource(id = R.string.monospace_font_description),
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isBoth = true
                    ),
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.useMonoSpaceFont,
                    switchEnabled = {
                        settingsViewModel.update(
                            settingsViewModel.settings.value.copy(
                                useMonoSpaceFont = it
                            )
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun OnLanguageClicked(settingsViewModel: SettingsViewModel, onExit: () -> Unit) {
    val context = LocalContext.current
    val languages = settingsViewModel.getSupportedLanguages(context).toList()
    ListDialog(
        text = stringResource(R.string.language),
        list = languages,
        settingsViewModel = settingsViewModel,
        onExit = onExit,
        extractDisplayData = { it },
        initialItem = Pair(context.getString(R.string.system_language), second = ""),
        setting = { isFirstItem, isLastItem, displayData ->
            SettingsBox(
                settingsViewModel = settingsViewModel,
                size = 8.dp,
                title = displayData.first,
                icon = IconResource.Vector(Icons.Rounded.Translate),
                radius = shapeManager(
                    isFirst = isFirstItem,
                    isLast = isLastItem,
                    radius = settingsViewModel.settings.value.cornerRadius
                ),
                actionType = ActionType.RADIOBUTTON,
                variable = if (displayData.second.isNotBlank()) {
                    AppCompatDelegate.getApplicationLocales()[0]?.language == displayData.second
                } else {
                    AppCompatDelegate.getApplicationLocales().isEmpty
                },
                switchEnabled = {
                    if (displayData.second.isNotBlank()) {
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(
                                displayData.second
                            )
                        )
                    } else {
                        AppCompatDelegate.setApplicationLocales(LocaleListCompat.getEmptyLocaleList())
                    }
                }
            )
        }
    )
}