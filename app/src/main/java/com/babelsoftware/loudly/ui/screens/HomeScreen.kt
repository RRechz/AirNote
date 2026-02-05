package com.babelsoftware.loudly.ui.screens

import android.annotation.SuppressLint
import android.graphics.RenderEffect
import android.net.Uri
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.babelsoftware.innertube.models.AlbumItem
import com.babelsoftware.innertube.models.ArtistItem
import com.babelsoftware.innertube.models.PlaylistItem
import com.babelsoftware.innertube.models.SongItem
import com.babelsoftware.innertube.models.WatchEndpoint
import com.babelsoftware.innertube.models.YTItem
import com.babelsoftware.innertube.pages.HomePage
import com.babelsoftware.innertube.utils.parseCookieString
import com.babelsoftware.loudly.BuildConfig
import com.babelsoftware.loudly.LocalDatabase
import com.babelsoftware.loudly.LocalPlayerAwareWindowInsets
import com.babelsoftware.loudly.LocalPlayerConnection
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.AccountNameKey
import com.babelsoftware.loudly.constants.GridThumbnailHeight
import com.babelsoftware.loudly.constants.HeaderImageKey
import com.babelsoftware.loudly.constants.InnerTubeCookieKey
import com.babelsoftware.loudly.constants.ListItemHeight
import com.babelsoftware.loudly.constants.ListThumbnailSize
import com.babelsoftware.loudly.constants.ShowContentFilterKey
import com.babelsoftware.loudly.constants.ShowRecentActivityKey
import com.babelsoftware.loudly.constants.ThumbnailCornerRadius
import com.babelsoftware.loudly.constants.YtmSyncKey
import com.babelsoftware.loudly.constants.showNoInternetDialogKey
import com.babelsoftware.loudly.db.entities.Album
import com.babelsoftware.loudly.db.entities.Artist
import com.babelsoftware.loudly.db.entities.LocalItem
import com.babelsoftware.loudly.db.entities.Playlist
import com.babelsoftware.loudly.db.entities.RecentActivityType
import com.babelsoftware.loudly.db.entities.Song
import com.babelsoftware.loudly.extensions.togglePlayPause
import com.babelsoftware.loudly.isNewerVersion
import com.babelsoftware.loudly.models.toMediaMetadata
import com.babelsoftware.loudly.playback.queues.YouTubeAlbumRadio
import com.babelsoftware.loudly.playback.queues.YouTubeQueue
import com.babelsoftware.loudly.ui.component.AlbumGridItem
import com.babelsoftware.loudly.ui.component.ArtistGridItem
import com.babelsoftware.loudly.ui.component.ChipsRow
import com.babelsoftware.loudly.ui.component.FeaturedContentCard
import com.babelsoftware.loudly.ui.component.HideOnScrollFAB
import com.babelsoftware.loudly.ui.component.LocalMenuState
import com.babelsoftware.loudly.ui.component.NavigationTitle
import com.babelsoftware.loudly.ui.component.SongGridItem
import com.babelsoftware.loudly.ui.component.SongListItem
import com.babelsoftware.loudly.ui.component.YouTubeCardItem
import com.babelsoftware.loudly.ui.component.YouTubeGridItem
import com.babelsoftware.loudly.ui.component.shimmer.GridItemPlaceHolder
import com.babelsoftware.loudly.ui.component.shimmer.ShimmerHost
import com.babelsoftware.loudly.ui.component.shimmer.TextPlaceholder
import com.babelsoftware.loudly.ui.menu.AlbumMenu
import com.babelsoftware.loudly.ui.menu.ArtistMenu
import com.babelsoftware.loudly.ui.menu.SongMenu
import com.babelsoftware.loudly.ui.menu.YouTubeAlbumMenu
import com.babelsoftware.loudly.ui.menu.YouTubeArtistMenu
import com.babelsoftware.loudly.ui.menu.YouTubePlaylistMenu
import com.babelsoftware.loudly.ui.menu.YouTubeSongMenu
import com.babelsoftware.loudly.ui.utils.SnapLayoutInfoProvider
import com.babelsoftware.loudly.utils.getLatestReleaseInfo
import com.babelsoftware.loudly.utils.isInternetAvailable
import com.babelsoftware.loudly.utils.rememberPreference
import com.babelsoftware.loudly.viewmodels.HomeViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.Calendar
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.random.Random

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var updateAvailable by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = Unit) {
        updateAvailable = checkForUpdate()
    }

    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val haptic = LocalHapticFeedback.current

    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val quickPicks by viewModel.quickPicks.collectAsState()
    val forgottenFavorites by viewModel.forgottenFavorites.collectAsState()
    val keepListening by viewModel.keepListening.collectAsState()
    val similarRecommendations by viewModel.similarRecommendations.collectAsState()
    val accountPlaylists by viewModel.accountPlaylists.collectAsState()
    val homePage by viewModel.homePage.collectAsState()
    val explorePage by viewModel.explorePage.collectAsState()
    val playlists by viewModel.playlists.collectAsState()
    val recentActivity by viewModel.recentActivity.collectAsState()

    val (showRecentActivity) = rememberPreference(ShowRecentActivityKey, defaultValue = true)
    val (innerTubeCookie) = rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) {
        "SAPISID" in parseCookieString(innerTubeCookie)
    }

    val isLoading by viewModel.isLoading.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val selectedChip by viewModel.selectedChip.collectAsState()
    val pullRefreshState = rememberPullToRefreshState()

    val forgottenFavoritesLazyGridState = rememberLazyGridState()

    val scope = rememberCoroutineScope()
    val lazylistState = rememberLazyListState()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val scrollToTop = backStackEntry?.savedStateHandle?.getStateFlow("scrollToTop", false)?.collectAsState()
    val (ytmSync) = rememberPreference(YtmSyncKey, true)
    val context = LocalContext.current
    var showNoInternetDialog by rememberPreference(showNoInternetDialogKey, defaultValue = false)

    LaunchedEffect(scrollToTop?.value) {
        if (scrollToTop?.value == true) {
            lazylistState.animateScrollToItem(0)
            backStackEntry?.savedStateHandle?.set("scrollToTop", false)
        }
    }

    LaunchedEffect(Unit) {
        snapshotFlow { lazylistState.layoutInfo.visibleItemsInfo.lastOrNull()?.index }
            .collect { lastVisibleIndex ->
                val len = lazylistState.layoutInfo.totalItemsCount
                if (lastVisibleIndex != null && lastVisibleIndex >= len - 3) {
                    viewModel.loadMoreYouTubeItems(homePage?.continuation)
                }
            }
    }

    if (!isInternetAvailable(context)) {
        LaunchedEffect(Unit) {
            showNoInternetDialog = true
        }
    }

    if (selectedChip != null) {
        BackHandler {
            viewModel.toggleChip(null)
        }
    }

    val localGridItem: @Composable (LocalItem) -> Unit = {
        when (it) {
            is Song -> SongGridItem(
                song = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            if (it.id == mediaMetadata?.id) {
                                playerConnection.player.togglePlayPause()
                            } else {
                                playerConnection.playQueue(
                                    YouTubeQueue.Companion.radio(it.toMediaMetadata()),
                                )
                            }
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(
                                HapticFeedbackType.LongPress,
                            )
                            menuState.show {
                                SongMenu(
                                    originalSong = it,
                                    navController = navController,
                                    onDismiss = menuState::dismiss,
                                )
                            }
                        },
                    ),
                isActive = it.id == mediaMetadata?.id,
                isPlaying = isPlaying,
            )

            is Album -> AlbumGridItem(
                album = it,
                isActive = it.id == mediaMetadata?.album?.id,
                isPlaying = isPlaying,
                coroutineScope = scope,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            navController.navigate("album/${it.id}")
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            menuState.show {
                                AlbumMenu(
                                    originalAlbum = it,
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                )
                            }
                        }
                    )
            )

            is Artist -> ArtistGridItem(
                artist = it,
                modifier = Modifier
                    .fillMaxWidth()
                    .combinedClickable(
                        onClick = {
                            navController.navigate("artist/${it.id}")
                        },
                        onLongClick = {
                            haptic.performHapticFeedback(
                                HapticFeedbackType.LongPress,
                            )
                            menuState.show {
                                ArtistMenu(
                                    originalArtist = it,
                                    coroutineScope = scope,
                                    onDismiss = menuState::dismiss,
                                )
                            }
                        },
                    ),
            )

            is Playlist -> {}
        }
    }

    val ytGridItem: @Composable (YTItem) -> Unit = { item ->
        val itemShape = when (item) {
            is ArtistItem -> CircleShape
            else -> RoundedCornerShape(ThumbnailCornerRadius)
        }
        YouTubeGridItem(
            item = item,
            isActive = item.id in listOf(mediaMetadata?.album?.id, mediaMetadata?.id),
            isPlaying = isPlaying,
            coroutineScope = scope,
            thumbnailRatio = 1f,
            shape = itemShape,
            modifier = Modifier
                .combinedClickable(
                    onClick = {
                        when (item) {
                            is SongItem -> playerConnection.playQueue(
                                YouTubeQueue(
                                    item.endpoint ?: WatchEndpoint(videoId = item.id),
                                    item.toMediaMetadata()
                                )
                            )

                            is AlbumItem -> navController.navigate("album/${item.id}")
                            is ArtistItem -> navController.navigate("artist/${item.id}")
                            is PlaylistItem -> navController.navigate("online_playlist/${item.id}")
                        }
                    },
                    onLongClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuState.show {
                            when (item) {
                                is SongItem -> YouTubeSongMenu(
                                    song = item,
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                )

                                is AlbumItem -> YouTubeAlbumMenu(
                                    albumItem = item,
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                )

                                is ArtistItem -> YouTubeArtistMenu(
                                    artist = item,
                                    onDismiss = menuState::dismiss
                                )

                                is PlaylistItem -> YouTubePlaylistMenu(
                                    navController = navController,
                                    playlist = item,
                                    coroutineScope = scope,
                                    onDismiss = menuState::dismiss
                                )
                            }
                        }
                    }
                )
        )
    }

    LaunchedEffect(forgottenFavorites) {
        forgottenFavoritesLazyGridState.scrollToItem(0)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .pullToRefresh(
                state = pullRefreshState,
                isRefreshing = isRefreshing,
                onRefresh = viewModel::refresh
            ),
        contentAlignment = Alignment.TopStart
    ) {
        val horizontalLazyGridItemWidthFactor = if (maxWidth * 0.475f >= 320.dp) 0.475f else 0.9f
        val horizontalLazyGridItemWidth = maxWidth * horizontalLazyGridItemWidthFactor
        val forgottenFavoritesSnapLayoutInfoProvider = remember(forgottenFavoritesLazyGridState) {
            SnapLayoutInfoProvider(
                lazyGridState = forgottenFavoritesLazyGridState,
                positionInLayout = { layoutSize, itemSize ->
                    (layoutSize * horizontalLazyGridItemWidthFactor / 2f - itemSize / 2f)
                }
            )
        }

        val recentActivityGridState = rememberLazyGridState()
        val (showContentFilter) = rememberPreference(ShowContentFilterKey, defaultValue = true)

        if (showNoInternetDialog) {
            AlertDialog(
                onDismissRequest = { showNoInternetDialog = false },
                shape = RoundedCornerShape(28.dp),
                title = {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.secondaryContainer,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.signal_cellular_nodata),
                                contentDescription = null,
                                modifier = Modifier.size(36.dp),
                                tint = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = stringResource(R.string.not_internet),
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                },
                text = {
                    Text(
                        text = stringResource(R.string.internet_required),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showNoInternetDialog = false
                            navController.navigate("library")
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(stringResource(R.string.filter_library))
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { showNoInternetDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                    ) {
                        Text(stringResource(R.string.close))
                    }
                }
            )
        }


        LazyColumn(
            state = lazylistState,
            contentPadding = LocalPlayerAwareWindowInsets.current
                .only(WindowInsetsSides.Vertical)
                .asPaddingValues(LocalDensity.current)
        ) {
            item {
                val currentChips = homePage?.chips
                if (!currentChips.isNullOrEmpty() && showContentFilter) {
                    ChipsRow(
                        chips = currentChips.mapNotNull { chip ->
                            chip?.let { it to it.title }
                        },
                        currentValue = selectedChip,
                        onValueUpdate = {
                            viewModel.toggleChip(it)
                        }
                    )
                }
            }

            if (selectedChip == null) {
                item {
                    HomeGreetingCard(
                        updateAvailable = updateAvailable,
                        modifier = Modifier
                            .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 16.dp)
                            .animateItem()
                    )
                }
            }

            if (selectedChip == null && showRecentActivity && isLoggedIn && !recentActivity.isNullOrEmpty()) {
                item {
                    NavigationTitle(title = stringResource(R.string.recent_activity))
                }
                item {
                    LazyHorizontalGrid(
                        state = recentActivityGridState,
                        rows = GridCells.Fixed(4),
                        flingBehavior = rememberSnapFlingBehavior(
                            forgottenFavoritesLazyGridState
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp * 4)
                            .padding(6.dp)
                    ) {
                        items(
                            items = recentActivity!!,
                            key = { it.id }
                        ) { item ->
                            YouTubeCardItem(
                                item,
                                onClick = {
                                    when (item.type) {
                                        RecentActivityType.PLAYLIST -> {
                                            val playlistDb = playlists
                                                ?.firstOrNull { it.playlist.browseId == item.id }

                                            if (playlistDb != null && playlistDb.songCount != 0)
                                                navController.navigate("local_playlist/${playlistDb.id}")
                                            else
                                                navController.navigate("online_playlist/${item.id}")
                                        }

                                        RecentActivityType.ALBUM -> navController.navigate("album/${item.id}")

                                        RecentActivityType.ARTIST -> navController.navigate("artist/${item.id}")
                                    }
                                },
                            )
                        }
                    }
                }
            }

            if (selectedChip == null) {
                quickPicks?.takeIf { it.isNotEmpty() }?.let { quickPicks ->
                    item {
                        NavigationTitle(
                            title = stringResource(R.string.quick_picks),
                            modifier = Modifier.animateItem()
                        )
                    }

                    item {
                        val chunkedQuickPicks = quickPicks.chunked(4)
                        val pagerState = rememberPagerState { chunkedQuickPicks.size }
                        val itemWidth = horizontalLazyGridItemWidth
                        val contentPadding = (maxWidth - itemWidth) / 2

                        LaunchedEffect(quickPicks) {
                            pagerState.scrollToPage(0)
                        }

                        HorizontalPager(
                            state = pagerState,
                            pageSize = PageSize.Fixed(itemWidth),
                            contentPadding = PaddingValues(horizontal = contentPadding),
                            pageSpacing = 8.dp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateItem()
                        ) { page ->
                            val pageOffset = (
                                    (pagerState.currentPage - page) + pagerState
                                        .currentPageOffsetFraction
                                    ).absoluteValue

                            val scaleFactor = lerp(0.7f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                            val alphaFactor = lerp(0.3f, 1f, 1f - pageOffset.coerceIn(0f, 1f))
                            val blurFactor = lerp(8f, 0f, 1f - pageOffset.coerceIn(0f, 1f))

                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.0f)
                                ),
                                modifier = Modifier
                                    .width(itemWidth)
                                    .graphicsLayer {
                                        scaleX = scaleFactor
                                        scaleY = scaleFactor
                                        transformOrigin = TransformOrigin.Center
                                        alpha = alphaFactor

                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                            renderEffect = if (blurFactor > 0) {
                                                RenderEffect
                                                    .createBlurEffect(
                                                        blurFactor, blurFactor,
                                                        android.graphics.Shader.TileMode.DECAL
                                                    )
                                                    .asComposeRenderEffect()
                                            } else {
                                                null
                                            }
                                        }
                                    }
                            ) {
                                Column {
                                    val songChunk = chunkedQuickPicks[page]
                                    songChunk.forEach { originalSong ->
                                        val song by database.song(originalSong.id)
                                            .collectAsState(initial = originalSong)

                                        SongListItem(
                                            song = song!!,
                                            showInLibraryIcon = true,
                                            isActive = song!!.id == mediaMetadata?.id,
                                            isPlaying = isPlaying,
                                            isSwipeable = false,
                                            trailingContent = {
                                                IconButton(
                                                    onClick = {
                                                        menuState.show {
                                                            SongMenu(
                                                                originalSong = song!!,
                                                                navController = navController,
                                                                onDismiss = menuState::dismiss
                                                            )
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        painter = painterResource(R.drawable.more_vert),
                                                        contentDescription = null
                                                    )
                                                }
                                            },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .combinedClickable(
                                                    onClick = {
                                                        if (song!!.id == mediaMetadata?.id) {
                                                            playerConnection.player.togglePlayPause()
                                                        } else {
                                                            playerConnection.playQueue(
                                                                YouTubeQueue.Companion.radio(
                                                                    song!!.toMediaMetadata()
                                                                )
                                                            )
                                                        }
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        menuState.show {
                                                            SongMenu(
                                                                originalSong = song!!,
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


                forgottenFavorites?.takeIf { it.isNotEmpty() }?.let { forgottenFavorites ->
                    item {
                        NavigationTitle(
                            title = stringResource(R.string.forgotten_favorites),
                            modifier = Modifier.animateItem()
                        )
                    }

                    item {
                        val rows = min(4, forgottenFavorites.size)
                        LazyHorizontalGrid(
                            state = forgottenFavoritesLazyGridState,
                            rows = GridCells.Fixed(rows),
                            flingBehavior = rememberSnapFlingBehavior(
                                forgottenFavoritesSnapLayoutInfoProvider
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(ListItemHeight * rows)
                                .animateItem()
                        ) {
                            items(
                                items = forgottenFavorites,
                                key = { it.id }
                            ) { originalSong ->
                                val song by database.song(originalSong.id)
                                    .collectAsState(initial = originalSong)

                                SongListItem(
                                    song = song!!,
                                    showInLibraryIcon = true,
                                    isActive = song!!.id == mediaMetadata?.id,
                                    isPlaying = isPlaying,
                                    isSwipeable = false,
                                    modifier = Modifier
                                        .width(horizontalLazyGridItemWidth)
                                        .combinedClickable(
                                            onClick = {
                                                if (song!!.id == mediaMetadata?.id) {
                                                    playerConnection.player.togglePlayPause()
                                                } else {
                                                    playerConnection.playQueue(
                                                        YouTubeQueue.Companion.radio(
                                                            song!!.toMediaMetadata()
                                                        )
                                                    )
                                                }
                                            },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                menuState.show {
                                                    SongMenu(
                                                        originalSong = song!!,
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

                keepListening?.takeIf { it.isNotEmpty() }?.let { fullList ->
                    val featuredItem = fullList.firstOrNull { it is Album || it is Artist }
                    val remainingItems = if (featuredItem != null) fullList.minus(featuredItem) else fullList
                    if (featuredItem != null) {
                        item {
                            FeaturedContentCard(
                                item = featuredItem,
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                                    .animateItem(),
                                onClick = {
                                    when (featuredItem) {
                                        is Album -> navController.navigate("album/${featuredItem.id}")
                                        is Artist -> navController.navigate("artist/${featuredItem.id}")
                                        else -> {
                                        }
                                    }
                                }
                            )
                        }
                    }

                    if (remainingItems.isNotEmpty()) {
                        item {
                            NavigationTitle(
                                title = stringResource(R.string.keep_listening),
                                modifier = Modifier.animateItem()
                            )
                        }
                        item {
                            val rows = if (remainingItems.size > 6) 2 else 1
                            LazyHorizontalGrid(
                                state = rememberLazyGridState(),
                                rows = GridCells.Fixed(rows),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height((GridThumbnailHeight + 24.dp + with(LocalDensity.current) {
                                        MaterialTheme.typography.bodyLarge.lineHeight.toDp() * 2 +
                                                MaterialTheme.typography.bodyMedium.lineHeight.toDp() * 2
                                    }) * rows)
                                    .animateItem()
                            ) {
                                items(remainingItems) {
                                    localGridItem(it)
                                }
                            }
                        }
                    }
                }
            }

            if (ytmSync && selectedChip == null) {
                accountPlaylists?.takeIf { it.isNotEmpty() }?.let { accountPlaylists ->
                    item {
                        NavigationTitle(
                            title = stringResource(R.string.your_youtube_playlists),
                            onClick = {
                                navController.navigate("YouTubePlaylists")
                            },
                            modifier = Modifier.animateItem()
                        )
                    }

                    item {
                        LazyRow(
                            contentPadding = WindowInsets.systemBars
                                .only(WindowInsetsSides.Horizontal)
                                .asPaddingValues(),
                            modifier = Modifier.animateItem()
                        ) {
                            items(
                                items = accountPlaylists,
                                key = { it.id },
                            ) { item ->
                                ytGridItem(item)
                            }
                        }
                    }
                }
            }

            if (selectedChip == null) {
                similarRecommendations?.forEach {
                    item {
                        NavigationTitle(
                            label = stringResource(R.string.similar_to),
                            title = it.title.title,
                            thumbnail = it.title.thumbnailUrl?.let { thumbnailUrl ->
                                {
                                    val shape = RoundedCornerShape(ThumbnailCornerRadius)
                                    AsyncImage(
                                        model = thumbnailUrl,
                                        contentDescription = null,
                                        modifier = Modifier.Companion
                                            .size(ListThumbnailSize)
                                            .clip(shape)
                                    )
                                }
                            },
                            onClick = {
                                when (it.title) {
                                    is Song -> navController.navigate("album/${it.title.album!!.id}")
                                    is Album -> navController.navigate("album/${it.title.id}")
                                    is Artist -> navController.navigate("artist/${it.title.id}")
                                    is Playlist -> {}
                                }
                            },
                            modifier = Modifier.animateItem()
                        )
                    }

                    item {
                        LazyRow(
                            contentPadding = WindowInsets.systemBars
                                .only(WindowInsetsSides.Horizontal)
                                .asPaddingValues(),
                            modifier = Modifier.animateItem()
                        ) {
                            items(it.items) { item ->
                                ytGridItem(item)
                            }
                        }
                    }
                }
            }

            homePage?.sections?.forEach {
                item {
                    NavigationTitle(
                        title = it.title,
                        label = it.label,
                        thumbnail = it.thumbnail?.let { thumbnailUrl ->
                            {
                                val shape = RoundedCornerShape(ThumbnailCornerRadius)
                                AsyncImage(
                                    model = thumbnailUrl,
                                    contentDescription = null,
                                    modifier = Modifier.Companion
                                        .size(ListThumbnailSize)
                                        .clip(shape)
                                )
                            }
                        },
                        modifier = Modifier.animateItem()
                    )
                }

                item {
                    when (it.sectionType) {
                        HomePage.SectionType.LIST -> {
                            LazyRow(
                                contentPadding = WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues(),
                                modifier = Modifier.animateItem()
                            ) {
                                items(it.items) { item ->
                                    ytGridItem(item)
                                }
                            }
                        }

                        HomePage.SectionType.GRID -> {
                            LazyRow (
                                contentPadding = WindowInsets.systemBars
                                    .only(WindowInsetsSides.Horizontal)
                                    .asPaddingValues(),
                                modifier = Modifier.animateItem()
                            ) {
                                items(it.items) { item ->
                                    ytGridItem(item)
                                }
                            }
                        }
                    }
                }
            }

            if (isLoading) {
                item {
                    ShimmerHost(
                        modifier = Modifier.animateItem()
                    ) {
                        TextPlaceholder(
                            height = 36.dp,
                            modifier = Modifier
                                .padding(12.dp)
                                .width(250.dp),
                        )
                        LazyRow {
                            items(4) {
                                GridItemPlaceHolder()
                            }
                        }
                    }
                }
            }
        }

        HideOnScrollFAB(
            visible = !quickPicks.isNullOrEmpty() || explorePage?.newReleaseAlbums?.isNotEmpty() == true,
            lazyListState = lazylistState,
            icon = (R.drawable.casino),
            onClick = {
                if (Random.nextBoolean() && !quickPicks.isNullOrEmpty()) {
                    val song = quickPicks!!.random()
                    playerConnection.playQueue(
                        YouTubeQueue(
                            WatchEndpoint(videoId = song.id),
                            song.toMediaMetadata()
                        )
                    )
                } else if (explorePage?.newReleaseAlbums?.isNotEmpty() == true) {
                    val album = explorePage?.newReleaseAlbums!!.random()
                    playerConnection.playQueue(YouTubeAlbumRadio(album.playlistId))
                }
            }
        )

        Indicator(
            isRefreshing = isRefreshing,
            state = pullRefreshState,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(LocalPlayerAwareWindowInsets.current.asPaddingValues()),
        )
    }
}

@Composable
private fun HomeGreetingCard(
    modifier: Modifier = Modifier,
    updateAvailable: Boolean
) {
    val accountName by rememberPreference(AccountNameKey, "")
    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) { "SAPISID" in parseCookieString(innerTubeCookie) }
    val (headerImageKey, _) = rememberPreference(HeaderImageKey, "Loudly-1")

    val painter = when {
        headerImageKey.startsWith("content://") -> {
            rememberAsyncImagePainter(model = Uri.parse(headerImageKey))
        }
        headerImageKey == "Loudly-1" -> painterResource(id = R.drawable.loudly_picutre_1)
        headerImageKey == "Loudly-2" -> painterResource(id = R.drawable.loudly_picutre_2)
        headerImageKey == "Loudly-3" -> painterResource(id = R.drawable.loudly_picutre_3)
        headerImageKey == "Loudly-4" -> painterResource(id = R.drawable.loudly_picutre_4)
        headerImageKey == "Loudly-5" -> painterResource(id = R.drawable.loudly_picutre_5)
        headerImageKey == "Loudly-6" -> painterResource(id = R.drawable.loudly_picutre_6)
        else -> painterResource(id = R.drawable.loudly_picutre_1)
    }

    val currentHour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when (currentHour) {
        in 6..11 -> stringResource(R.string.good_morning)
        in 12..17 -> stringResource(R.string.good_afternoon)
        in 18..23 -> stringResource(R.string.good_evening)
        else -> stringResource(R.string.good_night)
    }

    var showUpdateMessage by remember { mutableStateOf(false) }
    LaunchedEffect(updateAvailable) {
        showUpdateMessage = false
        if (updateAvailable) {
            delay(3000)
            showUpdateMessage = true
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(32.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomStart
        ) {
            Image(
                painter = painter,
                contentDescription = "Header Background",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f)),
                            startY = 150f
                        )
                    )
            )
            AnimatedContent(
                targetState = showUpdateMessage,
                modifier = Modifier.padding(24.dp),
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn())
                        .togetherWith(slideOutVertically { height -> -height } + fadeOut())
                },
                label = "GreetingCardAnimation"
            ) { shouldShowUpdate ->
                Column(
                    verticalArrangement = Arrangement.Bottom,
                    horizontalAlignment = if (shouldShowUpdate) Alignment.CenterHorizontally else Alignment.Start,
                    modifier = if (shouldShowUpdate) Modifier.fillMaxWidth() else Modifier.fillMaxSize()
                ) {
                    if (!shouldShowUpdate) {
                        if (isLoggedIn && accountName.isNotBlank()) {
                            Text(
                                text = greeting,
                                color = Color.White.copy(alpha = 0.9f),
                                style = MaterialTheme.typography.headlineMedium,
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = accountName.replace("@", ""),
                                color = Color.White,
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                stringResource(R.string.welcome_to_loudly),
                                color = Color.White,
                                style = MaterialTheme.typography.displayMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else {
                        Text(
                            text = stringResource(R.string.new_version_available),
                            color = Color.White,
                            style = MaterialTheme.typography.displaySmall,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.home_card_update_desc),
                            color = Color.White.copy(alpha = 0.9f),
                            style = MaterialTheme.typography.headlineSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

private suspend fun checkForUpdate(): Boolean {
    return withContext(Dispatchers.IO) {
        try {
            val latestInfo = getLatestReleaseInfo()
            if (latestInfo != null) {
                isNewerVersion(latestInfo.tagName, BuildConfig.VERSION_NAME)
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}