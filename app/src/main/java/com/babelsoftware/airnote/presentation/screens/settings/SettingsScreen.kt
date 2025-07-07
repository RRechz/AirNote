package com.babelsoftware.airnote.presentation.screens.settings

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForwardIos
import androidx.compose.material.icons.rounded.Cloud
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.CurrencyBitcoin
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material.icons.rounded.Work
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.babelsoftware.airnote.constant.ConnectionConst
import com.babelsoftware.airnote.presentation.components.NavigationIcon
import com.babelsoftware.airnote.presentation.components.NotesScaffold
import com.babelsoftware.airnote.presentation.components.TitleText
import com.babelsoftware.airnote.presentation.navigation.NavRoutes
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.presentation.screens.settings.settings.shapeManager
import com.babelsoftware.airnote.presentation.screens.settings.widgets.ActionType
import com.babelsoftware.airnote.presentation.screens.settings.widgets.SettingCategory
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.presentation.screens.settings.model.IconResource
import com.babelsoftware.airnote.presentation.screens.settings.widgets.SettingsBox

@Composable
fun SettingsScaffold(
    settingsViewModel: SettingsViewModel,
    title: String,
    onBackNavClicked: () -> Unit,
    snackbarHost: @Composable () -> Unit = {},
    content: @Composable () -> Unit
) {
    NotesScaffold(
        snackbarHost = snackbarHost,
        topBar = {
            key(settingsViewModel.settings.value) {
                TopBar(title, onBackNavClicked)
            }
        },
        content = {
            Box(Modifier.padding(16.dp, 8.dp, 16.dp)) {
                content()
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    onBackNavClicked: () -> Unit,
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        title = {
            TitleText(titleText = title)
        },
        navigationIcon = { NavigationIcon { onBackNavClicked() } }
    )
}

@Composable
fun MainSettings(settingsViewModel: SettingsViewModel, navController: NavController) {
    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = stringResource(id = R.string.screen_settings),
        onBackNavClicked = { navController.navigateUp() }
    ) {
        LazyColumn {
            item {
                SettingCategory(
                    smallSetting = true,
                    title = stringResource(id = R.string.support),
                    subTitle = stringResource(id = R.string.support_description),
                    icon = Icons.AutoMirrored.Rounded.ArrowForwardIos,
                    shape = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isBoth = true
                    ),
                    isLast = true,
                    composableAction = { onExit ->
                        BottomModal(
                            navController = navController,
                            settingsViewModel = settingsViewModel
                        ) { onExit() }
                    },
                    settingsViewModel = settingsViewModel
                )
            }
            item {
                SettingCategory(
                    title = stringResource(id = R.string.color_styles),
                    subTitle = stringResource(R.string.description_color_styles),
                    icon = Icons.Rounded.Palette,
                    shape = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isFirst = true
                    ),
                    action = { navController.navigate(NavRoutes.ColorStyles.route) },
                    settingsViewModel = settingsViewModel
                )
            }
            item {
                SettingCategory(
                    title = stringResource(id = R.string.Behavior),
                    subTitle = stringResource(id = R.string.description_markdown),
                    icon = Icons.Rounded.TextFields,
                    shape = shapeManager(radius = settingsViewModel.settings.value.cornerRadius),
                    action = { navController.navigate(NavRoutes.Markdown.route) },
                    settingsViewModel = settingsViewModel
                )
            }
            item {
                SettingCategory(
                    title = stringResource(R.string.airnote_ai),
                    subTitle = stringResource(R.string.airnote_ai_description),
                    icon = Icons.Rounded.AutoAwesome,
                    shape = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius
                    ),
                    action = { navController.navigate(NavRoutes.AirNoteAiSettings.route) },
                    settingsViewModel = settingsViewModel
                )
            }
            item {
                SettingCategory(
                    title = stringResource(id = R.string.language),
                    subTitle = stringResource(R.string.description_language),
                    icon = Icons.Rounded.Language,
                    isLast = true,
                    shape = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isLast = true
                    ),
                    action = { navController.navigate(NavRoutes.Language.route) },
                    settingsViewModel = settingsViewModel
                )
            }
            item {
                SettingCategory(
                    title = stringResource(id = R.string.backup),
                    subTitle = stringResource(R.string.description_cloud),
                    icon = Icons.Rounded.Cloud,
                    shape = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isFirst = true
                    ),
                    action = { navController.navigate(NavRoutes.Cloud.route) },
                    settingsViewModel = settingsViewModel
                )
            }
            item {
                SettingCategory(
                    title = stringResource(id = R.string.privacy),
                    subTitle = stringResource(id = R.string.screen_protection),
                    icon = ImageVector.vectorResource(id = R.drawable.incognito_fill),
                    shape = shapeManager(radius = settingsViewModel.settings.value.cornerRadius),
                    settingsViewModel = settingsViewModel,
                    action = { navController.navigate(NavRoutes.Privacy.route) }
                )
            }
            item {
                SettingCategory(
                    title = stringResource(id = R.string.tools),
                    subTitle = stringResource(R.string.description_tools),
                    icon = Icons.Rounded.Work,
                    shape = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isLast = true
                    ),
                    isLast = true,
                    action = { navController.navigate(NavRoutes.Tools.route) },
                    settingsViewModel = settingsViewModel
                )
            }
            item {
                val aboutSubtitle = if (settingsViewModel.updateAvailable.value) {
                    stringResource(R.string.update_available_description)
                } else {
                    stringResource(R.string.description_about)
                }
                SettingCategory(
                    title = stringResource(id = R.string.about),
                    subTitle = aboutSubtitle,
                    icon = Icons.Rounded.Info,
                    shape = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        isBoth = true
                    ),
                    action = { navController.navigate(NavRoutes.About.route) },
                    settingsViewModel = settingsViewModel
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomModal(navController: NavController, settingsViewModel: SettingsViewModel, onExit: () -> Unit) {
    val uriHandler = LocalUriHandler.current

    ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        onDismissRequest = { onExit() }
    ) {
        Column(
            modifier = Modifier.padding(20.dp, 0.dp, 20.dp, 20.dp)
        ) {
            SettingsBox(
                size = 8.dp,
                title = "Buy Me A Coffee",
                icon = IconResource.Vector(Icons.Rounded.Coffee),
                isCentered = true,
                actionType = ActionType.CUSTOM,
                radius = shapeManager(
                    isFirst = true,
                    radius = settingsViewModel.settings.value.cornerRadius
                ),
                customAction = { uriHandler.openUri(ConnectionConst.SUPPORT_BUYMEACOFFEE) },
                settingsViewModel = settingsViewModel
            )
            SettingsBox(
                title = stringResource(R.string.cryptocurrency),
                size = 8.dp,
                icon = IconResource.Vector(Icons.Rounded.CurrencyBitcoin),
                isCentered = true,
                actionType = ActionType.CUSTOM,
                radius = shapeManager(
                    radius = settingsViewModel.settings.value.cornerRadius,
                    isLast = true
                ),
                customAction = { LaunchedEffect(true) { navController.navigate(NavRoutes.Support.route) } },
                settingsViewModel = settingsViewModel
            )
        }
    }
}