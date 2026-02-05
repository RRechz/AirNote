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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeOff
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.BluetoothConnected
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Lyrics
import androidx.compose.material.icons.rounded.SdCard
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Tune
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.babelsoftware.innertube.utils.parseCookieString
import com.babelsoftware.loudly.LocalPlayerAwareWindowInsets
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.AddingPlayedSongsToYTMHistoryKey
import com.babelsoftware.loudly.constants.AudioNormalizationKey
import com.babelsoftware.loudly.constants.AudioOffload
import com.babelsoftware.loudly.constants.AudioQuality
import com.babelsoftware.loudly.constants.AudioQualityKey
import com.babelsoftware.loudly.constants.AutoLoadMoreKey
import com.babelsoftware.loudly.constants.AutoPlaySongWhenBluetoothDeviceConnectedKey
import com.babelsoftware.loudly.constants.AutoSkipNextOnErrorKey
import com.babelsoftware.loudly.constants.InnerTubeCookieKey
import com.babelsoftware.loudly.constants.PersistentQueueKey
import com.babelsoftware.loudly.constants.SkipSilenceKey
import com.babelsoftware.loudly.constants.StopMusicOnTaskClearKey
import com.babelsoftware.loudly.constants.StopPlayingSongWhenMinimumVolumeKey
import com.babelsoftware.loudly.constants.minPlaybackDurKey
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
fun PlayerSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val (audioQuality, onAudioQualityChange) = rememberEnumPreference(AudioQualityKey, defaultValue = AudioQuality.AUTO)
    val (persistentQueue, onPersistentQueueChange) = rememberPreference(PersistentQueueKey, defaultValue = true)
    val (skipSilence, onSkipSilenceChange) = rememberPreference(SkipSilenceKey, defaultValue = false)
    val (audioNormalization, onAudioNormalizationChange) = rememberPreference(AudioNormalizationKey, defaultValue = true)
    val (autoPlaySongWhenBluetoothDeviceConnected, onAutoPlaySongWhenBluetoothDeviceConnectedChange) = rememberPreference(AutoPlaySongWhenBluetoothDeviceConnectedKey, defaultValue = true)
    val (stopPlayingSongWhenMinimumVolume, onStopPlayingSongWhenMinimumVolumeChange) = rememberPreference(StopPlayingSongWhenMinimumVolumeKey, defaultValue = true)
    val (autoSkipNextOnError, onAutoSkipNextOnErrorChange) = rememberPreference(AutoSkipNextOnErrorKey, defaultValue = false)
    val (stopMusicOnTaskClear, onStopMusicOnTaskClearChange) = rememberPreference(StopMusicOnTaskClearKey, defaultValue = false)
    val (autoLoadMore, onAutoLoadMoreChange) = rememberPreference(AutoLoadMoreKey, defaultValue = true)
    val (minPlaybackDur, onMinPlaybackDurChange) = rememberPreference(minPlaybackDurKey, defaultValue = 30)
    val (audioOffload, onAudioOffloadChange) = rememberPreference(key = AudioOffload, defaultValue = false)
    val (addingPlayedSongsToYtmHistory, onAddingPlayedSongsToYtmHistoryChange) = rememberPreference(AddingPlayedSongsToYTMHistoryKey, defaultValue = true)
    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) { "SAPISID" in parseCookieString(innerTubeCookie) }
    var showMinPlaybackDurDialog by remember { mutableStateOf(false) }
    var showAudioQualityDialog by remember { mutableStateOf(false) }
    val miscSheetState = rememberModalBottomSheetState()
    var showMiscSheet by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()


    if (showMinPlaybackDurDialog) {
        CounterDialog(
            title = stringResource(R.string.minimum_playback_duration),
            description = stringResource(R.string.minimum_playback_duration_info),
            initialValue = minPlaybackDur,
            upperBound = 100,
            lowerBound = 0,
            resetValue = 30,
            unitDisplay = "%",
            onDismiss = { showMinPlaybackDurDialog = false },
            onConfirm = {
                onMinPlaybackDurChange(it)
                showMinPlaybackDurDialog = false
            },
            onCancel = { showMinPlaybackDurDialog = false },
            onReset = { onMinPlaybackDurChange(30) },
        )
    }

    if (showAudioQualityDialog) {
        AudioQualitySelectionDialog(
            selectedValue = audioQuality,
            onValueSelected = {
                onAudioQualityChange(it)
                showAudioQualityDialog = false
            },
            onDismissRequest = { showAudioQualityDialog = false }
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.player_and_audio)) },
                navigationIcon = {
                    IconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain
                    ) {
                        Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->

        if (showMiscSheet) {
            ModalBottomSheet(
                onDismissRequest = { showMiscSheet = false },
                sheetState = miscSheetState,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).padding(bottom = 24.dp)) {
                    SettingCategory(title = stringResource(R.string.misc))
                    SettingsBox(
                        title = stringResource(R.string.audio_offload),
                        description = stringResource(R.string.audio_offload_description),
                        icon = IconResource.Vector(Icons.Rounded.Bolt),
                        actionType = ActionType.SWITCH,
                        isChecked = audioOffload,
                        onCheckedChange = onAudioOffloadChange,
                        shape = shapeManager(isFirst = true)
                    )
                    Spacer(modifier = Modifier.padding(top=2.dp))
                    SettingsBox(
                        title = stringResource(R.string.stop_music_on_task_clear),
                        icon = IconResource.Drawable(painterResource(R.drawable.clear_all)),
                        actionType = ActionType.SWITCH,
                        isChecked = stopMusicOnTaskClear,
                        onCheckedChange = onStopMusicOnTaskClearChange,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom))
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp)
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                SettingCategory(title = stringResource(R.string.player))
                SettingsBox(
                    title = stringResource(R.string.audio_quality),
                    description = when (audioQuality) {
                        AudioQuality.AUTO -> stringResource(R.string.audio_quality_auto)
                        AudioQuality.MAX -> stringResource(R.string.audio_quality_max)
                        AudioQuality.HIGH -> stringResource(R.string.audio_quality_high)
                        AudioQuality.LOW -> stringResource(R.string.audio_quality_low)
                    },
                    icon = IconResource.Drawable(painterResource(R.drawable.graphic_eq)),
                    shape = shapeManager(isFirst = true),
                    onClick = { showAudioQualityDialog = true }
                )
                SettingsBox(
                    title = stringResource(R.string.lyrics_settings_title),
                    icon = IconResource.Vector(Icons.Rounded.Lyrics),
                    shape = shapeManager(),
                    onClick = { navController.navigate("settings/player/lyrics") }
                )
                SettingsBox(
                    title = stringResource(R.string.local_player_settings_title),
                    icon = IconResource.Vector(Icons.Rounded.SdCard),
                    shape = shapeManager(),
                    onClick = { navController.navigate("player/local") }
                )
                SettingsBox(
                    title = stringResource(R.string.minimum_playback_duration),
                    description = "$minPlaybackDur %",
                    icon = IconResource.Vector(Icons.Rounded.Sync),
                    shape = shapeManager(),
                    onClick = { showMinPlaybackDurDialog = true }
                )
                SettingsBox(
                    title = stringResource(R.string.skip_silence),
                    icon = IconResource.Drawable(painterResource(R.drawable.fast_forward)),
                    actionType = ActionType.SWITCH,
                    isChecked = skipSilence,
                    onCheckedChange = onSkipSilenceChange,
                    shape = shapeManager()
                )
                if (isLoggedIn) {
                    SettingsBox(
                        title = stringResource(R.string.adding_played_songs_to_ytm_history),
                        icon = IconResource.Drawable(painterResource(R.drawable.history)),
                        actionType = ActionType.SWITCH,
                        isChecked = addingPlayedSongsToYtmHistory,
                        onCheckedChange = onAddingPlayedSongsToYtmHistoryChange,
                        shape = shapeManager()
                    )
                }
                SettingsBox(
                    title = stringResource(R.string.audio_normalization),
                    icon = IconResource.Vector(Icons.AutoMirrored.Rounded.VolumeUp),
                    actionType = ActionType.SWITCH,
                    isChecked = audioNormalization,
                    onCheckedChange = onAudioNormalizationChange,
                    shape = shapeManager()
                )
                SettingsBox(
                    title = stringResource(R.string.play_song_when_bluetooth_device_connected),
                    icon = IconResource.Vector(Icons.Rounded.BluetoothConnected),
                    actionType = ActionType.SWITCH,
                    isChecked = autoPlaySongWhenBluetoothDeviceConnected,
                    onCheckedChange = onAutoPlaySongWhenBluetoothDeviceConnectedChange,
                    shape = shapeManager()
                )
                SettingsBox(
                    title = stringResource(R.string.stop_playing_when_song_sound_minimum_volume),
                    icon = IconResource.Vector(Icons.AutoMirrored.Rounded.VolumeOff),
                    actionType = ActionType.SWITCH,
                    isChecked = stopPlayingSongWhenMinimumVolume,
                    onCheckedChange = onStopPlayingSongWhenMinimumVolumeChange,
                    shape = shapeManager(isLast = true)
                )

                SettingCategory(title = stringResource(R.string.queue))
                SettingsBox(
                    title = stringResource(R.string.persistent_queue),
                    description = stringResource(R.string.persistent_queue_desc),
                    icon = IconResource.Drawable(painterResource(R.drawable.queue_music)),
                    actionType = ActionType.SWITCH,
                    isChecked = persistentQueue,
                    onCheckedChange = onPersistentQueueChange,
                    shape = shapeManager(isFirst = true)
                )
                SettingsBox(
                    title = stringResource(R.string.auto_load_more),
                    description = stringResource(R.string.auto_load_more_desc),
                    icon = IconResource.Drawable(painterResource(R.drawable.playlist_add)),
                    actionType = ActionType.SWITCH,
                    isChecked = autoLoadMore,
                    onCheckedChange = onAutoLoadMoreChange,
                    shape = shapeManager()
                )
                SettingsBox(
                    title = stringResource(R.string.auto_skip_next_on_error),
                    description = stringResource(R.string.auto_skip_next_on_error_desc),
                    icon = IconResource.Drawable(painterResource(R.drawable.skip_next)),
                    actionType = ActionType.SWITCH,
                    isChecked = autoSkipNextOnError,
                    onCheckedChange = onAutoSkipNextOnErrorChange,
                    shape = shapeManager(isLast = true)
                )

                SettingCategory(title = stringResource(R.string.advanced))
                SettingsBox(
                    title = stringResource(R.string.other_settings),
                    description = stringResource(R.string.other_settings_description),
                    icon = IconResource.Vector(Icons.Rounded.Tune),
                    shape = shapeManager(isBoth = true),
                    onClick = {
                        scope.launch {
                            showMiscSheet = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun AudioQualitySelectionDialog(
    selectedValue: AudioQuality,
    onValueSelected: (AudioQuality) -> Unit,
    onDismissRequest: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismissRequest,
        title = { Text(stringResource(R.string.audio_quality)) },
        text = {
            Column {
                AudioQuality.values().forEach { quality ->
                    val isSelected = selectedValue == quality
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onValueSelected(quality) }
                            .padding(vertical = 8.dp)
                    ) {
                        RadioButton(selected = isSelected, onClick = { onValueSelected(quality) })
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text(
                                text = when (quality) {
                                    AudioQuality.AUTO -> stringResource(R.string.audio_quality_auto)
                                    AudioQuality.MAX -> stringResource(R.string.audio_quality_max)
                                    AudioQuality.HIGH -> stringResource(R.string.audio_quality_high)
                                    AudioQuality.LOW -> stringResource(R.string.audio_quality_low)
                                },
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = when (quality) {
                                    AudioQuality.AUTO -> stringResource(R.string.audio_quality_auto_desc)
                                    AudioQuality.MAX -> stringResource(R.string.audio_quality_max_desc)
                                    AudioQuality.HIGH -> stringResource(R.string.audio_quality_high_desc)
                                    AudioQuality.LOW -> stringResource(R.string.audio_quality_low_desc)
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
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