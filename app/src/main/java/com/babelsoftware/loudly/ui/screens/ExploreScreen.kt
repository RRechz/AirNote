package com.babelsoftware.loudly.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.babelsoftware.loudly.LocalPlayerAwareWindowInsets
import com.babelsoftware.loudly.LocalPlayerConnection
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.ui.component.LocalMenuState
import com.babelsoftware.loudly.ui.component.NavigationTitle
import com.babelsoftware.loudly.ui.component.YouTubeGridItem
import com.babelsoftware.loudly.ui.component.shimmer.GridItemPlaceHolder
import com.babelsoftware.loudly.ui.component.shimmer.ShimmerHost
import com.babelsoftware.loudly.ui.component.shimmer.TextPlaceholder
import com.babelsoftware.loudly.ui.menu.YouTubeAlbumMenu
import com.babelsoftware.loudly.ui.theme.AppSpacing
import com.babelsoftware.loudly.ui.theme.FrostedCard
import com.babelsoftware.loudly.viewmodels.ExploreViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExploreScreen(
    navController: NavController,
    viewModel: ExploreViewModel = hiltViewModel(),
) {
    val menuState = LocalMenuState.current
    val haptic = LocalHapticFeedback.current
    val playerConnection = LocalPlayerConnection.current ?: return
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()

    val explorePage by viewModel.explorePage.collectAsState()

    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier.fillMaxSize(),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateTopPadding(),
                bottom = LocalPlayerAwareWindowInsets.current.asPaddingValues().calculateBottomPadding()
            ),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.Default)
        ) {
            explorePage?.newReleaseAlbums?.let { newReleaseAlbums ->
                item {
                    FrostedCard(
                        modifier = Modifier.padding(horizontal = AppSpacing.Default)
                    ) {
                        Column {
                            NavigationTitle(
                                title = stringResource(R.string.new_release_albums),
                                onClick = {
                                    navController.navigate("new_release")
                                },
                                modifier = Modifier.padding(
                                    top = AppSpacing.Default,
                                    start = AppSpacing.Default,
                                    end = AppSpacing.Default
                                )
                            )
                            LazyRow(
                                contentPadding = PaddingValues(
                                    horizontal = AppSpacing.Default,
                                    vertical = AppSpacing.Default
                                ),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.Default)
                            ) {
                                items(
                                    items = newReleaseAlbums,
                                    key = { it.id },
                                ) { album ->
                                    YouTubeGridItem(
                                        item = album,
                                        isActive = mediaMetadata?.album?.id == album.id,
                                        isPlaying = isPlaying,
                                        coroutineScope = coroutineScope,
                                        modifier =
                                            Modifier
                                                .combinedClickable(
                                                    onClick = {
                                                        navController.navigate("album/${album.id}")
                                                    },
                                                    onLongClick = {
                                                        haptic.performHapticFeedback(
                                                            HapticFeedbackType.LongPress
                                                        )
                                                        menuState.show {
                                                            YouTubeAlbumMenu(
                                                                albumItem = album,
                                                                navController = navController,
                                                                onDismiss = menuState::dismiss,
                                                            )
                                                        }
                                                    },
                                                )
                                                .animateItem()
                                    )
                                }
                            }
                        }
                    }
                }
            }
            explorePage?.moodAndGenres?.let { moodAndGenres ->
                item {
                    FrostedCard(
                        modifier = Modifier.padding(horizontal = AppSpacing.Default)
                    ) {
                        Column {
                            NavigationTitle(
                                title = stringResource(R.string.mood_and_genres),
                                onClick = {
                                    navController.navigate("mood_and_genres")
                                },
                                modifier = Modifier.padding(
                                    top = AppSpacing.Default,
                                    start = AppSpacing.Default,
                                    end = AppSpacing.Default
                                )
                            )
                            LazyHorizontalGrid(
                                rows = GridCells.Fixed(2),
                                contentPadding = PaddingValues(AppSpacing.Default),
                                horizontalArrangement = Arrangement.spacedBy(AppSpacing.Small),
                                verticalArrangement = Arrangement.spacedBy(AppSpacing.Small),
                                modifier = Modifier.height(140.dp),
                            ) {
                                items(moodAndGenres) {
                                    MoodAndGenresButton(
                                        title = it.title,
                                        onClick = {
                                            navController.navigate("youtube_browse/${it.endpoint.browseId}?params=${it.endpoint.params}")
                                        },
                                        modifier =
                                            Modifier
                                                .padding(6.dp)
                                                .width(180.dp)
                                    )
                                }
                            }
                            Spacer(
                                Modifier.height(
                                    LocalPlayerAwareWindowInsets.current.asPaddingValues()
                                        .calculateBottomPadding()
                                )
                            )
                        }
                    }
                    if (explorePage == null) {
                        ShimmerHost {
                            TextPlaceholder(
                                height = 36.dp,
                                modifier =
                                    Modifier
                                        .padding(vertical = 12.dp, horizontal = 12.dp)
                                        .width(250.dp)
                            )
                            Row {
                                repeat(2) {
                                    GridItemPlaceHolder()
                                }
                            }
                            TextPlaceholder(
                                height = 36.dp,
                                modifier =
                                    Modifier
                                        .padding(vertical = 12.dp, horizontal = 12.dp)
                                        .width(250.dp)
                            )
                            repeat(4) {
                                Row {
                                    repeat(2) {
                                        TextPlaceholder(
                                            height = MoodAndGenresButtonHeight,
                                            modifier =
                                                Modifier
                                                    .padding(horizontal = 6.dp)
                                                    .width(200.dp)
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