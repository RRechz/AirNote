package com.babelsoftware.airnote.presentation.screens.settings.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Label
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.Battery1Bar
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.DynamicFeed
import androidx.compose.material.icons.rounded.FormatSize
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.HdrAuto
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.RoundedCorner
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.presentation.screens.settings.SettingsScaffold
import com.babelsoftware.airnote.presentation.screens.settings.model.IconResource
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.presentation.screens.settings.widgets.ActionType
import com.babelsoftware.airnote.presentation.screens.settings.widgets.SettingsBox

fun shapeManager(isBoth: Boolean = false,isLast: Boolean = false,isFirst: Boolean = false,radius: Int): RoundedCornerShape {
    val smallerRadius: Dp = (radius/5).dp
    val defaultRadius: Dp = radius.dp

    return when {
        isBoth -> RoundedCornerShape(defaultRadius)
        isLast -> RoundedCornerShape(smallerRadius,smallerRadius,defaultRadius,defaultRadius)
        isFirst -> RoundedCornerShape(defaultRadius,defaultRadius,smallerRadius,smallerRadius)
        else -> RoundedCornerShape(smallerRadius)
    }
}

@Composable
fun ColorStylesScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = stringResource(id = R.string.color_styles),
        onBackNavClicked = { navController.navigateUp() }
    ) {
        LazyColumn {
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.minimalistic_mode),
                    description = stringResource(id = R.string.minimalistic_mode_description),
                    icon = IconResource.Vector(Icons.Rounded.DynamicFeed),
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isBoth = true
                    ),
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.minimalisticMode,
                    switchEnabled = {
                        settingsViewModel.update(
                            settingsViewModel.settings.value.copy(
                                minimalisticMode = it
                            )
                        )
                    }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.system_theme),
                    description = stringResource(id = R.string.system_theme_description),
                    icon = IconResource.Vector(Icons.Rounded.HdrAuto),
                    actionType = ActionType.SWITCH,
                    radius = shapeManager(
                        isFirst = true,
                        isBoth = (!isSystemInDarkTheme() && settingsViewModel.settings.value.automaticTheme),
                        radius = settingsViewModel.settings.value.cornerRadius
                    ),
                    variable = settingsViewModel.settings.value.automaticTheme,
                    switchEnabled = {
                        settingsViewModel.update(
                            settingsViewModel.settings.value.copy(
                                automaticTheme = it
                            )
                        )
                    }
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.dark_theme),
                    description = stringResource(id = R.string.dark_theme_description),
                    isEnabled = !settingsViewModel.settings.value.automaticTheme,
                    icon = IconResource.Vector(Icons.Rounded.Palette),
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius),
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.darkTheme,
                    switchEnabled = {
                        settingsViewModel.update(
                            settingsViewModel.settings.value.copy(
                                automaticTheme = false,
                                darkTheme = it
                            )
                        )
                    }
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.dynamic_colors),
                    description = stringResource(id = R.string.dynamic_colors_description),
                    icon = IconResource.Vector(Icons.Rounded.Colorize),
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isLast = !(settingsViewModel.settings.value.darkTheme)
                    ),
                    isEnabled = !settingsViewModel.settings.value.automaticTheme,
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.dynamicTheme,
                    switchEnabled = {
                        settingsViewModel.update(
                            settingsViewModel.settings.value.copy(
                                automaticTheme = false,
                                dynamicTheme = it
                            )
                        )
                    }
                )
            }
            item {
                val value = settingsViewModel.settings.value.amoledTheme
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.amoled_colors),
                    description = stringResource(id = R.string.amoled_colors_description),
                    icon = IconResource.Vector(Icons.Rounded.DarkMode),
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isLast = !value
                    ),
                    actionType = ActionType.SWITCH,
                    isEnabled = settingsViewModel.settings.value.darkTheme,
                    variable = value,
                    switchEnabled = {
                        settingsViewModel.update(
                            settingsViewModel.settings.value.copy(
                                amoledTheme = it
                            )
                        )
                    }
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.extreme_amoled_mode),
                    icon = IconResource.Vector(Icons.Rounded.Battery1Bar),
                    description = stringResource(id = R.string.extreme_amoled_mode_description),
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isLast = true
                    ),
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.extremeAmoledMode,
                    isEnabled = settingsViewModel.settings.value.amoledTheme && settingsViewModel.settings.value.darkTheme,
                    switchEnabled = {
                        settingsViewModel.update(
                            settingsViewModel.settings.value.copy(
                                extremeAmoledMode = it
                            )
                        )
                    }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.radius),
                    description = stringResource(id = R.string.radius_description),
                    icon = IconResource.Vector(Icons.Rounded.RoundedCorner),
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isFirst = true
                    ),
                    actionType = ActionType.CUSTOM,
                    customAction = { onExit ->
                        SettingsSliderDialog(
                            title = stringResource(id = R.string.select_radius),
                            valueLabelText = "${settingsViewModel.settings.value.cornerRadius} dp",
                            initialValue = settingsViewModel.settings.value.cornerRadius.toFloat(),
                            valueRange = 5f..35f,
                            cornerRadius = settingsViewModel.settings.value.cornerRadius,
                            onValueChange = { newValue ->
                                settingsViewModel.update(
                                    settingsViewModel.settings.value.copy(cornerRadius = newValue.toInt())
                                )
                            },
                            onDismiss = { onExit() }
                        ) { currentValue ->
                            @Composable
                            fun Example(shape: RoundedCornerShape) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(32.dp, 3.dp, 32.dp, 1.dp)
                                        .height(62.dp)
                                        .background(
                                            shape = shape,
                                            color = MaterialTheme.colorScheme.surfaceContainerHigh
                                        )
                                )
                            }
                            Example(shapeManager(radius = currentValue.toInt(), isFirst = true))
                            Example(shapeManager(radius = currentValue.toInt()))
                            Example(shapeManager(radius = currentValue.toInt(), isLast = true))
                        }
                    }
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = if (settingsViewModel.settings.value.viewMode) stringResource(id = R.string.grid_view) else stringResource(
                        id = R.string.column_view
                    ),
                    icon = if (settingsViewModel.settings.value.viewMode) IconResource.Vector(Icons.Rounded.GridView) else IconResource.Vector(Icons.Rounded.ViewAgenda),
                    description = stringResource(id = R.string.view_style_description),
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius),
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.viewMode,
                    switchEnabled = {
                        settingsViewModel.update(
                            settingsViewModel.settings.value.copy(
                                viewMode = it
                            )
                        )
                    }
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(R.string.folder_tag),
                    description = stringResource(R.string.description_folder_tag),
                    icon = IconResource.Vector(Icons.AutoMirrored.Rounded.Label),
                    radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius),
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.showFolderIndicator,
                    switchEnabled = {
                        settingsViewModel.update(
                            settingsViewModel.settings.value.copy(
                                showFolderIndicator = it
                            )
                        )
                    }
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = if (settingsViewModel.settings.value.sortDescending) stringResource(id = R.string.sort_descending) else stringResource(
                        id = R.string.sort_ascending
                    ),
                    description = stringResource(id = R.string.sort_description),
                    icon = IconResource.Vector(Icons.AutoMirrored.Rounded.Sort),
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isLast = true
                    ),
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.sortDescending,
                    switchEnabled = {
                        settingsViewModel.update(
                            settingsViewModel.settings.value.copy(
                                sortDescending = it
                            )
                        )
                    }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.make_search_bar_longer),
                    description = stringResource(id = R.string.make_search_bar_longer_description),
                    icon = IconResource.Vector(Icons.Rounded.Search),
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isFirst = true
                    ),
                    actionType = ActionType.SWITCH,
                    variable = settingsViewModel.settings.value.makeSearchBarLonger,
                    switchEnabled = {
                        settingsViewModel.update(
                            settingsViewModel.settings.value.copy(
                                makeSearchBarLonger = it
                            )
                        )
                    }
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.font_size),
                    description = stringResource(id = R.string.font_size_description),
                    icon = IconResource.Vector(Icons.Rounded.FormatSize),
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isLast = true
                    ),
                    actionType = ActionType.CUSTOM,
                    customAction = { onExit ->
                        SettingsSliderDialog(
                            title = stringResource(id = R.string.font_size),
                            valueLabelText = stringResource(id = R.string.font_size_value, settingsViewModel.settings.value.fontSize),
                            initialValue = settingsViewModel.settings.value.fontSize.toFloat(),
                            valueRange = 12f..18f,
                            cornerRadius = settingsViewModel.settings.value.cornerRadius,
                            onValueChange = { newValue ->
                                settingsViewModel.update(
                                    settingsViewModel.settings.value.copy(fontSize = newValue.toInt())
                                )
                            },
                            onDismiss = { onExit() }
                        ) { currentValue ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                                        shape = RoundedCornerShape(settingsViewModel.settings.value.cornerRadius / 2)
                                    )
                                    .padding(16.dp)
                            ) {
                                Text(
                                    text = stringResource(id = R.string.font_size_preview_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                Text(
                                    text = stringResource(id = R.string.font_size_preview_text),
                                    fontSize = currentValue.toInt().sp,
                                    fontWeight = FontWeight.Normal,
                                    modifier = Modifier.padding(vertical = 8.dp)
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsSliderDialog(
    title: String,
    valueLabelText: String,
    initialValue: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    cornerRadius: Int,
    onValueChange: (Float) -> Unit,
    onDismiss: () -> Unit,
    previewContent: @Composable (currentValue: Float) -> Unit
) {
    var sliderPosition by remember { mutableFloatStateOf(initialValue) }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(cornerRadius / 3)
                )
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 8.dp)
            )

            Text(
                text = valueLabelText,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            )

            previewContent(sliderPosition)

            Slider(
                value = sliderPosition,
                onValueChange = {
                    sliderPosition = it
                    onValueChange(it)
                },
                valueRange = valueRange,
                steps = (valueRange.endInclusive - valueRange.start).toInt() - 1,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                colors = SliderDefaults.colors(inactiveTrackColor = MaterialTheme.colorScheme.surfaceContainerHigh)
            )

            // Onay Butonu
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp, top = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(cornerRadius / 2)
                        )
                        .clickable { onDismiss() }
                        .padding(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = stringResource(id = R.string.agree),
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}