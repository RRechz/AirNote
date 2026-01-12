package com.babelsoftware.loudly.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.FolderCopy
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.babelsoftware.loudly.LocalPlayerAwareWindowInsets
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.AutoSyncLocalSongsKey
import com.babelsoftware.loudly.constants.FlatSubfoldersKey
import com.babelsoftware.loudly.constants.ScannerSensitivity
import com.babelsoftware.loudly.constants.ScannerSensitivityKey
import com.babelsoftware.loudly.constants.ScannerStrictExtKey
import com.babelsoftware.loudly.ui.component.IconButton
import com.babelsoftware.loudly.ui.screens.settings.card_design.ActionType
import com.babelsoftware.loudly.ui.screens.settings.card_design.IconResource
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingCategory
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingsBox
import com.babelsoftware.loudly.ui.screens.settings.card_design.shapeManager
import com.babelsoftware.loudly.ui.utils.backToMain
import com.babelsoftware.loudly.utils.rememberEnumPreference
import com.babelsoftware.loudly.utils.rememberPreference

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalPlayerSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val (autoSyncLocalSongs, onAutoSyncLocalSongs) = rememberPreference(key = AutoSyncLocalSongsKey, defaultValue = true)
    val (scannerSensitivity, onScannerSensitivityChange) = rememberEnumPreference(key = ScannerSensitivityKey, defaultValue = ScannerSensitivity.LEVEL_2)
    val (strictExtensions, onStrictExtensionsChange) = rememberPreference(ScannerStrictExtKey, defaultValue = false)
    val (flatSubfolders, onFlatSubfoldersChange) = rememberPreference(FlatSubfoldersKey, defaultValue = true)

    var showSensitivityDialog by remember { mutableStateOf(false) }

    if (showSensitivityDialog) {
        SelectionListDialog(
            title = stringResource(R.string.scanner_sensitivity_title),
            items = ScannerSensitivity.values().toList(),
            itemText = {
                when (it) {
                    ScannerSensitivity.LEVEL_1 -> stringResource(R.string.scanner_sensitivity_L1)
                    ScannerSensitivity.LEVEL_2 -> stringResource(R.string.scanner_sensitivity_L2)
                    ScannerSensitivity.LEVEL_3 -> stringResource(R.string.scanner_sensitivity_L3)
                }
            },
            selectedItem = scannerSensitivity,
            onItemSelected = {
                onScannerSensitivityChange(it)
                showSensitivityDialog = false
            },
            onDismissRequest = { showSensitivityDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.local_player_settings_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.arrow_back),
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
            SettingCategory(title = stringResource(R.string.scanner_settings_title))
            SettingsBox(
                title = stringResource(R.string.auto_scanner_title),
                description = stringResource(R.string.auto_scanner_description),
                icon = IconResource.Drawable(painterResource(R.drawable.sync)),
                actionType = ActionType.SWITCH,
                isChecked = autoSyncLocalSongs,
                onCheckedChange = onAutoSyncLocalSongs,
                shape = shapeManager(isFirst = true)
            )
            SettingsBox(
                title = stringResource(R.string.scanner_sensitivity_title),
                description = when (scannerSensitivity) {
                    ScannerSensitivity.LEVEL_1 -> stringResource(R.string.scanner_sensitivity_L1)
                    ScannerSensitivity.LEVEL_2 -> stringResource(R.string.scanner_sensitivity_L2)
                    ScannerSensitivity.LEVEL_3 -> stringResource(R.string.scanner_sensitivity_L3)
                },
                icon = IconResource.Vector(Icons.Rounded.GraphicEq),
                onClick = { showSensitivityDialog = true },
                shape = shapeManager()
            )
            SettingsBox(
                title = stringResource(R.string.scanner_strict_file_name_title),
                description = stringResource(R.string.scanner_strict_file_name_description),
                icon = IconResource.Vector(Icons.Rounded.TextFields),
                actionType = ActionType.SWITCH,
                isChecked = strictExtensions,
                onCheckedChange = onStrictExtensionsChange,
                shape = shapeManager(isLast = true)
            )

            SettingCategory(title = stringResource(R.string.folders_settings_title))
            SettingsBox(
                title = stringResource(R.string.flat_subfolders_title),
                description = stringResource(R.string.flat_subfolders_description),
                icon = IconResource.Vector(Icons.Rounded.FolderCopy),
                actionType = ActionType.SWITCH,
                isChecked = flatSubfolders,
                onCheckedChange = onFlatSubfoldersChange,
                shape = shapeManager(isBoth = true)
            )
        }
    }
}

@Composable
private fun <T> SelectionListDialog(
    title: String,
    items: List<T>,
    itemText: @Composable (T) -> String,
    selectedItem: T,
    onItemSelected: (T) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(text = title) },
        text = {
            LazyColumn {
                items(items) { item ->
                    val isSelected = item == selectedItem
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onItemSelected(item) }
                            .padding(vertical = 14.dp)
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onItemSelected(item) }
                        )
                        Spacer(Modifier.width(16.dp))
                        Text(
                            text = itemText(item),
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismissRequest) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}