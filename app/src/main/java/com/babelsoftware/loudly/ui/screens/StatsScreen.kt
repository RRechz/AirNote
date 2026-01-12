package com.babelsoftware.loudly.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.babelsoftware.innertube.models.WatchEndpoint
import com.babelsoftware.loudly.LocalPlayerAwareWindowInsets
import com.babelsoftware.loudly.LocalPlayerConnection
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.StatPeriod
import com.babelsoftware.loudly.db.entities.Song
import com.babelsoftware.loudly.extensions.togglePlayPause
import com.babelsoftware.loudly.models.toMediaMetadata
import com.babelsoftware.loudly.playback.queues.YouTubeQueue
import com.babelsoftware.loudly.ui.component.*
import com.babelsoftware.loudly.ui.menu.AlbumMenu
import com.babelsoftware.loudly.ui.menu.ArtistMenu
import com.babelsoftware.loudly.ui.menu.SongMenu
import com.babelsoftware.loudly.viewmodels.StatsViewModel

@Composable
fun AnimatedGradientBackground(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "background_transition")
    val colors = listOf(
        MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.background.copy(alpha = 0.5f),
        MaterialTheme.colorScheme.background
    )

    val animatedOffset by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "offset_animation"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val brush = Brush.radialGradient(
            colors = colors,
            center = center + Offset(x = center.x * animatedOffset, y = center.y * animatedOffset),
            radius = size.maxDimension * 1.2f
        )
        drawRect(brush)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SongStatsListItem(
    song: Song,
    playCountRatio: Float,
    isActive: Boolean,
    onPlayClick: () -> Unit,
    onMenuClick: () -> Unit
) {
    var targetRatio by remember { mutableStateOf(0f) }
    val animatedRatio by animateFloatAsState(
        targetValue = targetRatio,
        animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
        label = "ratio_animation"
    )

    LaunchedEffect(playCountRatio) {
        targetRatio = playCountRatio
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onPlayClick,
                onLongClick = onMenuClick
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedRatio)
                .height(64.dp)
                .background(
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                )
        )
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = song.thumbnailUrl,
                contentDescription = song.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = song.title,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = song.artists.joinToString { it.name },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
            IconButton(onClick = onMenuClick) {
                Icon(
                    painter = painterResource(R.drawable.more_vert),
                    contentDescription = stringResource(R.string.menu),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val statPeriod by viewModel.statPeriod.collectAsState()
    val summaryMostPlayedSongs by viewModel.summaryMostPlayedSongs.collectAsState()
    val detailedMostPlayedSongs by viewModel.detailedMostPlayedSongs.collectAsState()
    val mostPlayedArtists by viewModel.mostPlayedArtists.collectAsState()
    val mostPlayedAlbums by viewModel.mostPlayedAlbums.collectAsState()
    var songsCardExpanded by rememberSaveable { mutableStateOf(false) }
    var artistsCardExpanded by rememberSaveable { mutableStateOf(false) }
    var albumsCardExpanded by rememberSaveable { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val parallaxOffset by remember {
        derivedStateOf {
            listState.firstVisibleItemScrollOffset * 0.38f
        }
    }

    val frostedCardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
    )
    val frostedCardBorder = BorderStroke(1.dp, MaterialTheme.colorScheme.onSurface.copy(alpha = 0.15f))

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedGradientBackground(
            modifier = Modifier.graphicsLayer {
                translationY = -parallaxOffset
            }
        )

        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.stats)) },
                    navigationIcon = {
                        IconButton(onClick = navController::navigateUp) {
                            Icon(
                                painterResource(R.drawable.arrow_back),
                                contentDescription = stringResource(R.string.back)
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f)
                    )
                )
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                state = listState,
                contentPadding = PaddingValues(
                    start = 16.dp,
                    end = 16.dp,
                    top = innerPadding.calculateTopPadding() + 8.dp,
                    bottom = LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom).asPaddingValues().calculateBottomPadding() + 16.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (summaryMostPlayedSongs.isEmpty() && mostPlayedArtists.isEmpty() && mostPlayedAlbums.isEmpty()) {
                    item {
                        EmptyPlaceholder(
                            icon = R.drawable.trending_up,
                            text = stringResource(R.string.stats_empty),
                            modifier = Modifier
                                .fillParentMaxSize()
                                .animateItem()
                        )
                    }
                } else {
                    item {
                        ChipsRow(
                            chips = listOf(
                                StatPeriod.`1_WEEK` to pluralStringResource(R.plurals.n_week, 1, 1),
                                StatPeriod.`1_MONTH` to pluralStringResource(R.plurals.n_month, 1, 1),
                                StatPeriod.`3_MONTH` to pluralStringResource(R.plurals.n_month, 3, 3),
                                StatPeriod.`6_MONTH` to pluralStringResource(R.plurals.n_month, 6, 6),
                                StatPeriod.`1_YEAR` to pluralStringResource(R.plurals.n_year, 1, 1),
                                StatPeriod.ALL to stringResource(R.string.filter_all)
                            ),
                            currentValue = statPeriod,
                            onValueUpdate = { viewModel.statPeriod.value = it },
                            modifier = Modifier.animateItem()
                        )
                    }

                    if (summaryMostPlayedSongs.isNotEmpty()) {
                        item(key = "mostPlayedSongs") {
                            val itemsToShow = if (songsCardExpanded) detailedMostPlayedSongs else summaryMostPlayedSongs
                            val maxPlayCount = (detailedMostPlayedSongs.firstOrNull()?.playCount?.toFloat() ?: 1f) * 1.1f
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = frostedCardColors,
                                border = frostedCardBorder,
                                onClick = { songsCardExpanded = !songsCardExpanded },
                                modifier = Modifier.fillMaxWidth().animateItem()
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    NavigationTitle(
                                        title = stringResource(R.string.most_played_songs),
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    itemsToShow.forEach { songWithCount ->
                                        val ratio = songWithCount.playCount.toFloat() / maxPlayCount
                                        SongStatsListItem(
                                            song = songWithCount.song,
                                            playCountRatio = ratio,
                                            isActive = songWithCount.song.id == mediaMetadata?.id,
                                            onPlayClick = {
                                                if (songWithCount.song.id == mediaMetadata?.id) {
                                                    playerConnection.player.togglePlayPause()
                                                } else {
                                                    playerConnection.playQueue(
                                                        YouTubeQueue(
                                                            endpoint = WatchEndpoint(songWithCount.song.id),
                                                            preloadItem = songWithCount.song.toMediaMetadata()
                                                        )
                                                    )
                                                }
                                            },
                                            onMenuClick = {
                                                menuState.show {
                                                    SongMenu(
                                                        originalSong = songWithCount.song,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss
                                                    )
                                                }
                                            }
                                        )
                                    }
                                    if (detailedMostPlayedSongs.size > summaryMostPlayedSongs.size) {
                                        TextButton(
                                            onClick = { songsCardExpanded = !songsCardExpanded },
                                            modifier = Modifier.align(Alignment.CenterHorizontally)
                                        ) {
                                            Text(
                                                text = if (songsCardExpanded) stringResource(R.string.show_less)
                                                else stringResource(R.string.show_more)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (mostPlayedArtists.isNotEmpty()) {
                        item(key = "mostPlayedArtists") {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = frostedCardColors,
                                border = frostedCardBorder,
                                onClick = { artistsCardExpanded = !artistsCardExpanded },
                                modifier = Modifier.fillMaxWidth().animateItem()
                            ) {
                                Column {
                                    NavigationTitle(
                                        title = stringResource(R.string.most_played_artists),
                                        modifier = Modifier.padding(16.dp)
                                    )
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        items(
                                            items = mostPlayedArtists,
                                            key = { it.id }
                                        ) { artist ->
                                            ArtistGridItem(
                                                artist = artist,
                                                modifier = Modifier.combinedClickable(
                                                    onClick = { navController.navigate("artist/${artist.id}") },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        menuState.show {
                                                            ArtistMenu(
                                                                originalArtist = artist,
                                                                coroutineScope = coroutineScope,
                                                                onDismiss = menuState::dismiss
                                                            )
                                                        }
                                                    }
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (mostPlayedAlbums.isNotEmpty()) {
                        item(key = "mostPlayedAlbums") {
                            Card(
                                shape = RoundedCornerShape(20.dp),
                                colors = frostedCardColors,
                                border = frostedCardBorder,
                                onClick = { albumsCardExpanded = !albumsCardExpanded },
                                modifier = Modifier.fillMaxWidth().animateItem()
                            ) {
                                Column {
                                    NavigationTitle(
                                        title = stringResource(R.string.most_played_albums),
                                        modifier = Modifier.padding(16.dp)
                                    )
                                    LazyRow(
                                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        items(
                                            items = mostPlayedAlbums,
                                            key = { it.id }
                                        ) { album ->
                                            AlbumGridItem(
                                                album = album,
                                                isActive = album.id == mediaMetadata?.album?.id,
                                                isPlaying = isPlaying,
                                                coroutineScope = coroutineScope,
                                                modifier = Modifier.combinedClickable(
                                                    onClick = { navController.navigate("album/${album.id}") },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        menuState.show {
                                                            AlbumMenu(
                                                                originalAlbum = album,
                                                                navController = navController,
                                                                onDismiss = menuState::dismiss
                                                            )
                                                        }
                                                    }
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
