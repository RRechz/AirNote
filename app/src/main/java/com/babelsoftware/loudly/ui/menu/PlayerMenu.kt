@file:Suppress("NAME_SHADOWING")

package com.babelsoftware.loudly.ui.menu

import android.content.Intent
import android.media.audiofx.AudioEffect
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.AddLink
import androidx.compose.material.icons.rounded.Album
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.DownloadDone
import androidx.compose.material.icons.rounded.Equalizer
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PlaylistAdd
import androidx.compose.material.icons.rounded.Radio
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Timer
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.media3.common.PlaybackParameters
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import com.babelsoftware.innertube.YouTube
import com.babelsoftware.loudly.LocalDatabase
import com.babelsoftware.loudly.LocalDownloadUtil
import com.babelsoftware.loudly.LocalPlayerConnection
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.ListItemHeight
import com.babelsoftware.loudly.constants.SliderStyle
import com.babelsoftware.loudly.constants.SliderStyleKey
import com.babelsoftware.loudly.models.MediaMetadata
import com.babelsoftware.loudly.playback.ExoDownloadService
import com.babelsoftware.loudly.ui.component.BottomSheetState
import com.babelsoftware.loudly.ui.component.ListDialog
import com.babelsoftware.loudly.ui.component.PlayerSliderTrack
import com.babelsoftware.loudly.ui.screens.share.SocialShareScreen
import com.babelsoftware.loudly.utils.makeTimeString
import com.babelsoftware.loudly.utils.rememberEnumPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.saket.squiggles.SquigglySlider
import java.time.LocalDateTime
import kotlin.math.log2
import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerMenu(
    mediaMetadata: MediaMetadata?,
    navController: NavController,
    bottomSheetState: BottomSheetState,
    onShowDetailsDialog: () -> Unit = {},
    onDismiss: () -> Unit,
) {
    mediaMetadata ?: return
    val context = LocalContext.current
    val database = LocalDatabase.current
    val downloadUtil = LocalDownloadUtil.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val playerVolume = playerConnection.service.playerVolume.collectAsState()
    val activityResultLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { }
    val librarySong by database.song(mediaMetadata.id).collectAsState(initial = null)
    val coroutineScope = rememberCoroutineScope()
    val download by LocalDownloadUtil.current.getDownload(mediaMetadata.id)
        .collectAsState(initial = null)

    val artists = remember(mediaMetadata.artists) {
        mediaMetadata.artists.filter { it.id != null }
    }

    val sleepTimerEnabled = remember(
        playerConnection.service.sleepTimer.triggerTime,
        playerConnection.service.sleepTimer.pauseWhenSongEnd
    ) {
        playerConnection.service.sleepTimer.isActive
    }

    var sleepTimerTimeLeft by remember {
        mutableLongStateOf(0L)
    }

    LaunchedEffect(librarySong?.song?.liked) {
        librarySong?.let {
            downloadUtil.autoDownloadIfLiked(it.song)
        }
    }

    LaunchedEffect(sleepTimerEnabled) {
        if (sleepTimerEnabled) {
            while (isActive) {
                sleepTimerTimeLeft = if (playerConnection.service.sleepTimer.pauseWhenSongEnd) {
                    playerConnection.player.duration - playerConnection.player.currentPosition
                } else {
                    playerConnection.service.sleepTimer.triggerTime - System.currentTimeMillis()
                }
                delay(1000L)
            }
        }
    }

    var showSocialShareDialog by remember { mutableStateOf(false) }

    if (showSocialShareDialog) {
        SocialShareScreen(
            mediaMetadata = mediaMetadata,
            onDismiss = { showSocialShareDialog = false }
        )
    }

    var showSleepTimerDialog by remember {
        mutableStateOf(false)
    }

    var sleepTimerValue by remember {
        mutableFloatStateOf(30f)
    }

    val sliderStyle by rememberEnumPreference(SliderStyleKey, SliderStyle.DEFAULT)

    if (showSleepTimerDialog) {
        AlertDialog(
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { showSleepTimerDialog = false },
            icon = {
                Icon(
                    painter = painterResource(R.drawable.bedtime),
                    contentDescription = null
                )
            },
            title = { Text(stringResource(R.string.sleep_timer)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showSleepTimerDialog = false
                        playerConnection.service.sleepTimer.start(sleepTimerValue.roundToInt())
                    }
                ) {
                    Text(stringResource(android.R.string.ok))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showSleepTimerDialog = false }
                ) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = pluralStringResource(
                            R.plurals.minute,
                            sleepTimerValue.roundToInt(),
                            sleepTimerValue.roundToInt()
                        ),
                        style = MaterialTheme.typography.bodyLarge
                    )

                    when (sliderStyle) {
                        SliderStyle.DEFAULT -> {
                            Slider(
                                value = sleepTimerValue,
                                onValueChange = { sleepTimerValue = it },
                                valueRange = 5f..120f,
                                steps = (120 - 5) / 5 - 1,
                                thumb = { Spacer(modifier = Modifier.size(0.dp)) },
                                track = { sliderState ->
                                    PlayerSliderTrack(
                                        sliderState = sliderState,
                                        colors = SliderDefaults.colors()
                                    )
                                },
                            )
                        }

                        SliderStyle.SQUIGGLY -> {
                            SquigglySlider(
                                value = sleepTimerValue,
                                onValueChange = { sleepTimerValue = it },
                                valueRange = 5f..120f,
                            )
                        }

                        SliderStyle.COMPOSE -> {
                            Slider(
                                value = sleepTimerValue,
                                onValueChange = { sleepTimerValue = it },
                                valueRange = 5f..120f,
                                steps = (120 - 5) / 5 - 1,
                            )
                        }
                    }

                    OutlinedButton(
                        onClick = {
                            showSleepTimerDialog = false
                            playerConnection.service.sleepTimer.start(-1)
                        }
                    ) {
                        Text(stringResource(R.string.end_of_song))
                    }
                }
            }
        )
    }

    var showChoosePlaylistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    AddToPlaylistDialog(
        navController = navController,
        isVisible = showChoosePlaylistDialog,
        onGetSong = { playlist ->
            database.transaction {
                insert(mediaMetadata)
            }

            coroutineScope.launch(Dispatchers.IO) {
                playlist.playlist.browseId?.let { YouTube.addToPlaylist(it, mediaMetadata.id) }
            }

            listOf(mediaMetadata.id)
        },
        onDismiss = {
            showChoosePlaylistDialog = false
        }
    )

    var showSelectArtistDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showSelectArtistDialog) {
        ListDialog(
            onDismiss = { showSelectArtistDialog = false }
        ) {
            items(artists) { artist ->
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .height(ListItemHeight)
                        .clickable {
                            navController.navigate("artist/${artist.id}")
                            showSelectArtistDialog = false
                            bottomSheetState.collapseSoft()
                            onDismiss()
                        }
                        .padding(horizontal = 24.dp),
                ) {
                    Text(
                        text = artist.name,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }

    var showTempoPitchDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showTempoPitchDialog) {
        TempoPitchDialog(
            onDismiss = { showTempoPitchDialog = false }
        )
    }

    LazyColumn(
        contentPadding = PaddingValues(
            top = 16.dp,
            bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 16.dp
        )
    ) {
        item {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.VolumeDown,
                    contentDescription = stringResource(R.string.volume),
                    modifier = Modifier.size(24.dp)
                )
                Slider(
                    value = playerVolume.value,
                    onValueChange = { playerConnection.service.playerVolume.value = it },
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = MaterialTheme.colorScheme.primary,
                        activeTrackColor = MaterialTheme.colorScheme.primary,
                        inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.VolumeUp,
                    contentDescription = stringResource(R.string.volume),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }

        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!mediaMetadata.isLocal) {
                    ActionChip(
                        title = stringResource(R.string.start_radio),
                        icon = Icons.Rounded.Radio,
                        onClick = {
                            playerConnection.service.startRadioSeamlessly()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    val inLibrary = librarySong?.song?.inLibrary != null
                    ActionChip(
                        title = if (inLibrary) stringResource(R.string.add_to_library) else stringResource(R.string.add_to_library),
                        painterIcon = if (inLibrary) R.drawable.library_add_check else R.drawable.library_add,
                        isVector = false,
                        onClick = {
                            database.transaction {
                                if (inLibrary) {
                                    inLibrary(mediaMetadata.id, null)
                                } else {
                                    insert(mediaMetadata)
                                    inLibrary(mediaMetadata.id, LocalDateTime.now())
                                }
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                ActionChip(
                    title = stringResource(R.string.add_to_playlist),
                    icon = Icons.Rounded.PlaylistAdd,
                    onClick = { showChoosePlaylistDialog = true },
                    modifier = Modifier.weight(1f)
                )

                if (!mediaMetadata.isLocal) {
                    val downloadState = download?.state
                    ActionChip(
                        title = when (downloadState) {
                            Download.STATE_COMPLETED -> stringResource(R.string.download)
                            Download.STATE_DOWNLOADING -> stringResource(R.string.downloading)
                            else -> stringResource(R.string.download)
                        },
                        icon = when (downloadState) {
                            Download.STATE_COMPLETED -> Icons.Rounded.DownloadDone
                            else -> Icons.Rounded.Download
                        },
                        isLoading = downloadState == Download.STATE_DOWNLOADING,
                        onClick = {
                            if (downloadState == Download.STATE_COMPLETED) {
                                DownloadService.sendRemoveDownload(context, ExoDownloadService::class.java, mediaMetadata.id, false)
                            } else if (downloadState == null) {
                                database.transaction { insert(mediaMetadata) }
                                val downloadRequest = DownloadRequest.Builder(mediaMetadata.id, mediaMetadata.id.toUri())
                                    .setCustomCacheKey(mediaMetadata.id)
                                    .setData(mediaMetadata.title.toByteArray())
                                    .build()
                                DownloadService.sendAddDownload(context, ExoDownloadService::class.java, downloadRequest, false)
                            }
                        },
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    ActionChip(
                        title = stringResource(R.string.equalizer),
                        icon = Icons.Rounded.Equalizer,
                        onClick = {
                            val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                                putExtra(AudioEffect.EXTRA_AUDIO_SESSION, playerConnection.player.audioSessionId)
                                putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                                putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                            }
                            if (intent.resolveActivity(context.packageManager) != null) {
                                activityResultLauncher.launch(intent)
                            }
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                ActionChip(
                    title = stringResource(R.string.share),
                    icon = Icons.Rounded.Share,
                    onClick = {
                        showSocialShareDialog = true
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp))
        }

        if (!mediaMetadata.isLocal) {
            val inLibrary = librarySong?.song?.inLibrary != null
            item {
                MenuActionRow(
                    title = if (inLibrary) stringResource(R.string.remove_from_library) else stringResource(R.string.add_to_library),
                    painterIcon = if (inLibrary) R.drawable.library_add_check else R.drawable.library_add,
                    isVector = false
                ) {
                    database.transaction {
                        if (inLibrary) {
                            inLibrary(mediaMetadata.id, null)
                        } else {
                            insert(mediaMetadata)
                            inLibrary(mediaMetadata.id, LocalDateTime.now())
                        }
                    }
                }
            }
        }

        if (artists.isNotEmpty()) {
            item {
                MenuActionRow(
                    title = stringResource(R.string.view_artist),
                    icon = Icons.Rounded.Person
                ) {
                    if (artists.size == 1) {
                        navController.navigate("artist/${artists[0].id}")
                        bottomSheetState.collapseSoft()
                        onDismiss()
                    } else {
                        showSelectArtistDialog = true
                    }
                }
            }
        }

        if (mediaMetadata.album != null) {
            item {
                MenuActionRow(
                    title = stringResource(R.string.view_album),
                    icon = Icons.Rounded.Album
                ) {
                    navController.navigate("album/${mediaMetadata.album.id}")
                    bottomSheetState.collapseSoft()
                    onDismiss()
                }
            }
        }

        if (!mediaMetadata.isLocal) {
            item {
                MenuActionRow(
                    title = stringResource(R.string.equalizer),
                    icon = Icons.Rounded.Equalizer
                ) {
                    val intent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL).apply {
                        putExtra(AudioEffect.EXTRA_AUDIO_SESSION, playerConnection.player.audioSessionId)
                        putExtra(AudioEffect.EXTRA_PACKAGE_NAME, context.packageName)
                        putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                    }
                    if (intent.resolveActivity(context.packageManager) != null) {
                        activityResultLauncher.launch(intent)
                    }
                    onDismiss()
                }
            }
        }

        item {
            val summary = if (sleepTimerEnabled) makeTimeString(sleepTimerTimeLeft) else ""
            MenuActionRow(
                title = stringResource(R.string.sleep_timer),
                summary = summary,
                icon = Icons.Rounded.Timer
            ) {
                if (sleepTimerEnabled) playerConnection.service.sleepTimer.clear()
                else showSleepTimerDialog = true
            }
        }

        item {
            MenuActionRow(
                title = stringResource(R.string.tempo_and_pitch),
                painterIcon = R.drawable.speed,
                isVector = false
            ) {
                showTempoPitchDialog = true
            }
        }

        if (!mediaMetadata.isLocal) {
            item {
                MenuActionRow(
                    title = stringResource(R.string.listen_youtube_music),
                    icon = Icons.Rounded.AddLink
                ) {
                    val intent = Intent(Intent.ACTION_VIEW, "https://music.youtube.com/watch?v=${mediaMetadata.id}".toUri())
                    context.startActivity(intent)
                }
            }
        }

        item {
            MenuActionRow(
                title = stringResource(R.string.details),
                icon = Icons.Rounded.Info
            ) {
                onShowDetailsDialog()
                onDismiss()
            }
        }
    }
}

@Composable
fun ActionChip(
    title: String,
    icon: ImageVector? = null,
    painterIcon: Int? = null,
    isVector: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false
) {
    val isDarkTheme = isSystemInDarkTheme()
    val backgroundColor = if (isDarkTheme) {
        MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    Card(
        onClick = onClick,
        modifier = modifier.height(80.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(vertical = 8.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                if (isVector && icon != null) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        modifier = Modifier.size(24.dp)
                    )
                } else if (painterIcon != null) {
                    Icon(
                        painter = painterResource(painterIcon),
                        contentDescription = title,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.basicMarquee()
            )
        }
    }
}

@Composable
fun MenuActionRow(
    title: String,
    summary: String = "",
    icon: ImageVector? = null,
    painterIcon: Int? = null,
    isVector: Boolean = true,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isVector && icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = MaterialTheme.colorScheme.secondary
            )
        } else if (painterIcon != null) {
            Icon(
                painter = painterResource(painterIcon),
                contentDescription = title,
                tint = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            if (summary.isNotEmpty()) {
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}


@Composable
fun TempoPitchDialog(
    onDismiss: () -> Unit,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    var tempo by remember {
        mutableFloatStateOf(playerConnection.player.playbackParameters.speed)
    }
    var transposeValue by remember {
        mutableIntStateOf(round(12 * log2(playerConnection.player.playbackParameters.pitch)).toInt())
    }
    val updatePlaybackParameters = {
        playerConnection.player.playbackParameters =
            PlaybackParameters(tempo, 2f.pow(transposeValue.toFloat() / 12))
    }

    AlertDialog(
        properties = DialogProperties(usePlatformDefaultWidth = false),
        onDismissRequest = onDismiss,
        title = {
            Text(stringResource(R.string.tempo_and_pitch))
        },
        dismissButton = {
            TextButton(
                onClick = {
                    tempo = 1f
                    transposeValue = 0
                    updatePlaybackParameters()
                }
            ) {
                Text(stringResource(R.string.reset))
            }
        },
        confirmButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        text = {
            Column {
                ValueAdjuster(
                    icon = R.drawable.speed,
                    currentValue = tempo,
                    values = (0..35).map { round((0.25f + it * 0.05f) * 100) / 100 },
                    onValueUpdate = {
                        tempo = it
                        updatePlaybackParameters()
                    },
                    valueText = { "x$it" },
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                ValueAdjuster(
                    icon = R.drawable.discover_tune,
                    currentValue = transposeValue,
                    values = (-12..12).toList(),
                    onValueUpdate = {
                        transposeValue = it
                        updatePlaybackParameters()
                    },
                    valueText = { "${if (it > 0) "+" else ""}$it" }
                )
            }
        }
    )
}

@Composable
fun <T> ValueAdjuster(
    @DrawableRes icon: Int,
    currentValue: T,
    values: List<T>,
    onValueUpdate: (T) -> Unit,
    valueText: (T) -> String,
    modifier: Modifier = Modifier,
) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = Modifier.size(28.dp)
        )

        IconButton(
            enabled = currentValue != values.first(),
            onClick = {
                onValueUpdate(values[values.indexOf(currentValue) - 1])
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.remove),
                contentDescription = null
            )
        }

        Text(
            text = valueText(currentValue),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(80.dp)
        )

        IconButton(
            enabled = currentValue != values.last(),
            onClick = {
                onValueUpdate(values[values.indexOf(currentValue) + 1])
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.add),
                contentDescription = null
            )
        }
    }
}
