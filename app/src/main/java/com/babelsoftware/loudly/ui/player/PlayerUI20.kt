package com.babelsoftware.loudly.ui.player

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import androidx.annotation.RequiresPermission
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.rounded.PlaylistRemove
import androidx.compose.material.icons.rounded.VpnLock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.navigation.NavController
import coil.ImageLoader
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.babelsoftware.innertube.YouTube
import com.babelsoftware.loudly.LocalDatabase
import com.babelsoftware.loudly.LocalPlayerConnection
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.LyricFontSizeKey
import com.babelsoftware.loudly.constants.LyricsBackgroundDimKey
import com.babelsoftware.loudly.constants.ShowLyricsKey
import com.babelsoftware.loudly.constants.SliderStyle
import com.babelsoftware.loudly.constants.SliderStyleKey
import com.babelsoftware.loudly.extensions.metadata
import com.babelsoftware.loudly.extensions.togglePlayPause
import com.babelsoftware.loudly.extensions.toggleRepeatMode
import com.babelsoftware.loudly.extensions.toggleShuffleMode
import com.babelsoftware.loudly.lyrics.LyricsUiState
import com.babelsoftware.loudly.lyrics.LyricsUtils
import com.babelsoftware.loudly.lyrics.LyricsViewModel
import com.babelsoftware.loudly.models.MediaMetadata
import com.babelsoftware.loudly.playback.StreakInfo
import com.babelsoftware.loudly.ui.component.BottomSheetState
import com.babelsoftware.loudly.ui.component.LocalMenuState
import com.babelsoftware.loudly.ui.component.PlayerSliderTrack
import com.babelsoftware.loudly.ui.component.ResizableIconButton
import com.babelsoftware.loudly.ui.menu.AddToPlaylistDialog
import com.babelsoftware.loudly.ui.menu.PlayerMenu
import com.babelsoftware.loudly.ui.theme.extractGradientColors
import com.babelsoftware.loudly.utils.makeTimeString
import com.babelsoftware.loudly.utils.rememberEnumPreference
import com.babelsoftware.loudly.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.saket.squiggles.SquigglySlider
import java.util.Locale
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

private const val HQ_BITRATE = 100000

@Composable
fun PlayerUI20(
    position: Long,
    duration: Long,
    navController: NavController,
    bottomSheetState: BottomSheetState,
    modifier: Modifier = Modifier,
    lyricsViewModel: LyricsViewModel = hiltViewModel()
) {
    var showPlaylist by remember { mutableStateOf(false) }
    var showLyrics by rememberPreference(ShowLyricsKey, defaultValue = false)
    val playerConnection = LocalPlayerConnection.current ?: return
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val context = LocalContext.current
    var dominantColor by remember { mutableStateOf<Color?>(null) }

    LaunchedEffect(mediaMetadata?.id) {
        val currentMediaMetadata = mediaMetadata
        if (currentMediaMetadata == null) {
            dominantColor = null
            return@LaunchedEffect
        }
        withContext(Dispatchers.IO) {
            val result = (ImageLoader(context).execute(
                ImageRequest.Builder(context)
                    .data(currentMediaMetadata.thumbnailUrl)
                    .allowHardware(false)
                    .build()
            ).drawable as? BitmapDrawable)?.bitmap?.extractGradientColors()

            dominantColor = result?.firstOrNull()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        val currentMediaMetadata = mediaMetadata
        if (currentMediaMetadata != null) {
            AsyncImage(
                model = currentMediaMetadata.thumbnailUrl,
                contentDescription = "Background Picture",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(80.dp)
                    .alpha(0.6f)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            )
        }
        AnimatedVisibility(
            visible = !showLyrics,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300))
        ) {
            AnimatedContent(
                targetState = showPlaylist,
                transitionSpec = {
                    if (targetState) {
                        slideInVertically { height -> height } + fadeIn() togetherWith
                                slideOutVertically { height -> -height } + fadeOut()
                    } else {
                        slideInVertically { height -> -height } + fadeIn() togetherWith
                                slideOutVertically { height -> height } + fadeOut()
                    }
                },
                label = "Player/Playlist"
            ) { isPlaylistVisible ->
                if (isPlaylistVisible) {
                    PlaylistScreen(
                        onClose = { showPlaylist = false },
                        dominantColor = dominantColor ?: MaterialTheme.colorScheme.primary,
                        navController = navController,
                        bottomSheetState = bottomSheetState
                    )
                } else {
                    NowPlayingScreen(
                        position = position,
                        duration = duration,
                        navController = navController,
                        bottomSheetState = bottomSheetState,
                        onShowPlaylist = { showPlaylist = true },
                        onClose = { bottomSheetState.collapseSoft() },
                        dominantColor = dominantColor,
                        onToggleLyrics = { showLyrics = !showLyrics }
                    )
                }
            }
        }
        AnimatedVisibility(
            visible = showLyrics,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut()
        ) {
            LyricsScreen(
                position = position,
                viewModel = lyricsViewModel,
                onDismiss = { showLyrics = false }
            )
        }
    }
}

@Composable
fun NowPlayingScreen(
    position: Long,
    duration: Long,
    navController: NavController,
    bottomSheetState: BottomSheetState,
    onShowPlaylist: () -> Unit,
    onClose: () -> Unit,
    dominantColor: Color?,
    onToggleLyrics: () -> Unit
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()
    var sliderPosition by remember { mutableStateOf<Long?>(null) }
    var showDetailsDialog by remember { mutableStateOf(false) }

    if (showDetailsDialog) {
        DetailsDialog(
            onDismiss = { showDetailsDialog = false }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    if (dragAmount < -50) {
                        onShowPlaylist()
                        change.consume()
                    }
                }
            }
            .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Vertical)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        PlayerTopBar(onClose = onClose)
        Spacer(modifier = Modifier.height(16.dp))

        PlayerArtwork(
            mediaMetadata = mediaMetadata,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 32.dp)
        ) { direction ->
            if (direction > 0) playerConnection.player.seekToPrevious()
            else playerConnection.player.seekToNext()
        }

        SongInfo(
            mediaMetadata = mediaMetadata,
            modifier = Modifier.padding(horizontal = 32.dp, vertical = 24.dp)
        )

        ContextualInfoRow(
            navController = navController,
            bottomSheetState = bottomSheetState,
            onShowPlaylist = onShowPlaylist,
            onShowDetailsDialog = { showDetailsDialog = true }
        )
        Spacer(modifier = Modifier.height(8.dp))

        ControlsCard(
            position = sliderPosition ?: position,
            duration = duration,
            isPlaying = isPlaying,
            dominantColor = dominantColor,
            onSeek = { newPosition ->
                playerConnection.player.seekTo(newPosition)
                sliderPosition = null
            },
            onToggleLyrics = onToggleLyrics,
            navController = navController
        )
    }
}

@Composable
private fun PlayerTopBar(onClose: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onClose) {
            Icon(painterResource(R.drawable.arrow_back), contentDescription = "Geri", tint = Color.White)
        }
    }
}

@Composable
private fun PlayerArtwork(
    mediaMetadata: MediaMetadata?,
    modifier: Modifier = Modifier,
    onSwipe: (direction: Int) -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val alpha by animateFloatAsState(
        targetValue = 1f - (offsetX.coerceIn(-300f, 300f).absoluteValue / 300f) * 0.7f,
        label = "ArtworkAlpha"
    )

    AnimatedContent(
        targetState = mediaMetadata,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "Artwork",
        modifier = modifier
            .aspectRatio(1f)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        when {
                            offsetX > 150 -> onSwipe(1)
                            offsetX < -150 -> onSwipe(-1)
                        }
                        offsetX = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        offsetX += dragAmount
                    }
                )
            }
            .graphicsLayer {
                shadowElevation = 16.dp.toPx()
                shape = RoundedCornerShape(24.dp)
                clip = true
            }
    ) { currentArtworkMetadata ->
        AsyncImage(
            model = currentArtworkMetadata?.thumbnailUrl,
            contentDescription = "Album Thumbnail",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .alpha(alpha)
                .offset(x = (offsetX / 2).dp)
        )
    }
}

@Composable
private fun SongInfo(mediaMetadata: MediaMetadata?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedContent(
            targetState = mediaMetadata?.title,
            transitionSpec = {
                (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
            },
            label = "Title"
        ) { title ->
            Text(
                text = title ?: "",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.basicMarquee()
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        AnimatedContent(
            targetState = mediaMetadata?.artists?.joinToString { it.name },
            transitionSpec = {
                (slideInVertically { it } + fadeIn()) togetherWith (slideOutVertically { -it } + fadeOut())
            },
            label = "Artist"
        ) { artist ->
            Text(
                text = artist ?: "",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.7f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
                modifier = Modifier.basicMarquee()
            )
        }
    }
}

@Composable
fun ContextualInfoRow(
    navController: NavController,
    bottomSheetState: BottomSheetState,
    onShowPlaylist: () -> Unit,
    onShowDetailsDialog: () -> Unit
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val menuState = LocalMenuState.current
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val currentFormat by playerConnection.currentFormat.collectAsState(initial = null)

    val showArtistChip = mediaMetadata?.artists?.any { it.id != null } == true
    val bitrate = currentFormat?.bitrate
    val isHq = bitrate != null && bitrate > HQ_BITRATE

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.End,
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (showArtistChip || bitrate != null) {
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
                ),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.height(IntrinsicSize.Min)
                ) {
                    if (showArtistChip) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clickable {
                                    navController.navigate("artist/${mediaMetadata!!.artists.first { it.id != null }.id}")
                                    bottomSheetState.collapseSoft()
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.artist),
                                contentDescription = stringResource(R.string.view_artist),
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = stringResource(R.string.artist),
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                    if (bitrate != null && showArtistChip) {
                        HorizontalDivider(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp),
                            color = Color.White.copy(alpha = 0.25f)
                        )
                    }
                    if (bitrate != null) {
                        val qualityRowModifier = if (!isHq) {
                            Modifier.clickable {
                                navController.navigate("settings/player")
                                bottomSheetState.collapseSoft()
                            }
                        } else {
                            Modifier
                        }
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = qualityRowModifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                painter = painterResource(if (isHq) R.drawable.high_quality else R.drawable.standart_quality),
                                contentDescription = "Quality",
                                tint = Color.White.copy(alpha = 0.7f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (isHq) "HD" else "SD",
                                color = Color.White.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.width(8.dp))

        InfoChip(
            text = stringResource(R.string.queue),
            icon = R.drawable.queue_music,
            color = Color.White,
            onClick = onShowPlaylist
        )

        Spacer(Modifier.width(8.dp))

        InfoChip(
            icon = R.drawable.more_vert,
            text = stringResource(R.string.menu),
            color = Color.White,
            onClick = {
                menuState.show {
                    PlayerMenu(
                        mediaMetadata = mediaMetadata,
                        navController = navController,
                        bottomSheetState = bottomSheetState,
                        onShowDetailsDialog = onShowDetailsDialog,
                        onDismiss = menuState::dismiss
                    )
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ControlsCard(
    position: Long,
    duration: Long,
    isPlaying: Boolean,
    dominantColor: Color?,
    onSeek: (Long) -> Unit,
    onToggleLyrics: () -> Unit,
    navController: NavController
) {
    var sliderPosition by remember { mutableStateOf<Float?>(null) }
    val sliderStyle by rememberEnumPreference(SliderStyleKey, SliderStyle.COMPOSE)

    Card(
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .padding(bottom = 16.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(vertical = 16.dp, horizontal = 24.dp)
        ) {
            when (sliderStyle) {
                SliderStyle.DEFAULT -> {
                    Slider(
                        value = sliderPosition ?: position.toFloat(),
                        valueRange = 0f..(if (duration > 0) duration.toFloat() else 1f),
                        onValueChange = { sliderPosition = it },
                        onValueChangeFinished = {
                            sliderPosition?.let { onSeek(it.toLong()) }
                            sliderPosition = null
                        },
                        thumb = { Spacer(modifier = Modifier.size(0.dp)) },
                        track = { sliderState ->
                            PlayerSliderTrack(
                                sliderState = sliderState,
                                colors = SliderDefaults.colors()
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                SliderStyle.SQUIGGLY -> {
                    SquigglySlider(
                        value = sliderPosition ?: position.toFloat(),
                        valueRange = 0f..(if (duration > 0) duration.toFloat() else 1f),
                        onValueChange = { sliderPosition = it },
                        onValueChangeFinished = {
                            sliderPosition?.let { onSeek(it.toLong()) }
                            sliderPosition = null
                        },
                        squigglesSpec = SquigglySlider.SquigglesSpec(
                            amplitude = if (isPlaying) 2.dp else 0.dp,
                            strokeWidth = 3.dp,
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                SliderStyle.COMPOSE -> {
                    Slider(
                        value = sliderPosition ?: position.toFloat(),
                        valueRange = 0f..(if (duration > 0) duration.toFloat() else 1f),
                        onValueChange = { sliderPosition = it },
                        onValueChangeFinished = {
                            sliderPosition?.let { onSeek(it.toLong()) }
                            sliderPosition = null
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
            Spacer(Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = makeTimeString(sliderPosition?.toLong() ?: position),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(
                    text = makeTimeString(duration),
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }
            Spacer(Modifier.height(16.dp))

            MainControls(isPlaying = isPlaying, dominantColor = dominantColor)
            Spacer(Modifier.height(16.dp))

            SecondaryControlsRow(
                dominantColor = dominantColor,
                onToggleLyrics = onToggleLyrics,
                navController = navController
            )
        }
    }
}

@Composable
private fun MainControls(isPlaying: Boolean, dominantColor: Color?) {
    val playerConnection = LocalPlayerConnection.current!!
    val shuffleModeEnabled by playerConnection.shuffleModeEnabled.collectAsState()
    val repeatMode by playerConnection.repeatMode.collectAsState()
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()
    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    val haptic = LocalHapticFeedback.current
    val playbackState by playerConnection.playbackState.collectAsState()

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        ResizableIconButton(
            icon = R.drawable.shuffle,
            modifier = Modifier.size(28.dp),
            color = if (shuffleModeEnabled) dominantColor ?: Color.White else Color.White.copy(alpha = 0.7f),
            onClick = playerConnection.player::toggleShuffleMode
        )

        ResizableIconButton(
            icon = R.drawable.skip_previous,
            enabled = canSkipPrevious,
            color = Color.White,
            modifier = Modifier.size(40.dp),
            onClick = playerConnection.player::seekToPrevious
        )

        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(Color.White)
                .clickable {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    if (playbackState == Player.STATE_ENDED) {
                        playerConnection.player.seekTo(0, 0)
                        playerConnection.player.playWhenReady = true
                    } else {
                        playerConnection.player.togglePlayPause()
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = isPlaying,
                label = "PlayPauseIcon",
                contentAlignment = Alignment.Center
            ) { playing ->
                Icon(
                    painter = painterResource(if (playing) R.drawable.pause else R.drawable.play),
                    contentDescription = "Play/Pause",
                    tint = Color.Black,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        ResizableIconButton(
            icon = R.drawable.skip_next,
            enabled = canSkipNext,
            color = Color.White,
            modifier = Modifier.size(40.dp),
            onClick = playerConnection.player::seekToNext
        )

        ResizableIconButton(
            icon = when (repeatMode) {
                Player.REPEAT_MODE_OFF, Player.REPEAT_MODE_ALL -> R.drawable.repeat
                Player.REPEAT_MODE_ONE -> R.drawable.repeat_one
                else -> throw IllegalStateException()
            },
            modifier = Modifier.size(28.dp),
            color = if (repeatMode != Player.REPEAT_MODE_OFF) dominantColor ?: Color.White else Color.White.copy(alpha = 0.7f),
            onClick = playerConnection.player::toggleRepeatMode
        )
    }
}

@Composable
private fun SecondaryControlsRow(
    dominantColor: Color?,
    onToggleLyrics: () -> Unit,
    navController: NavController
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val database = LocalDatabase.current
    val coroutineScope = rememberCoroutineScope()
    var showChoosePlaylistDialog by remember { mutableStateOf(false) }

    if (mediaMetadata != null) {
        AddToPlaylistDialog(
            navController = navController,
            isVisible = showChoosePlaylistDialog,
            onGetSong = { playlist ->
                database.transaction {
                    insert(mediaMetadata!!)
                }
                coroutineScope.launch(Dispatchers.IO) {
                    playlist.playlist.browseId?.let {
                        YouTube.addToPlaylist(it, mediaMetadata!!.id)
                    }
                }
                listOf(mediaMetadata!!.id)
            },
            onDismiss = {
                showChoosePlaylistDialog = false
            }
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val isLiked = currentSong?.song?.liked == true
        val likeColor by animateColorAsState(
            targetValue = if (isLiked) dominantColor ?: MaterialTheme.colorScheme.error else Color.White.copy(alpha = 0.7f),
            label = "LikeColor"
        )
        TextButton(onClick = { playerConnection.toggleLike() }) {
            Icon(
                painter = if (isLiked) painterResource(R.drawable.favorite) else painterResource(R.drawable.favorite_border),
                contentDescription = stringResource(R.string.like),
                tint = likeColor,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.like),
                color = likeColor,
                modifier = Modifier
                    .widthIn(max = 75.dp)
                    .basicMarquee()
            )
        }

        TextButton(onClick = { showChoosePlaylistDialog = true }) {
            Icon(
                painter = painterResource(R.drawable.playlist_add),
                contentDescription = stringResource(R.string.add_to_playlist),
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.add_to_playlist),
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .widthIn(max = 75.dp)
                    .basicMarquee()
            )
        }
        TextButton(onClick = onToggleLyrics) {
            Icon(
                painter = painterResource(R.drawable.lyrics),
                contentDescription = stringResource(R.string.lyrics),
                tint = Color.White.copy(alpha = 0.7f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.lyrics),
                color = Color.White.copy(alpha = 0.7f),
                modifier = Modifier
                    .widthIn(max = 75.dp)
                    .basicMarquee()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeBackground(
    dismissValue: SwipeToDismissBoxValue,
    icon: ImageVector,
    text: String,
    alignment: Alignment,
    color: Color
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color)
            .padding(horizontal = 24.dp),
        contentAlignment = alignment
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onPrimary,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaylistScreen(
    onClose: () -> Unit,
    dominantColor: Color,
    navController: NavController,
    bottomSheetState: BottomSheetState
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val queueWindows by playerConnection.queueWindows.collectAsState()
    val currentWindowIndex by playerConnection.currentWindowIndex.collectAsState()
    val lazyListState = rememberLazyListState()
    val queueTitle by playerConnection.queueTitle.collectAsState()

    LaunchedEffect(currentWindowIndex) {
        if (currentWindowIndex != -1) {
            lazyListState.animateScrollToItem(currentWindowIndex)
        }
    }

    val backgroundColor = MaterialTheme.colorScheme.surface

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .pointerInput(Unit) {
                detectVerticalDragGestures { change, dragAmount ->
                    if (dragAmount > 50) { // Threshold for downward swipe
                        onClose()
                        change.consume()
                    }
                }
            }
    ) {
        LazyColumn(
            state = lazyListState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 280.dp, bottom = 16.dp)
        ) {
            itemsIndexed(queueWindows, key = { _, window -> window.mediaItem.mediaId }) { index, window ->
                window.mediaItem.metadata?.let { metadata ->
                    val currentItem by rememberUpdatedState(window.mediaItem)
                    val dismissState = rememberSwipeToDismissBoxState()

                    LaunchedEffect(dismissState.targetValue) {
                        if (dismissState.targetValue != SwipeToDismissBoxValue.Settled) {
                            when (dismissState.targetValue) {
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    val artistId = currentItem.metadata?.artists?.firstOrNull { it.id != null }?.id
                                    if (artistId != null) {
                                        navController.navigate("artist/$artistId")
                                        bottomSheetState.collapseSoft()
                                    }
                                }
                                SwipeToDismissBoxValue.EndToStart -> {
                                    playerConnection.player.removeMediaItem(index)
                                }
                                else -> {}
                            }
                            dismissState.reset()
                        }
                    }

                    SwipeToDismissBox(
                        state = dismissState,
                        backgroundContent = {
                            val direction = dismissState.dismissDirection
                            when (direction) {
                                SwipeToDismissBoxValue.StartToEnd -> {
                                    SwipeBackground(
                                        dismissValue = direction,
                                        icon = Icons.Default.Person,
                                        text = stringResource(R.string.view_artist),
                                        alignment = Alignment.CenterStart,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                                    )
                                }
                                SwipeToDismissBoxValue.EndToStart -> {
                                    SwipeBackground(
                                        dismissValue = direction,
                                        icon = Icons.Rounded.PlaylistRemove,
                                        text = stringResource(R.string.remove_from_queue),
                                        alignment = Alignment.CenterEnd,
                                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                                    )
                                }
                                SwipeToDismissBoxValue.Settled -> {}
                            }
                        },
                        modifier = Modifier.animateItem()
                    ) {
                        SimplePlaylistItem(
                            metadata = metadata,
                            isActive = index == currentWindowIndex,
                            activeColor = dominantColor,
                            onClick = {
                                if (index == currentWindowIndex) {
                                    playerConnection.player.togglePlayPause()
                                } else {
                                    playerConnection.player.seekToDefaultPosition(index)
                                    if (!playerConnection.player.playWhenReady) {
                                        playerConnection.player.playWhenReady = true
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, backgroundColor)
                    )
                )
        )

        val parallaxOffset = if (lazyListState.firstVisibleItemIndex == 0) {
            lazyListState.firstVisibleItemScrollOffset * 0.5f
        } else {
            0f
        }

        Box(modifier = Modifier.graphicsLayer { translationY = parallaxOffset }) {
            CurvedHeader(
                onClose = onClose,
                queueWindows = queueWindows,
                queueTitle = queueTitle
            )
        }
    }
}

@Composable
fun SimplePlaylistItem(
    metadata: MediaMetadata,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit
) {
    val inactiveColor = MaterialTheme.colorScheme.onSurface
    val textColor by animateColorAsState(
        targetValue = if (isActive) activeColor else inactiveColor,
        label = "TextColor"
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(horizontal = 24.dp, vertical = 12.dp)
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = metadata.title,
                color = textColor,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Spacer(Modifier.width(16.dp))
        Text(
            text = metadata.duration?.let { makeTimeString(it * 1000L) } ?: "0:00",
            color = textColor.copy(alpha = 0.7f),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
fun CurvedHeader(
    onClose: () -> Unit,
    queueWindows: List<Timeline.Window>,
    queueTitle: String?
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val context = LocalContext.current

    val headerColor = MaterialTheme.colorScheme.surface
    val onHeaderColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(CurvedHeaderShape())
            .background(headerColor)
    ) {
        val currentMediaMetadata = mediaMetadata
        if (currentMediaMetadata != null) {
            AsyncImage(
                model = currentMediaMetadata.thumbnailUrl,
                contentDescription = "Albüm Kapağı",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(if (isSystemInDarkTheme()) 0.4f else 1f)
            )
        }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, headerColor.copy(alpha = 0.8f), headerColor),
                        startY = 150f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Top)),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onClose) {
                    Icon(painterResource(id = R.drawable.arrow_back), contentDescription = "Geri", tint = onHeaderColor)
                }
                InfoChip(
                    icon = R.drawable.share,
                    text = stringResource(R.string.share_list),
                    color = onHeaderColor,
                    onClick = {
                        val shareText = buildString {
                            appendLine(queueTitle ?: "Playlist")
                            appendLine()
                            queueWindows.forEach { window ->
                                window.mediaItem.metadata?.let { metadata ->
                                    appendLine("${metadata.title} - ${metadata.artists.joinToString { it.name }}")
                                }
                            }
                        }
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }
                )
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val currentMediaMetadata = mediaMetadata
                if (currentMediaMetadata != null) {
                    Text(
                        text = queueTitle ?: "Playlist",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = onHeaderColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = currentMediaMetadata.artists.joinToString { it.name },
                        style = MaterialTheme.typography.titleMedium,
                        color = onHeaderColor.copy(alpha = 0.8f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

class CurvedHeaderShape : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width, size.height - 60f)
            quadraticTo(
                x1 = size.width / 2f, y1 = size.height + 60f,
                x2 = 0f, y2 = size.height - 60f
            )
            close()
        }
        return Outline.Generic(path)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoChip(
    modifier: Modifier = Modifier,
    icon: Int? = null,
    text: String? = null,
    color: Color = LocalContentColor.current,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            if (icon != null) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = color
                )
            }
            if (text != null) {
                if (icon != null) Spacer(Modifier.width(4.dp))
                Text(
                    text = text,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = color
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LyricsControls(
    onDismiss: () -> Unit,
    viewModel: LyricsViewModel
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val lyricsEntity by playerConnection.currentLyrics.collectAsState()

    var fontSize by rememberPreference(LyricFontSizeKey, 22)
    var backgroundDim by rememberPreference(LyricsBackgroundDimKey, 0)
    var showLanguageSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val onSurfaceColor = Color.White
    val onSurfaceVariantColor = Color.White.copy(alpha = 0.7f)

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("A-", color = onSurfaceVariantColor, modifier = Modifier.clickable { if (fontSize > 14) fontSize -= 2 })
            SquigglySlider(
                value = fontSize.toFloat(),
                onValueChange = { fontSize = it.roundToInt() },
                valueRange = 14f..40f,
                modifier = Modifier.weight(1f)
            )
            Text("A+", color = onSurfaceVariantColor, modifier = Modifier.clickable { if (fontSize < 40) fontSize += 2 })

            Spacer(modifier = Modifier.width(16.dp))

            Icon(painterResource(R.drawable.dark_mode), contentDescription = "Dim", tint = onSurfaceVariantColor)
            SquigglySlider(
                value = backgroundDim.toFloat(),
                onValueChange = { backgroundDim = it.roundToInt() },
                valueRange = 0f..100f,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            IconButton(onClick = { showLanguageSheet = true }) {
                Icon(
                    painter = painterResource(R.drawable.translate),
                    contentDescription = stringResource(R.string.translate),
                    tint = onSurfaceVariantColor
                )
            }
            IconButton(onClick = {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, lyricsEntity?.lyrics ?: "")
                    type = "text/plain"
                }
                context.startActivity(Intent.createChooser(sendIntent, null))
            }) {
                Icon(painterResource(R.drawable.share), contentDescription = stringResource(R.string.share), tint = onSurfaceVariantColor)
            }
            IconButton(onClick = {
                clipboardManager.setText(AnnotatedString(lyricsEntity?.lyrics ?: ""))
            }) {
                Icon(painterResource(R.drawable.content_copy), contentDescription = stringResource(R.string.copy), tint = onSurfaceVariantColor)
            }
            IconButton(onClick = { playerConnection.saveCurrentLyrics() }) {
                Icon(painterResource(R.drawable.save), contentDescription = "Save Offline", tint = onSurfaceVariantColor)
            }
            IconButton(onClick = onDismiss) {
                Icon(painterResource(R.drawable.arrow_upward), contentDescription = "Back to Player", tint = onSurfaceColor)
            }
        }
    }
    if (showLanguageSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageSheet = false },
            sheetState = sheetState
        ) {
            LanguageSelectionSheet(
                languages = viewModel.targetLanguages,
                onLanguageSelected = { languageCode ->
                    viewModel.translateLyrics(languageCode)
                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                        if (!sheetState.isVisible) {
                            showLanguageSheet = false
                        }
                    }
                }
            )
        }
    }
}

@Composable
fun LanguageSelectionSheet(
    languages: List<String>,
    onLanguageSelected: (String) -> Unit
) {
    LazyColumn {
        items(languages) { languageCode ->
            ListItem(
                headlineContent = {
                    Text(text = Locale(languageCode).displayLanguage)
                },
                modifier = Modifier.clickable { onLanguageSelected(languageCode) }
            )
        }
    }
}

@Composable
private fun LyricsScreen(
    position: Long,
    viewModel: LyricsViewModel,
    modifier: Modifier = Modifier,
    onDismiss: () -> Unit
) {
    val lyricsUiState by viewModel.lyricsState.collectAsState()
    val playerConnection = LocalPlayerConnection.current ?: return
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    LaunchedEffect(mediaMetadata?.id) {
        mediaMetadata?.let {
            viewModel.loadLyricsFor(it)
        }
    }

    val fontSize by rememberPreference(LyricFontSizeKey, 22)
    val backgroundDim by rememberPreference(LyricsBackgroundDimKey, 0)
    val onSurfaceColor = Color.White
    val dimColor = if (isSystemInDarkTheme()) Color.Black else MaterialTheme.colorScheme.surface

    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                .background(dimColor.copy(alpha = backgroundDim.toFloat() / 100f))
                .clickable(onClick = onDismiss)
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp)
                .windowInsetsPadding(WindowInsets.systemBars),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center
            ) {
                when (val state = lyricsUiState) {
                    is LyricsUiState.Loading -> {
                        CircularProgressIndicator(color = onSurfaceColor)
                    }
                    is LyricsUiState.Error -> {
                        Text(
                            text = state.message,
                            color = onSurfaceColor.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                    is LyricsUiState.Success -> {
                        val parsedLyrics = state.entries
                        if (parsedLyrics.isEmpty()) {
                            Text(
                                text = "No lyrics were found for this song.",
                                color = onSurfaceColor.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.headlineSmall,
                                textAlign = TextAlign.Center
                            )
                        } else {
                            val listState = rememberLazyListState()
                            val currentLineIndex by remember(parsedLyrics, position) {
                                derivedStateOf { LyricsUtils.findCurrentLineIndex(parsedLyrics, position) }
                            }

                            LaunchedEffect(currentLineIndex) {
                                if (currentLineIndex != -1 && currentLineIndex < listState.layoutInfo.totalItemsCount) {
                                    listState.animateScrollToItem(index = currentLineIndex, scrollOffset = -250)
                                }
                            }

                            LazyColumn(
                                state = listState,
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                modifier = Modifier.fillMaxSize()
                            ) {
                                item { Spacer(modifier = Modifier.height(200.dp)) }
                                itemsIndexed(parsedLyrics) { index, line ->
                                    if (line.isTranslation) return@itemsIndexed
                                    val isActive = index == currentLineIndex
                                    val color by animateColorAsState(targetValue = if (isActive) onSurfaceColor else onSurfaceColor.copy(alpha = 0.6f), label = "")
                                    val dynamicFontSize by animateFloatAsState(targetValue = if (isActive) (fontSize + 4).toFloat() else fontSize.toFloat(), label = "")
                                    val fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal

                                    Text(
                                        text = line.text,
                                        color = color,
                                        fontSize = dynamicFontSize.sp,
                                        fontWeight = fontWeight,
                                        textAlign = TextAlign.Center,
                                        lineHeight = (dynamicFontSize * 1.5).sp,
                                        modifier = Modifier.clickable { playerConnection.player.seekTo(line.time) }
                                    )
                                }
                                item { Spacer(modifier = Modifier.height(200.dp)) }
                            }
                        }
                    }
                }
            }
            LyricsControls(onDismiss = onDismiss, viewModel = viewModel)
        }
    }
}
