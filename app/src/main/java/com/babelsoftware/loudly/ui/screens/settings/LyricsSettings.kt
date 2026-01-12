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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.rounded.ContentCut
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.TextFields
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toLowerCase
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.babelsoftware.loudly.LocalPlayerAwareWindowInsets
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.EnableKugouKey
import com.babelsoftware.loudly.constants.EnableLrcLibKey
import com.babelsoftware.loudly.constants.LyricFontSizeKey
import com.babelsoftware.loudly.constants.LyricTrimKey
import com.babelsoftware.loudly.constants.LyricsTextPositionKey
import com.babelsoftware.loudly.constants.MultilineLrcKey
import com.babelsoftware.loudly.constants.PreferredLyricsProvider
import com.babelsoftware.loudly.constants.PreferredLyricsProviderKey
import com.babelsoftware.loudly.ui.component.CounterDialog
import com.babelsoftware.loudly.ui.component.IconButton
import com.babelsoftware.loudly.ui.screens.settings.card_design.ActionType
import com.babelsoftware.loudly.ui.screens.settings.card_design.IconResource
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingCategory
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingsBox
import com.babelsoftware.loudly.ui.screens.settings.card_design.shapeManager
import com.babelsoftware.loudly.ui.utils.backToMain
import com.babelsoftware.loudly.utils.rememberEnumPreference
import com.babelsoftware.loudly.utils.rememberPreference
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LyricsSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val (enableKugou, onEnableKugouChange) = rememberPreference(key = EnableKugouKey, defaultValue = true)
    val (lyricsPosition, onLyricsPositionChange) = rememberEnumPreference(LyricsTextPositionKey, defaultValue = LyricsPosition.CENTER)
    val (enableLrcLib, onEnableLrcLibChange) = rememberPreference(key = EnableLrcLibKey, defaultValue = true)
    val (preferredProvider, onPreferredProviderChange) = rememberEnumPreference(key = PreferredLyricsProviderKey, defaultValue = PreferredLyricsProvider.LRCLIB)
    val (multilineLrc, onMultilineLrcChange) = rememberPreference(MultilineLrcKey, defaultValue = true)
    val (lyricTrim, onLyricTrimChange) = rememberPreference(LyricTrimKey, defaultValue = false)
    val (lyricFontSize, onLyricFontSizeChange) = rememberPreference(LyricFontSizeKey, defaultValue = 20)

    var showFontSizeDialog by remember { mutableStateOf(false) }
    var showLyricsPositionDialog by remember { mutableStateOf(false) }
    val providerSheetState = rememberModalBottomSheetState()
    var showProviderSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()


    if (showFontSizeDialog) {
        CounterDialog(
            title = stringResource(R.string.lyrics_font_size),
            initialValue = lyricFontSize,
            upperBound = 28,
            lowerBound = 10,
            resetValue = 20,
            unitDisplay = " sp",
            onDismiss = { showFontSizeDialog = false },
            onConfirm = {
                onLyricFontSizeChange(it)
                showFontSizeDialog = false
            },
            onCancel = { showFontSizeDialog = false },
            onReset = { onLyricFontSizeChange(20) },
        )
    }

    if (showLyricsPositionDialog) {
        SelectionListDialog(
            title = stringResource(R.string.lyrics_text_position),
            items = LyricsPosition.values().toList(),
            itemText = {
                when (it) {
                    LyricsPosition.LEFT -> stringResource(R.string.left)
                    LyricsPosition.CENTER -> stringResource(R.string.center)
                    LyricsPosition.RIGHT -> stringResource(R.string.right)
                }
            },
            selectedItem = lyricsPosition,
            onItemSelected = {
                onLyricsPositionChange(it)
                showLyricsPositionDialog = false
            },
            onDismissRequest = { showLyricsPositionDialog = false }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.lyrics_settings_title)) },
                navigationIcon = {
                    IconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain
                    ) {
                        Icon(
                            Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = null
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->

        if (showProviderSheet) {
            ModalBottomSheet(
                onDismissRequest = { showProviderSheet = false },
                sheetState = providerSheetState
            ) {
                Column(modifier = Modifier.padding(bottom = 32.dp)) {
                    Text(
                        text = stringResource(R.string.default_lyrics_provider),
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
                    )
                    PreferredLyricsProvider.values().forEach { provider ->
                        val isSelected = preferredProvider == provider
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onPreferredProviderChange(provider)
                                    scope
                                        .launch { providerSheetState.hide() }
                                        .invokeOnCompletion {
                                            if (!providerSheetState.isVisible) {
                                                showProviderSheet = false
                                            }
                                        }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            RadioButton(selected = isSelected, onClick = null)
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = provider.name.toLowerCase(Locale.current).capitalize(Locale.current),
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            SettingCategory(title = stringResource(R.string.main))
            SettingsBox(
                title = stringResource(R.string.enable_kugou),
                icon = IconResource.Vector(Icons.Rounded.Lyrics),
                actionType = ActionType.SWITCH,
                isChecked = enableKugou,
                onCheckedChange = onEnableKugouChange,
                shape = shapeManager(isFirst = true)
            )
            SettingsBox(
                title = stringResource(R.string.enable_lrclib),
                icon = IconResource.Vector(Icons.Rounded.Lyrics),
                actionType = ActionType.SWITCH,
                isChecked = enableLrcLib,
                onCheckedChange = onEnableLrcLibChange,
                shape = shapeManager()
            )
            SettingsBox(
                title = stringResource(R.string.lyrics_multiline_title),
                description = stringResource(R.string.lyrics_multiline_description),
                icon = IconResource.Vector(Icons.AutoMirrored.Rounded.Sort),
                actionType = ActionType.SWITCH,
                isChecked = multilineLrc,
                onCheckedChange = onMultilineLrcChange,
                shape = shapeManager()
            )
            SettingsBox(
                title = stringResource(R.string.lyrics_trim_title),
                icon = IconResource.Vector(Icons.Rounded.ContentCut),
                actionType = ActionType.SWITCH,
                isChecked = lyricTrim,
                onCheckedChange = onLyricTrimChange,
                shape = shapeManager()
            )
            SettingsBox(
                title = stringResource(R.string.lyrics_text_position),
                description = when (lyricsPosition) {
                    LyricsPosition.LEFT -> stringResource(R.string.left)
                    LyricsPosition.CENTER -> stringResource(R.string.center)
                    LyricsPosition.RIGHT -> stringResource(R.string.right)
                },
                icon = IconResource.Vector(Icons.Rounded.Lyrics),
                onClick = { showLyricsPositionDialog = true },
                shape = shapeManager()
            )
            SettingsBox(
                title = stringResource(R.string.lyrics_font_size),
                description = "$lyricFontSize sp",
                icon = IconResource.Vector(Icons.Rounded.TextFields),
                onClick = { showFontSizeDialog = true },
                shape = shapeManager(isLast = true)
            )

            SettingCategory(title = stringResource(R.string.misc))
            SettingsBox(
                title = stringResource(R.string.default_lyrics_provider),
                description = preferredProvider.name.toLowerCase(Locale.current).capitalize(Locale.current),
                onClick = { showProviderSheet = true },
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