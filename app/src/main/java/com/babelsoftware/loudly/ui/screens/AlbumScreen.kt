@file:Suppress("NAME_SHADOWING")

package com.babelsoftware.loudly.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEachIndexed
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.babelsoftware.loudly.LocalDatabase
import com.babelsoftware.loudly.LocalDownloadUtil
import com.babelsoftware.loudly.LocalPlayerAwareWindowInsets
import com.babelsoftware.loudly.LocalPlayerConnection
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.AlbumThumbnailSize
import com.babelsoftware.loudly.constants.PlayerStyle
import com.babelsoftware.loudly.constants.ThumbnailCornerRadius
import com.babelsoftware.loudly.db.entities.Album
import com.babelsoftware.loudly.db.entities.AlbumWithSongs
import com.babelsoftware.loudly.extensions.togglePlayPause
import com.babelsoftware.loudly.playback.ExoDownloadService
import com.babelsoftware.loudly.playback.queues.LocalAlbumRadio
import com.babelsoftware.loudly.ui.component.AutoResizeText
import com.babelsoftware.loudly.ui.component.FontSizeRange
import com.babelsoftware.loudly.ui.component.IconButton
import com.babelsoftware.loudly.ui.component.LocalMenuState
import com.babelsoftware.loudly.ui.component.NavigationTitle
import com.babelsoftware.loudly.ui.component.SongListItem
import com.babelsoftware.loudly.ui.component.YouTubeGridItem
import com.babelsoftware.loudly.ui.component.shimmer.ButtonPlaceholder
import com.babelsoftware.loudly.ui.component.shimmer.ListItemPlaceHolder
import com.babelsoftware.loudly.ui.component.shimmer.ShimmerHost
import com.babelsoftware.loudly.ui.component.shimmer.TextPlaceholder
import com.babelsoftware.loudly.ui.menu.AlbumMenu
import com.babelsoftware.loudly.ui.menu.SongMenu
import com.babelsoftware.loudly.ui.menu.SongSelectionMenu
import com.babelsoftware.loudly.ui.menu.YouTubeAlbumMenu
import com.babelsoftware.loudly.ui.utils.backToMain
import com.babelsoftware.loudly.viewmodels.AlbumViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: AlbumViewModel = hiltViewModel(),
    playerStyle: PlayerStyle
) {
    if (playerStyle == PlayerStyle.UI_2_0) {
        AlbumScreenUI20(navController = navController, scrollBehavior = scrollBehavior, viewModel = viewModel)
    } else {
        AlbumScreenLegacy(navController = navController, scrollBehavior = scrollBehavior, viewModel = viewModel)
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AlbumScreenLegacy(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: AlbumViewModel,
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val database = LocalDatabase.current
    val playerConnection = LocalPlayerConnection.current ?: return

    val scope = rememberCoroutineScope()

    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val albumWithSongs by viewModel.albumWithSongs.collectAsState()
    val otherVersions by viewModel.otherVersions.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val downloadUtil = LocalDownloadUtil.current
    var downloadState by remember {
        mutableIntStateOf(Download.STATE_STOPPED)
    }

    val state = rememberLazyListState()
    val showTopBarTitle by remember {
        derivedStateOf {
            state.firstVisibleItemIndex > 0
        }
    }

    var inSelectMode by rememberSaveable { mutableStateOf(false) }
    val selection = rememberSaveable(
        saver = listSaver<MutableList<Int>, Int>(
            save = { it.toList() },
            restore = { it.toMutableStateList() }
        )
    ) { mutableStateListOf() }
    val onExitSelectionMode = {
        inSelectMode = false
        selection.clear()
    }
    if (inSelectMode) {
        BackHandler(onBack = onExitSelectionMode)
    }

    LaunchedEffect(albumWithSongs) {
        val songs = albumWithSongs?.songs?.map { it.id }
        if (songs.isNullOrEmpty()) return@LaunchedEffect
        downloadUtil.downloads.collect { downloads ->
            downloadState =
                if (songs.all { downloads[it]?.state == Download.STATE_COMPLETED })
                    Download.STATE_COMPLETED
                else if (songs.all {
                        downloads[it]?.state == Download.STATE_QUEUED
                                || downloads[it]?.state == Download.STATE_DOWNLOADING
                                || downloads[it]?.state == Download.STATE_COMPLETED
                    })
                    Download.STATE_DOWNLOADING
                else
                    Download.STATE_STOPPED
        }
    }

    LazyColumn(
        state = state,
        contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
    ) {
        val currentAlbumWithSongs = albumWithSongs
        if (currentAlbumWithSongs != null && currentAlbumWithSongs.songs.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    AsyncImage(
                        model = currentAlbumWithSongs.album.thumbnailUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(AlbumThumbnailSize)
                            .clip(RoundedCornerShape(ThumbnailCornerRadius))
                            .align(alignment = Alignment.CenterHorizontally)
                    )

                    Spacer(Modifier.height(12.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AutoResizeText(
                            text = currentAlbumWithSongs.album.title,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            fontSizeRange = FontSizeRange(16.sp, 22.sp)
                        )

                        Text(buildAnnotatedString {
                            withStyle(
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Normal,
                                    color = MaterialTheme.colorScheme.onBackground
                                ).toSpanStyle()
                            ) {
                                currentAlbumWithSongs.artists.fastForEachIndexed { index, artist ->
                                    val link = LinkAnnotation.Clickable(artist.id) {
                                        navController.navigate("artist/${artist.id}")
                                    }
                                    withLink(link) {
                                        append(artist.name)
                                    }
                                    if (index != currentAlbumWithSongs.artists.lastIndex) {
                                        append(", ")
                                    }
                                }
                            }
                        })

                        if (currentAlbumWithSongs.album.year != null) {
                            Text(
                                text = currentAlbumWithSongs.album.year.toString(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Normal
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Button(
                            onClick = {
                                playerConnection.playQueue(
                                    LocalAlbumRadio(currentAlbumWithSongs)
                                )
                            },
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.play),
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(
                                text = stringResource(R.string.play)
                            )
                        }

                        Button(
                            onClick = {
                                playerConnection.playQueue(
                                    LocalAlbumRadio(currentAlbumWithSongs.copy(songs = currentAlbumWithSongs.songs.shuffled()))
                                )
                            },
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.shuffle),
                                contentDescription = null,
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(stringResource(R.string.shuffle))
                        }
                    }
                }
            }

            itemsIndexed(
                items = currentAlbumWithSongs.songs,
                key = { _, song -> song.id }
            ) { index, song ->
                val onCheckedChange: (Boolean) -> Unit = {
                    if (it) {
                        selection.add(index)
                    } else {
                        selection.remove(index)
                    }
                }
                if (index == 0) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = pluralStringResource(
                                R.plurals.n_song,
                                currentAlbumWithSongs.songs.size,
                                currentAlbumWithSongs.songs.size
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Normal
                        )
                    }
                }
                SongListItem(
                    song = song,
                    albumIndex = index + 1,
                    isActive = song.id == mediaMetadata?.id,
                    isPlaying = isPlaying,
                    showInLibraryIcon = true,
                    trailingContent = {
                        if (inSelectMode) {
                            Checkbox(
                                checked = index in selection,
                                onCheckedChange = onCheckedChange
                            )
                        } else {
                            IconButton(
                                onClick = {
                                    menuState.show {
                                        SongMenu(
                                            originalSong = song,
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
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .combinedClickable(
                            onClick = {
                                if (inSelectMode) {
                                    onCheckedChange(index !in selection)
                                } else if (song.id == mediaMetadata?.id) {
                                    playerConnection.player.togglePlayPause()
                                } else {
                                    playerConnection.playQueue(
                                        LocalAlbumRadio(currentAlbumWithSongs, startIndex = index)
                                    )
                                }
                            },
                            onLongClick = {
                                if (!inSelectMode) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    inSelectMode = true
                                    onCheckedChange(true)
                                }
                            }
                        )
                )
            }

            if (otherVersions.isNotEmpty()) {
                item {
                    NavigationTitle(
                        title = stringResource(R.string.other_versions),
                    )
                }
                item {
                    LazyRow {
                        items(
                            items = otherVersions,
                            key = { it.id },
                        ) { item ->
                            YouTubeGridItem(
                                item = item,
                                isActive = mediaMetadata?.album?.id == item.id,
                                isPlaying = isPlaying,
                                coroutineScope = scope,
                                modifier =
                                    Modifier
                                        .combinedClickable(
                                            onClick = { navController.navigate("album/${item.id}") },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                menuState.show {
                                                    YouTubeAlbumMenu(
                                                        albumItem = item,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                }
                                            },
                                        )
                                        .animateItem(),
                            )
                        }
                    }
                }
            }
        } else {
            item {
                ShimmerHost {
                    Column(Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Spacer(
                                modifier = Modifier
                                    .size(AlbumThumbnailSize)
                                    .clip(RoundedCornerShape(ThumbnailCornerRadius))
                                    .background(MaterialTheme.colorScheme.onSurface)
                            )

                            Spacer(Modifier.width(16.dp))

                            Column(
                                verticalArrangement = Arrangement.Center,
                            ) {
                                TextPlaceholder()
                                TextPlaceholder()
                                TextPlaceholder()
                            }
                        }

                        Spacer(Modifier.padding(8.dp))

                        Row {
                            ButtonPlaceholder(Modifier.weight(1f))

                            Spacer(Modifier.width(12.dp))

                            ButtonPlaceholder(Modifier.weight(1f))
                        }
                    }

                    repeat(6) {
                        ListItemPlaceHolder()
                    }
                }
            }
        }
    }

    TopAppBar(
        title = {
            if (inSelectMode) {
                Text(pluralStringResource(R.plurals.n_selected, selection.size, selection.size))
            } else if (showTopBarTitle) {
                Text(albumWithSongs?.album?.title.orEmpty())
            }
        },
        navigationIcon = {
            if (inSelectMode) {
                IconButton(onClick = onExitSelectionMode) {
                    Icon(
                        painter = painterResource(R.drawable.close),
                        contentDescription = null,
                    )
                }
            } else {
                IconButton(
                    onClick = navController::navigateUp,
                    onLongClick = navController::backToMain
                ) {
                    Icon(
                        painterResource(R.drawable.arrow_back),
                        contentDescription = null
                    )
                }
            }
        },
        actions = {
            if (inSelectMode) {
                Checkbox(
                    checked = selection.size == albumWithSongs?.songs?.size,
                    onCheckedChange = {
                        albumWithSongs?.let { currentAlbumWithSongs ->
                            if (selection.size == currentAlbumWithSongs.songs.size) {
                                selection.clear()
                            } else {
                                selection.clear()
                                selection.addAll(currentAlbumWithSongs.songs.indices)
                            }
                        }
                    }
                )
                IconButton(
                    enabled = selection.isNotEmpty(),
                    onClick = {
                        menuState.show {
                            SongSelectionMenu(
                                navController = navController,
                                selection = selection.mapNotNull { index ->
                                    albumWithSongs?.songs?.getOrNull(index)
                                },
                                onDismiss = menuState::dismiss,
                                onExitSelectionMode = onExitSelectionMode
                            )
                        }
                    }
                ) {
                    Icon(
                        painterResource(R.drawable.more_vert),
                        contentDescription = null
                    )
                }
            } else {
                IconButton(onClick = {
                    albumWithSongs?.album?.let { album ->
                        database.query { update(album.toggleLike()) }
                    }
                }) {
                    Icon(
                        painter = painterResource(if (albumWithSongs?.album?.bookmarkedAt != null) R.drawable.favorite else R.drawable.favorite_border),
                        contentDescription = stringResource(R.string.favorite)
                    )
                }

                when (downloadState) {
                    Download.STATE_COMPLETED -> {
                        IconButton(
                            onClick = {
                                albumWithSongs?.let { albumData ->
                                    albumData.songs.forEach { song ->
                                        DownloadService.sendRemoveDownload(
                                            context,
                                            ExoDownloadService::class.java,
                                            song.id,
                                            false
                                        )
                                    }
                                }
                            },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.offline),
                                contentDescription = stringResource(R.string.remove_download),
                            )
                        }
                    }

                    Download.STATE_DOWNLOADING -> {
                        IconButton(
                            onClick = {
                                albumWithSongs?.let { albumData ->
                                    albumData.songs.forEach { song ->
                                        DownloadService.sendRemoveDownload(
                                            context,
                                            ExoDownloadService::class.java,
                                            song.id,
                                            false
                                        )
                                    }
                                }
                            }
                        ) {
                            CircularProgressIndicator(
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(24.dp),
                            )
                        }
                    }

                    else -> {
                        IconButton(
                            onClick = {
                                albumWithSongs?.let { albumData ->
                                    albumData.songs.forEach { song ->
                                        val downloadRequest = DownloadRequest.Builder(
                                            song.id,
                                            song.id.toUri()
                                        )
                                            .setCustomCacheKey(song.id)
                                            .setData(song.song.title.toByteArray())
                                            .build()
                                        DownloadService.sendAddDownload(
                                            context,
                                            ExoDownloadService::class.java,
                                            downloadRequest,
                                            false
                                        )
                                    }
                                }
                            },
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.download),
                                contentDescription = stringResource(R.string.download)
                            )
                        }
                    }
                }
                IconButton(
                    onClick = {
                        albumWithSongs?.let { albumData ->
                            menuState.show {
                                AlbumMenu(
                                    originalAlbum = Album(
                                        albumData.album,
                                        albumData.artists
                                    ),
                                    navController = navController,
                                    onDismiss = menuState::dismiss
                                )
                            }
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.more_vert),
                        contentDescription = stringResource(R.string.menu)
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent,
            scrolledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
        )
    )
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
                .align(Alignment.BottomCenter)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun AlbumScreenUI20(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    viewModel: AlbumViewModel
) {
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return

    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val albumWithSongs by viewModel.albumWithSongs.collectAsState()
    val otherVersions by viewModel.otherVersions.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    val downloadUtil = LocalDownloadUtil.current
    var downloadState by remember { mutableIntStateOf(Download.STATE_STOPPED) }

    val state = rememberLazyListState()
    val showTopBarTitle by remember { derivedStateOf { state.firstVisibleItemIndex > 0 } }

    var inSelectMode by rememberSaveable { mutableStateOf(false) }
    val selection = rememberSaveable(
        saver = listSaver<MutableList<Int>, Int>(save = { it.toList() }, restore = { it.toMutableStateList() })
    ) { mutableStateListOf() }
    val onExitSelectionMode = {
        inSelectMode = false
        selection.clear()
    }
    if (inSelectMode) {
        BackHandler(onBack = onExitSelectionMode)
    }

    LaunchedEffect(albumWithSongs) {
        val songs = albumWithSongs?.songs?.map { it.id }
        if (songs.isNullOrEmpty()) return@LaunchedEffect
        downloadUtil.downloads.collect { downloads ->
            downloadState =
                if (songs.all { downloads[it]?.state == Download.STATE_COMPLETED }) Download.STATE_COMPLETED
                else if (songs.all {
                        downloads[it]?.state == Download.STATE_QUEUED
                                || downloads[it]?.state == Download.STATE_DOWNLOADING
                                || downloads[it]?.state == Download.STATE_COMPLETED
                    }) Download.STATE_DOWNLOADING
                else Download.STATE_STOPPED
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val albumData = albumWithSongs
            if (albumData != null) {
                AsyncImage(
                    model = albumData.album.thumbnailUrl,
                    contentDescription = "Album Background",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .blur(70.dp)
                        .alpha(0.55f)
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                )
            }

            LazyColumn(
                state = state,
                contentPadding = LocalPlayerAwareWindowInsets.current.asPaddingValues()
            ) {
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
                if (albumData != null && albumData.songs.isNotEmpty()) {
                    item {
                        AlbumHeaderUI20(
                            albumWithSongs = albumData,
                            navController = navController,
                            downloadState = downloadState,
                            onDownloadClick = {
                                if (downloadState == Download.STATE_COMPLETED || downloadState == Download.STATE_DOWNLOADING) {
                                    albumData.songs.forEach { song ->
                                        DownloadService.sendRemoveDownload(context, ExoDownloadService::class.java, song.id, false)
                                    }
                                } else {
                                    albumData.songs.forEach { song ->
                                        val downloadRequest = DownloadRequest.Builder(song.id, song.id.toUri())
                                            .setCustomCacheKey(song.id)
                                            .setData(song.song.title.toByteArray())
                                            .build()
                                        DownloadService.sendAddDownload(context, ExoDownloadService::class.java, downloadRequest, false)
                                    }
                                }
                            }
                        )
                    }

                    item {
                        Text(
                            text = stringResource(R.string.top_songs),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(start = 16.dp, top = 24.dp, bottom = 8.dp, end = 16.dp)
                        )
                    }

                    itemsIndexed(
                        items = albumData.songs,
                        key = { _, song -> song.id }
                    ) { index, song ->
                        val onCheckedChange: (Boolean) -> Unit = {
                            if (it) selection.add(index) else selection.remove(index)
                        }
                        SongListItem(
                            song = song,
                            albumIndex = index + 1,
                            isActive = song.id == mediaMetadata?.id,
                            isPlaying = isPlaying,
                            showInLibraryIcon = true,
                            trailingContent = {
                                if (inSelectMode) {
                                    Checkbox(checked = index in selection, onCheckedChange = onCheckedChange)
                                } else {
                                    IconButton(onClick = {
                                        menuState.show {
                                            SongMenu(
                                                originalSong = song,
                                                navController = navController,
                                                onDismiss = menuState::dismiss
                                            )
                                        }
                                    }) {
                                        Icon(painterResource(R.drawable.more_vert), contentDescription = null)
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        if (inSelectMode) {
                                            onCheckedChange(index !in selection)
                                        } else if (song.id == mediaMetadata?.id) {
                                            playerConnection.player.togglePlayPause()
                                        } else {
                                            playerConnection.playQueue(
                                                LocalAlbumRadio(albumData, startIndex = index)
                                            )
                                        }
                                    },
                                    onLongClick = {
                                        if (!inSelectMode) {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            inSelectMode = true
                                            onCheckedChange(true)
                                        }
                                    }
                                )
                        )
                    }

                    if (otherVersions.isNotEmpty()) {
                        item {
                            NavigationTitle(
                                title = stringResource(R.string.other_versions),
                                modifier = Modifier.padding(top = 16.dp)
                            )
                        }
                        item {
                            LazyRow(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                items(
                                    items = otherVersions,
                                    key = { it.id },
                                ) { item ->
                                    YouTubeGridItem(
                                        item = item,
                                        isActive = mediaMetadata?.album?.id == item.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = rememberCoroutineScope(),
                                        modifier = Modifier.combinedClickable(
                                            onClick = { navController.navigate("album/${item.id}") },
                                            onLongClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                menuState.show {
                                                    YouTubeAlbumMenu(
                                                        albumItem = item,
                                                        navController = navController,
                                                        onDismiss = menuState::dismiss,
                                                    )
                                                }
                                            },
                                        )
                                    )
                                }
                            }
                        }
                    }

                } else {
                    item {
                        ShimmerHost {
                            // Shimmer placeholders for UI 2.0
                        }
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = {
                        if (inSelectMode) {
                            Text(pluralStringResource(R.plurals.n_selected, selection.size, selection.size))
                        } else if (showTopBarTitle) {
                            Text(albumWithSongs?.album?.title.orEmpty(), maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    },
                    navigationIcon = {
                        if (inSelectMode) {
                            IconButton(onClick = onExitSelectionMode) {
                                Icon(painter = painterResource(R.drawable.close), contentDescription = null)
                            }
                        } else {
                            IconButton(onClick = navController::navigateUp, onLongClick = navController::backToMain) {
                                Icon(painterResource(R.drawable.arrow_back), contentDescription = null)
                            }
                        }
                    },
                    actions = {
                        if (inSelectMode) {
                            Checkbox(
                                checked = selection.size == albumWithSongs?.songs?.size,
                                onCheckedChange = {
                                    albumWithSongs?.let { data ->
                                        if (selection.size == data.songs.size) selection.clear()
                                        else {
                                            selection.clear()
                                            selection.addAll(data.songs.indices)
                                        }
                                    }
                                }
                            )
                            IconButton(
                                enabled = selection.isNotEmpty(),
                                onClick = {
                                    menuState.show {
                                        SongSelectionMenu(
                                            navController = navController,
                                            selection = selection.mapNotNull { index -> albumWithSongs?.songs?.getOrNull(index) },
                                            onDismiss = menuState::dismiss,
                                            onExitSelectionMode = onExitSelectionMode
                                        )
                                    }
                                }
                            ) {
                                Icon(painterResource(R.drawable.more_vert), contentDescription = null)
                            }
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = if (showTopBarTitle) MaterialTheme.colorScheme.background.copy(alpha = 0.9f) else Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                    )
                )

                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier
                        .windowInsetsPadding(LocalPlayerAwareWindowInsets.current)
                        .align(Alignment.BottomCenter)
                )
            }
        }
    }
}

@Composable
private fun AlbumHeaderUI20(
    albumWithSongs: AlbumWithSongs,
    navController: NavController,
    downloadState: Int,
    onDownloadClick: () -> Unit
) {
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current!!
    val database = LocalDatabase.current

    Box(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AsyncImage(
                model = albumWithSongs.album.thumbnailUrl,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .aspectRatio(1f) // Ensure it's square
                    .clip(RoundedCornerShape(32.dp))
                    .graphicsLayer {
                        shadowElevation = 24.dp.toPx()
                    },
                contentScale = ContentScale.Crop
            )

            Spacer(Modifier.height(24.dp))

            Text(
                text = albumWithSongs.album.title,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(buildAnnotatedString {
                withStyle(
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    ).toSpanStyle()
                ) {
                    albumWithSongs.artists.fastForEachIndexed { index, artist ->
                        val link = LinkAnnotation.Clickable(artist.id) {
                            navController.navigate("artist/${artist.id}")
                        }
                        withLink(link) {
                            append(artist.name)
                        }
                        if (index != albumWithSongs.artists.lastIndex) {
                            append(", ")
                        }
                    }
                    albumWithSongs.album.year?.let {
                        append(" • $it")
                    }
                }
            })

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    playerConnection.playQueue(LocalAlbumRadio(albumWithSongs.copy(songs = albumWithSongs.songs.shuffled())))
                }) {
                    Icon(painter = painterResource(R.drawable.shuffle), contentDescription = stringResource(R.string.shuffle))
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = {
                    database.query { update(albumWithSongs.album.toggleLike()) }
                }) {
                    Icon(
                        painter = painterResource(if (albumWithSongs.album.bookmarkedAt != null) R.drawable.favorite else R.drawable.favorite_border),
                        contentDescription = stringResource(R.string.favorite)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                when (downloadState) {
                    Download.STATE_COMPLETED -> {
                        IconButton(onClick = onDownloadClick) {
                            Icon(painter = painterResource(R.drawable.offline), contentDescription = stringResource(R.string.remove_download))
                        }
                    }
                    Download.STATE_DOWNLOADING -> {
                        IconButton(onClick = onDownloadClick) {
                            CircularProgressIndicator(strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                        }
                    }
                    else -> {
                        IconButton(onClick = onDownloadClick) {
                            Icon(painter = painterResource(R.drawable.download), contentDescription = stringResource(R.string.download))
                        }
                    }
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = {
                    menuState.show {
                        AlbumMenu(
                            originalAlbum = Album(albumWithSongs.album, albumWithSongs.artists),
                            navController = navController,
                            onDismiss = menuState::dismiss
                        )
                    }
                }) {
                    Icon(painter = painterResource(R.drawable.more_vert), contentDescription = stringResource(R.string.menu))
                }
            }
        }

        FloatingActionButton(
            onClick = { playerConnection.playQueue(LocalAlbumRadio(albumWithSongs)) },
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp)
        ) {
            Icon(painter = painterResource(R.drawable.play), contentDescription = stringResource(R.string.play), modifier = Modifier.size(32.dp))
        }
    }
}
