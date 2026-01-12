package com.babelsoftware.loudly.ui.screens.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.babelsoftware.loudly.LocalDatabase
import com.babelsoftware.loudly.LocalPlayerAwareWindowInsets
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.DisableScreenshotKey
import com.babelsoftware.loudly.constants.PauseListenHistoryKey
import com.babelsoftware.loudly.constants.PauseSearchHistoryKey
import com.babelsoftware.loudly.ui.component.DefaultDialog
import com.babelsoftware.loudly.ui.component.IconButton
import com.babelsoftware.loudly.ui.screens.settings.card_design.ActionType
import com.babelsoftware.loudly.ui.screens.settings.card_design.IconResource
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingCategory
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingsBox
import com.babelsoftware.loudly.ui.screens.settings.card_design.shapeManager
import com.babelsoftware.loudly.ui.utils.backToMain
import com.babelsoftware.loudly.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacySettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val database = LocalDatabase.current
    val (pauseListenHistory, onPauseListenHistoryChange) = rememberPreference(
        key = PauseListenHistoryKey,
        defaultValue = false
    )
    val (pauseSearchHistory, onPauseSearchHistoryChange) = rememberPreference(
        key = PauseSearchHistoryKey,
        defaultValue = false
    )
    val (disableScreenshot, onDisableScreenshotChange) = rememberPreference(
        key = DisableScreenshotKey,
        defaultValue = false
    )

    var showClearListenHistoryDialog by remember { mutableStateOf(false) }
    var showClearSearchHistoryDialog by remember { mutableStateOf(false) }

    if (showClearListenHistoryDialog) {
        DefaultDialog(
            onDismiss = { showClearListenHistoryDialog = false },
            title = { Text(stringResource(R.string.clear_listen_history)) },
            content = {
                Text(
                    text = stringResource(R.string.clear_listen_history_confirm),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            buttons = {
                TextButton(
                    onClick = { showClearListenHistoryDialog = false }
                ) {
                    Text(text = stringResource(R.string.cancel))
                }

                TextButton(
                    onClick = {
                        database.query { clearListenHistory() }
                        showClearListenHistoryDialog = false
                    }
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            }
        )
    }

    if (showClearSearchHistoryDialog) {
        DefaultDialog(
            onDismiss = { showClearSearchHistoryDialog = false },
            title = { Text(stringResource(R.string.clear_search_history)) },
            content = {
                Text(
                    text = stringResource(R.string.clear_search_history_confirm),
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            buttons = {
                TextButton(
                    onClick = { showClearSearchHistoryDialog = false }
                ) {
                    Text(text = stringResource(R.string.cancel))
                }
                TextButton(
                    onClick = {
                        database.query { clearSearchHistory() }
                        showClearSearchHistoryDialog = false
                    }
                ) {
                    Text(text = stringResource(android.R.string.ok))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.privacy)) },
                navigationIcon = {
                    IconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain
                    ) {
                        Icon(
                            painterResource(R.drawable.arrow_back),
                            contentDescription = null
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            SettingCategory(title = stringResource(R.string.listen_history))
            SettingsBox(
                title = stringResource(R.string.pause_listen_history),
                icon = IconResource.Drawable(painterResource(R.drawable.history)),
                actionType = ActionType.SWITCH,
                isChecked = pauseListenHistory,
                onCheckedChange = onPauseListenHistoryChange,
                shape = shapeManager(isFirst = true)
            )
            SettingsBox(
                title = stringResource(R.string.clear_listen_history),
                icon = IconResource.Drawable(painterResource(R.drawable.delete_history)),
                onClick = { showClearListenHistoryDialog = true },
                shape = shapeManager(isLast = true)
            )

            SettingCategory(title = stringResource(R.string.search_history))
            SettingsBox(
                title = stringResource(R.string.pause_search_history),
                icon = IconResource.Drawable(painterResource(R.drawable.search_off)),
                actionType = ActionType.SWITCH,
                isChecked = pauseSearchHistory,
                onCheckedChange = onPauseSearchHistoryChange,
                shape = shapeManager(isFirst = true)
            )
            SettingsBox(
                title = stringResource(R.string.clear_search_history),
                icon = IconResource.Drawable(painterResource(R.drawable.clear_all)),
                onClick = { showClearSearchHistoryDialog = true },
                shape = shapeManager(isLast = true)
            )

            SettingCategory(title = stringResource(R.string.misc))
            SettingsBox(
                title = stringResource(R.string.disable_screenshot),
                description = stringResource(R.string.disable_screenshot_desc),
                icon = IconResource.Drawable(painterResource(R.drawable.screenshot)),
                actionType = ActionType.SWITCH,
                isChecked = disableScreenshot,
                onCheckedChange = onDisableScreenshotChange,
                shape = shapeManager(isBoth = true)
            )
        }
    }
}