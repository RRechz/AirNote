package com.babelsoftware.loudly.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEachReversed
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.babelsoftware.innertube.utils.parseCookieString
import com.babelsoftware.loudly.LocalDatabase
import com.babelsoftware.loudly.LocalPlayerConnection
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.HistorySource
import com.babelsoftware.loudly.constants.InnerTubeCookieKey
import com.babelsoftware.loudly.constants.YtmSyncKey
import com.babelsoftware.loudly.db.entities.EventWithSong
import com.babelsoftware.loudly.extensions.toMediaItem
import com.babelsoftware.loudly.extensions.togglePlayPause
import com.babelsoftware.loudly.models.toMediaMetadata
import com.babelsoftware.loudly.playback.queues.ListQueue
import com.babelsoftware.loudly.playback.queues.YouTubeQueue
import com.babelsoftware.loudly.ui.component.ChipsRow
import com.babelsoftware.loudly.ui.component.EmptyPlaceholder
import com.babelsoftware.loudly.ui.component.HideOnScrollFAB
import com.babelsoftware.loudly.ui.component.IconButton
import com.babelsoftware.loudly.ui.component.LocalMenuState
import com.babelsoftware.loudly.ui.component.NavigationTitle
import com.babelsoftware.loudly.ui.component.SongListItem
import com.babelsoftware.loudly.ui.component.YouTubeListItem
import com.babelsoftware.loudly.ui.menu.SongMenu
import com.babelsoftware.loudly.ui.menu.SongSelectionMenu
import com.babelsoftware.loudly.ui.menu.YouTubeSongMenu
import com.babelsoftware.loudly.ui.theme.AppSpacing
import com.babelsoftware.loudly.ui.theme.FrostedCard
import com.babelsoftware.loudly.ui.utils.backToMain
import com.babelsoftware.loudly.utils.isInternetAvailable
import com.babelsoftware.loudly.utils.rememberPreference
import com.babelsoftware.loudly.viewmodels.DateAgo
import com.babelsoftware.loudly.viewmodels.HistoryViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    navController: NavController,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val database = LocalDatabase.current
    val haptic = LocalHapticFeedback.current
    val menuState = LocalMenuState.current
    val playerConnection = LocalPlayerConnection.current ?: return

    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val historyPage by viewModel.historyPage
    val historySource by viewModel.historySource.collectAsState()

    val (ytmSync) = rememberPreference(YtmSyncKey, true)
    val innerTubeCookie by rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) { "SAPISID" in parseCookieString(innerTubeCookie) }

    var isSearching by rememberSaveable { mutableStateOf(false) }
    var query by rememberSaveable(stateSaver = TextFieldValue.Saver) { mutableStateOf(TextFieldValue()) }
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(isSearching) { if (isSearching) focusRequester.requestFocus() }
    if (isSearching) BackHandler { isSearching = false; query = TextFieldValue() }

    var inSelectMode by rememberSaveable { mutableStateOf(false) }
    val selection = rememberSaveable(
        saver = listSaver<MutableList<Long>, Long>(
            save = { it.toList() },
            restore = { it.toMutableStateList() })
    ) { mutableStateListOf() }
    val onExitSelectionMode = { inSelectMode = false; selection.clear() }
    if (inSelectMode) BackHandler(onBack = onExitSelectionMode)

    val eventsMap by viewModel.events.collectAsState()
    val filteredEventsMap = remember(eventsMap, query) {
        if (query.text.isEmpty()) eventsMap
        else eventsMap
            .mapValues { (_, songs) ->
                songs.filter { song ->
                    song.song.title.contains(query.text, ignoreCase = true) ||
                            song.song.artists.fastAny {
                                it.name.contains(
                                    query.text,
                                    ignoreCase = true
                                )
                            }
                }
            }
            .filterValues { it.isNotEmpty() }
    }
    val filteredEventIndex: Map<Long, EventWithSong> by remember(filteredEventsMap) {
        derivedStateOf {
            filteredEventsMap.flatMap { it.value }.associateBy { it.event.id }
        }
    }

    val filteredRemoteContent = remember(historyPage, query) {
        if (query.text.isEmpty()) {
            historyPage?.sections
        } else {
            historyPage?.sections?.map { section ->
                section.copy(
                    songs = section.songs.filter { song ->
                        song.title.contains(query.text, ignoreCase = true) ||
                                song.artists.any { it.name.contains(query.text, ignoreCase = true) }
                    }
                )
            }?.filter { it.songs.isNotEmpty() }
        }
    }

    LaunchedEffect(filteredEventsMap) {
        selection.fastForEachReversed { eventId ->
            if (filteredEventIndex[eventId] == null) {
                selection.remove(eventId)
            }
        }
    }

    val lazyListState = rememberLazyListState()

    Box(Modifier.fillMaxSize()) {
        AnimatedGradientBackground()
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        if (inSelectMode) {
                            Text(
                                pluralStringResource(
                                    R.plurals.n_selected,
                                    selection.size,
                                    selection.size
                                )
                            )
                        } else if (isSearching) {
                            TextField(
                                value = query,
                                onValueChange = { query = it },
                                placeholder = {
                                    Text(
                                        text = stringResource(R.string.search),
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                },
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleLarge,
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    disabledIndicatorColor = Color.Transparent,
                                ),
                                modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
                                trailingIcon = {
                                    if (query.text.isNotEmpty()) {
                                        IconButton(onClick = { query = TextFieldValue("") }) {
                                            Icon(
                                                painter = painterResource(R.drawable.close),
                                                contentDescription = null
                                            )
                                        }
                                    }
                                }
                            )
                        } else {
                            Text(stringResource(R.string.history))
                        }
                    },
                    navigationIcon = {
                        if (inSelectMode) {
                            IconButton(onClick = onExitSelectionMode) {
                                Icon(
                                    painter = painterResource(R.drawable.close),
                                    contentDescription = null
                                )
                            }
                        } else {
                            IconButton(
                                onClick = {
                                    if (isSearching) {
                                        isSearching = false; query = TextFieldValue()
                                    } else {
                                        navController.navigateUp()
                                    }
                                },
                                onLongClick = {
                                    if (!isSearching) {
                                        navController.backToMain()
                                    }
                                }
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
                                checked = selection.size == filteredEventIndex.size && selection.isNotEmpty(),
                                onCheckedChange = {
                                    if (selection.size == filteredEventIndex.size) {
                                        selection.clear()
                                    } else {
                                        selection.clear()
                                        selection.addAll(filteredEventsMap.flatMap { group -> group.value.map { it.event.id } })
                                    }
                                }
                            )
                            IconButton(
                                enabled = selection.isNotEmpty(),
                                onClick = {
                                    menuState.show {
                                        SongSelectionMenu(
                                            navController = navController,
                                            selection = selection.mapNotNull { eventId -> filteredEventIndex[eventId]?.song },
                                            onDismiss = menuState::dismiss,
                                            onRemoveFromHistory = {
                                                val sel =
                                                    selection.mapNotNull { eventId -> filteredEventIndex[eventId]?.event }
                                                database.query { sel.forEach { delete(it) } }
                                            },
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
                        } else if (!isSearching) {
                            IconButton(onClick = { isSearching = true }) {
                                Icon(painterResource(R.drawable.search), contentDescription = null)
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        scrolledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.9f)
                    )
                )
            },
            floatingActionButton = {
                if (!inSelectMode) {
                    val fabVisible =
                        if (historySource == HistorySource.LOCAL) filteredEventsMap.isNotEmpty() else filteredRemoteContent?.isNotEmpty() == true
                    HideOnScrollFAB(
                        visible = fabVisible,
                        lazyListState = lazyListState,
                        icon = R.drawable.shuffle,
                        onClick = {
                            val items = if (historySource == HistorySource.LOCAL) {
                                filteredEventIndex.values.map { it.song.toMediaItem() }.shuffled()
                            } else {
                                filteredRemoteContent?.flatMap { it.songs }
                                    ?.map { it.toMediaItem() }?.shuffled() ?: emptyList()
                            }
                            playerConnection.playQueue(
                                ListQueue(
                                    title = context.getString(if (historySource == HistorySource.LOCAL) R.string.history_queue_title_local else R.string.history_queue_title_online),
                                    items = items
                                )
                            )
                        }
                    )
                }
            },
            containerColor = Color.Transparent
        ) { innerPadding ->
            LazyColumn(
                state = lazyListState,
                contentPadding = PaddingValues(
                    start = AppSpacing.Default,
                    end = AppSpacing.Default,
                    top = innerPadding.calculateTopPadding() + AppSpacing.Small,
                    bottom = innerPadding.calculateBottomPadding() + AppSpacing.Default
                ),
                verticalArrangement = Arrangement.spacedBy(AppSpacing.Default),
                modifier = Modifier.fillMaxSize()
            ) {
                // Filtre çipleri en başta kalıyor
                item {
                    if (ytmSync && isLoggedIn && isInternetAvailable(context)) {
                        ChipsRow(
                            chips = listOf(
                                HistorySource.LOCAL to stringResource(R.string.local_history),
                                HistorySource.REMOTE to stringResource(R.string.remote_history),
                            ),
                            currentValue = historySource,
                            onValueUpdate = { viewModel.historySource.value = it }
                        )
                    }
                }

                val isLocalEmpty =
                    historySource == HistorySource.LOCAL && filteredEventsMap.isEmpty()
                val isRemoteEmpty =
                    historySource == HistorySource.REMOTE && filteredRemoteContent.isNullOrEmpty()

                if (isLocalEmpty || isRemoteEmpty) {
                    item {
                        val textRes =
                            if (isSearching) R.string.no_results_found else R.string.history_empty
                        EmptyPlaceholder(
                            icon = R.drawable.history,
                            text = stringResource(textRes),
                            modifier = Modifier.fillParentMaxSize().animateItem()
                        )
                    }
                } else {
                    // Liste boş değilse, kaynağa göre kartları çiziyoruz.
                    if (historySource == HistorySource.LOCAL) {
                        filteredEventsMap.forEach { (dateAgo, events) ->
                            item(key = "local-${dateAgo}") { // Key'e "local-" ön eki ekleyerek daha güvenli hale getirdik.
                                FrostedCard(modifier = Modifier.animateItem()) {
                                    Column {
                                        NavigationTitle(
                                            title = when (dateAgo) {
                                                DateAgo.Today -> stringResource(R.string.today)
                                                DateAgo.Yesterday -> stringResource(R.string.yesterday)
                                                DateAgo.ThisWeek -> stringResource(R.string.this_week)
                                                DateAgo.LastWeek -> stringResource(R.string.last_week)
                                                is DateAgo.Other -> dateAgo.date.format(
                                                    DateTimeFormatter.ofPattern("yyyy/MM")
                                                )
                                            },
                                            modifier = Modifier.padding(16.dp)
                                        )
                                        events.forEach { event ->
                                            val onCheckedChange: (Boolean) -> Unit = {
                                                if (it) selection.add(event.event.id)
                                                else selection.remove(event.event.id)
                                            }
                                            SongListItem(
                                                song = event.song,
                                                isActive = event.song.id == mediaMetadata?.id,
                                                isPlaying = isPlaying,
                                                showInLibraryIcon = true,
                                                trailingContent = {
                                                    if (inSelectMode) {
                                                        Checkbox(
                                                            checked = event.event.id in selection,
                                                            onCheckedChange = onCheckedChange
                                                        )
                                                    } else {
                                                        IconButton(onClick = {
                                                            menuState.show {
                                                                SongMenu(
                                                                    originalSong = event.song,
                                                                    event = event.event,
                                                                    navController = navController,
                                                                    onDismiss = menuState::dismiss
                                                                )
                                                            }
                                                        }) {
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
                                                                onCheckedChange(event.event.id !in selection)
                                                            } else if (event.song.id == mediaMetadata?.id) {
                                                                playerConnection.player.togglePlayPause()
                                                            } else {
                                                                playerConnection.playQueue(
                                                                    YouTubeQueue.Companion.radio(
                                                                        event.song.toMediaMetadata()
                                                                    )
                                                                )
                                                            }
                                                        },
                                                        onLongClick = {
                                                            if (!inSelectMode) {
                                                                haptic.performHapticFeedback(
                                                                    HapticFeedbackType.LongPress
                                                                )
                                                                inSelectMode = true
                                                                onCheckedChange(true)
                                                            }
                                                        }
                                                    ).animateItem()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    } else { // historySource == HistorySource.REMOTE
                        filteredRemoteContent?.forEach { section ->
                            item(key = "remote-${section.title}") { // Key'e "remote-" ön eki eklendi.
                                FrostedCard(modifier = Modifier.animateItem()) {
                                    Column {
                                        NavigationTitle(
                                            title = section.title,
                                            modifier = Modifier.padding(16.dp)
                                        )
                                        section.songs.forEach { song ->
                                            YouTubeListItem(
                                                item = song,
                                                isActive = song.id == mediaMetadata?.id,
                                                isPlaying = isPlaying,
                                                trailingContent = {
                                                    IconButton(onClick = {
                                                        menuState.show {
                                                            YouTubeSongMenu(
                                                                song = song,
                                                                navController = navController,
                                                                onDismiss = menuState::dismiss
                                                            )
                                                        }
                                                    }) {
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
                                                            if (song.id == mediaMetadata?.id) {
                                                                playerConnection.player.togglePlayPause()
                                                            } else {
                                                                playerConnection.playQueue(
                                                                    YouTubeQueue.Companion.radio(
                                                                        song.toMediaMetadata()
                                                                    )
                                                                )
                                                            }
                                                        },
                                                        onLongClick = {
                                                            haptic.performHapticFeedback(
                                                                HapticFeedbackType.LongPress
                                                            )
                                                            menuState.show {
                                                                YouTubeSongMenu(
                                                                    song = song,
                                                                    navController = navController,
                                                                    onDismiss = menuState::dismiss,
                                                                )
                                                            }
                                                        }
                                                    ).animateItem()
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