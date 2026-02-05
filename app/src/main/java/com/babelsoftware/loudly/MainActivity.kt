package com.babelsoftware.loudly

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.net.toUri
import androidx.core.util.Consumer
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.imageLoader
import coil.request.ImageRequest
import com.babelsoftware.innertube.YouTube
import com.babelsoftware.innertube.models.SongItem
import com.babelsoftware.innertube.models.WatchEndpoint
import com.babelsoftware.loudly.background.ContextualWorker
import com.babelsoftware.loudly.constants.AppBarHeight
import com.babelsoftware.loudly.constants.AppDesignVariantKey
import com.babelsoftware.loudly.constants.AppDesignVariantType
import com.babelsoftware.loudly.constants.ChipSortTypeKey
import com.babelsoftware.loudly.constants.DarkModeKey
import com.babelsoftware.loudly.constants.DefaultOpenTabKey
import com.babelsoftware.loudly.constants.DefaultOpenTabOldKey
import com.babelsoftware.loudly.constants.DisableScreenshotKey
import com.babelsoftware.loudly.constants.DynamicThemeKey
import com.babelsoftware.loudly.constants.FirstSetupPassed
import com.babelsoftware.loudly.constants.LibraryFilter
import com.babelsoftware.loudly.constants.MiniPlayerHeight
import com.babelsoftware.loudly.constants.PauseSearchHistoryKey
import com.babelsoftware.loudly.constants.PlayerStyle
import com.babelsoftware.loudly.constants.PlayerStyleKey
import com.babelsoftware.loudly.constants.PureBlackKey
import com.babelsoftware.loudly.constants.SearchSource
import com.babelsoftware.loudly.constants.SearchSourceKey
import com.babelsoftware.loudly.constants.SongSortType
import com.babelsoftware.loudly.constants.StopMusicOnTaskClearKey
import com.babelsoftware.loudly.db.MusicDatabase
import com.babelsoftware.loudly.db.entities.SearchHistory
import com.babelsoftware.loudly.extensions.toEnum
import com.babelsoftware.loudly.extensions.toMediaItem
import com.babelsoftware.loudly.models.toMediaMetadata
import com.babelsoftware.loudly.playback.DownloadUtil
import com.babelsoftware.loudly.playback.MusicService
import com.babelsoftware.loudly.playback.MusicService.MusicBinder
import com.babelsoftware.loudly.playback.PlayerConnection
import com.babelsoftware.loudly.playback.queues.ListQueue
import com.babelsoftware.loudly.playback.queues.YouTubeQueue
import com.babelsoftware.loudly.ui.component.ActionChipButton
import com.babelsoftware.loudly.ui.component.BottomSheetMenu
import com.babelsoftware.loudly.ui.component.IconButton
import com.babelsoftware.loudly.ui.component.LocalMenuState
import com.babelsoftware.loudly.ui.component.LoudlyBottomNavBar
import com.babelsoftware.loudly.ui.component.TopSearch
import com.babelsoftware.loudly.ui.component.rememberBottomSheetState
import com.babelsoftware.loudly.ui.component.shimmer.ShimmerTheme
import com.babelsoftware.loudly.ui.menu.YouTubeSongMenu
import com.babelsoftware.loudly.ui.player.BottomSheetPlayer
import com.babelsoftware.loudly.ui.screens.Screens
import com.babelsoftware.loudly.ui.screens.navigationBuilder
import com.babelsoftware.loudly.ui.screens.search.LocalSearchScreen
import com.babelsoftware.loudly.ui.screens.search.OnlineSearchScreen
import com.babelsoftware.loudly.ui.screens.settings.DarkMode
import com.babelsoftware.loudly.ui.screens.settings.NavigationTab
import com.babelsoftware.loudly.ui.screens.settings.NavigationTabOld
import com.babelsoftware.loudly.ui.screens.settings.updateLanguage
import com.babelsoftware.loudly.ui.theme.ColorSaver
import com.babelsoftware.loudly.ui.theme.DefaultThemeColor
import com.babelsoftware.loudly.ui.theme.LoudlyTheme
import com.babelsoftware.loudly.ui.theme.extractThemeColor
import com.babelsoftware.loudly.ui.utils.appBarScrollBehavior
import com.babelsoftware.loudly.ui.utils.backToMain
import com.babelsoftware.loudly.ui.utils.imageCache
import com.babelsoftware.loudly.ui.utils.resetHeightOffset
import com.babelsoftware.loudly.utils.SyncUtils
import com.babelsoftware.loudly.utils.Updater
import com.babelsoftware.loudly.utils.dataStore
import com.babelsoftware.loudly.utils.get
import com.babelsoftware.loudly.utils.rememberEnumPreference
import com.babelsoftware.loudly.utils.rememberPreference
import com.babelsoftware.loudly.utils.urlEncode
import com.google.android.datatransport.BuildConfig
import com.valentinilk.shimmer.LocalShimmerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.net.URLDecoder
import java.util.Locale
import javax.inject.Inject
import kotlin.time.Duration.Companion.days

@Suppress("NAME_SHADOWING")
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var database: MusicDatabase

    @Inject
    lateinit var downloadUtil: DownloadUtil

    @Inject
    lateinit var syncUtils: SyncUtils

    private var playerConnection by mutableStateOf<PlayerConnection?>(null)
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            if (service is MusicBinder) {
                playerConnection = PlayerConnection(this@MainActivity, service, database, lifecycleScope)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            playerConnection?.dispose()
            playerConnection = null
        }
    }


    // storage permission helpers
    private val mediaPermissionLevel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) Manifest.permission.READ_MEDIA_AUDIO
    else Manifest.permission.READ_EXTERNAL_STORAGE

    private val nearbyDevicesPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE,
            Manifest.permission.BLUETOOTH_CONNECT
        )
    } else {
        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION)
    }
    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {}

    private var latestVersionName by mutableStateOf(BuildConfig.VERSION_NAME)

    override fun onStart() {
        super.onStart()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startService(Intent(this, MusicService::class.java))
        } else {
            startService(Intent(this, MusicService::class.java))
        }
        bindService(Intent(this, MusicService::class.java), serviceConnection, Context.BIND_AUTO_CREATE)
    }

    override fun onStop() {
        unbindService(serviceConnection)
        super.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (dataStore.get(StopMusicOnTaskClearKey, false) && playerConnection?.isPlaying?.value == true && isFinishing) {
            stopService(Intent(this, MusicService::class.java))
            unbindService(serviceConnection)
            playerConnection = null
        }
    }

    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "ObsoleteSdkInt", "UnusedBoxWithConstraintsScope")
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        handleIncomingIntent(intent)

        lifecycleScope.launch {
            dataStore.data
                .map { it[DisableScreenshotKey] ?: false }
                .distinctUntilChanged()
                .collectLatest {
                    if (it) {
                        window.setFlags(
                            WindowManager.LayoutParams.FLAG_SECURE,
                            WindowManager.LayoutParams.FLAG_SECURE
                        )
                    } else {
                        window.clearFlags(WindowManager.LayoutParams.FLAG_SECURE)
                    }
                }
        }

        val sharedPreferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val savedLanguage = sharedPreferences.getString("app_language", Locale.getDefault().language) ?: "en"
        updateLanguage(this, savedLanguage)

        setContent {
            LaunchedEffect(Unit) {
                if (System.currentTimeMillis() - Updater.lastCheckTime > 1.days.inWholeMilliseconds) {
                    Updater.getLatestVersionName().onSuccess { latestVersionName = it }
                }
            }


            val snackbarHostState = remember { SnackbarHostState() }
            val (appDesignVariant) = rememberEnumPreference(AppDesignVariantKey, defaultValue = AppDesignVariantType.NEW)
            val (defaultOpenTabOld) = rememberEnumPreference(DefaultOpenTabOldKey, defaultValue = NavigationTabOld.HOME)
            val navController = rememberNavController()
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val inSelectMode = navBackStackEntry?.savedStateHandle?.getStateFlow("inSelectMode", false)?.collectAsState()
            val enableDynamicTheme by rememberPreference(DynamicThemeKey, defaultValue = true)
            val darkTheme by rememberEnumPreference(DarkModeKey, defaultValue = DarkMode.AUTO)
            val pureBlack by rememberPreference(PureBlackKey, defaultValue = false)
            val isSystemInDarkTheme = isSystemInDarkTheme()
            val useDarkTheme = remember(darkTheme, isSystemInDarkTheme) {
                if (darkTheme == DarkMode.AUTO) isSystemInDarkTheme else darkTheme == DarkMode.ON
            }
            LaunchedEffect(useDarkTheme) {
                setSystemBarAppearance(useDarkTheme)
            }
            var themeColor by rememberSaveable(stateSaver = ColorSaver) {
                mutableStateOf(DefaultThemeColor)
            }

            LaunchedEffect(playerConnection, enableDynamicTheme, isSystemInDarkTheme) {
                val playerConnection = playerConnection
                if (!enableDynamicTheme || playerConnection == null) {
                    themeColor = DefaultThemeColor
                    return@LaunchedEffect
                }
                playerConnection.service.currentMediaMetadata.collectLatest { song ->
                    themeColor = if (song != null) {
                        withContext(Dispatchers.IO) {
                            if (!song.isLocal) {
                                val result = imageLoader.execute(
                                    ImageRequest.Builder(this@MainActivity)
                                        .data(song.thumbnailUrl)
                                        .allowHardware(false)
                                        .build()
                                )
                                (result.drawable as? BitmapDrawable)?.bitmap?.extractThemeColor() ?: DefaultThemeColor
                            } else {
                                imageCache.getLocalThumbnail(song.localPath)?.extractThemeColor()
                                    ?: DefaultThemeColor
                            }
                        }
                    } else DefaultThemeColor
                }
            }
            if (checkSelfPermission(mediaPermissionLevel) == PackageManager.PERMISSION_DENIED) {
                permissionsLauncher.launch(arrayOf(mediaPermissionLevel))
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (nearbyDevicesPermission.any {
                        checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
                    }) {
                    permissionsLauncher.launch(nearbyDevicesPermission)
                }
            } else {
                if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                    permissionsLauncher.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION))
                }
            }

            LoudlyTheme(
                darkTheme = useDarkTheme,
                pureBlack = pureBlack,
                themeColor = themeColor
            ) {
                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    val focusManager = LocalFocusManager.current
                    val density = LocalDensity.current
                    val windowsInsets = WindowInsets.systemBars
                    val bottomInset = with(density) { windowsInsets.getBottom(density).toDp() }

                    val navController = rememberNavController()
                    val navBackStackEntry by navController.currentBackStackEntryAsState()

                    val navigationItems = remember(appDesignVariant) {
                        if (appDesignVariant == AppDesignVariantType.NEW) {
                            Screens.MainScreens
                        } else {
                            Screens.MainScreensOld
                        }
                    }
                    val defaultOpenTab = remember {
                        if (appDesignVariant == AppDesignVariantType.NEW) dataStore[DefaultOpenTabKey].toEnum(defaultValue = NavigationTab.HOME)
                        else dataStore[DefaultOpenTabKey].toEnum(defaultValue = NavigationTab.HOME)
                    }
                    val tabOpenedFromShortcut = remember {
                        when (intent?.action) {
                            ACTION_SONGS -> if (appDesignVariant == AppDesignVariantType.NEW) NavigationTab.LIBRARY else NavigationTabOld.SONGS
                            ACTION_ARTISTS -> if (appDesignVariant == AppDesignVariantType.NEW) NavigationTab.LIBRARY else NavigationTabOld.ARTISTS
                            ACTION_ALBUMS -> if (appDesignVariant == AppDesignVariantType.NEW) NavigationTab.LIBRARY else NavigationTabOld.ALBUMS
                            ACTION_PLAYLISTS -> if (appDesignVariant == AppDesignVariantType.NEW) NavigationTab.LIBRARY else NavigationTabOld.PLAYLISTS
                            else -> null
                        }
                    }
                    if (tabOpenedFromShortcut != null && appDesignVariant == AppDesignVariantType.NEW) {
                        var filter by rememberEnumPreference(ChipSortTypeKey, LibraryFilter.LIBRARY)
                        filter = when (intent?.action) {
                            ACTION_SONGS -> LibraryFilter.SONGS
                            ACTION_ARTISTS -> LibraryFilter.ARTISTS
                            ACTION_ALBUMS -> LibraryFilter.ALBUMS
                            ACTION_PLAYLISTS -> LibraryFilter.PLAYLISTS
                            else -> LibraryFilter.LIBRARY
                        }
                    }
                    val topLevelScreens = listOf(
                        Screens.Home.route,
                        Screens.Explore.route,
                        Screens.Library.route,
                        Screens.Songs.route,
                        Screens.Artists.route,
                        Screens.Albums.route,
                        Screens.Playlists.route,
                        "settings"
                    )

                    val (query, onQueryChange) = rememberSaveable(stateSaver = TextFieldValue.Saver) {
                        mutableStateOf(TextFieldValue())
                    }
                    var active by rememberSaveable {
                        mutableStateOf(false)
                    }
                    val onActiveChange: (Boolean) -> Unit = { newActive ->
                        active = newActive
                        if (!newActive) {
                            focusManager.clearFocus()
                            if (navigationItems.any { it.route == navBackStackEntry?.destination?.route }) {
                                onQueryChange(TextFieldValue())
                            }
                        }
                    }
                    var searchSource by rememberEnumPreference(SearchSourceKey, SearchSource.ONLINE)

                    val searchBarFocusRequester = remember { FocusRequester() }

                    val onSearch: (String) -> Unit = {
                        if (it.isNotEmpty()) {
                            onActiveChange(false)
                            navController.navigate("search/${it.urlEncode()}")
                            if (dataStore[PauseSearchHistoryKey] != true) {
                                database.query {
                                    insert(SearchHistory(query = it))
                                }
                            }
                        }
                    }

                    var openSearchImmediately: Boolean by remember {
                        mutableStateOf(intent?.action == ACTION_SEARCH)
                    }

                    val (playerStyle) = rememberEnumPreference(PlayerStyleKey, defaultValue = PlayerStyle.UI_2_0)

                    val shouldShowSearchBar = remember(active, navBackStackEntry, inSelectMode?.value) {
                        (active ||
                                navigationItems.any { it.route == navBackStackEntry?.destination?.route } ||
                                navBackStackEntry?.destination?.route?.startsWith("search/") == true) &&
                                inSelectMode?.value != true
                    }
                    val shouldShowNavigationBar = remember(navBackStackEntry, active) {
                        navBackStackEntry?.destination?.route == null ||
                                navigationItems.any { it.route == navBackStackEntry?.destination?.route } && !active
                    }
                    // ==================== KEYS & ADDICTIONS ====================
                    val miniPlayerMargin = if (playerStyle == PlayerStyle.UI_2_0) 8.dp else 0.dp
                    val collapsedBound = bottomInset + (if (shouldShowNavigationBar) 80.dp else 0.dp) + MiniPlayerHeight + miniPlayerMargin

                    val maxHeightInDp = with(density) { constraints.maxHeight.toDp() }
                    val playerBottomSheetState = rememberBottomSheetState(
                        dismissedBound = 0.dp,
                        collapsedBound = collapsedBound,
                        expandedBound = maxHeightInDp,
                    )

                    LaunchedEffect(shouldShowNavigationBar, playerBottomSheetState.isDismissed) {
                        if (!playerBottomSheetState.isDismissed) {
                            playerBottomSheetState.collapseSoft()
                        }
                    }

                    val currentSong by playerConnection?.service?.currentMediaMetadata?.collectAsState(initial = null)
                        ?: remember { mutableStateOf(null) }

                    LaunchedEffect(currentSong) {
                        if (currentSong == null) {
                            if (!playerBottomSheetState.isDismissed) {
                                playerBottomSheetState.dismiss()
                            }
                        } else {
                            if (playerBottomSheetState.isDismissed) {
                                playerBottomSheetState.collapseSoft()
                            }
                        }
                    }

                    val playerAwareWindowInsets = remember(bottomInset, shouldShowNavigationBar, playerBottomSheetState.isDismissed, miniPlayerMargin) {
                        var bottom = bottomInset
                        if (shouldShowNavigationBar) bottom += 80.dp
                        if (!playerBottomSheetState.isDismissed) {
                            bottom += MiniPlayerHeight
                            bottom += miniPlayerMargin
                        }
                        windowsInsets
                            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top)
                            .add(WindowInsets(top = AppBarHeight, bottom = bottom))
                    }

                    val searchBarScrollBehavior = appBarScrollBehavior(
                        canScroll = {
                            navBackStackEntry?.destination?.route?.startsWith("search/") == false &&
                                    (playerBottomSheetState.isCollapsed || playerBottomSheetState.isDismissed)
                        }
                    )
                    val topAppBarScrollBehavior = appBarScrollBehavior(
                        canScroll = {
                            navBackStackEntry?.destination?.route?.startsWith("search/") == false &&
                                    (playerBottomSheetState.isCollapsed || playerBottomSheetState.isDismissed)
                        }
                    )
                    // ========================================================================
                    val isScrolled by remember {
                        derivedStateOf {
                            searchBarScrollBehavior.state.heightOffset < -5f
                        }
                    }


                    val (firstSetupPassed) = rememberPreference(FirstSetupPassed, defaultValue = false)

                    LaunchedEffect(Unit) {
                        if (!firstSetupPassed) {
                            navController.navigate("setup_wizard")
                        }
                    }

                    LaunchedEffect(navBackStackEntry) {
                        if (navBackStackEntry?.destination?.route?.startsWith("search/") == true) {
                            val searchQuery =
                                withContext(Dispatchers.IO) {
                                    if (navBackStackEntry
                                            ?.arguments
                                            ?.getString(
                                                "query",
                                            )!!
                                            .contains(
                                                "%",
                                            )
                                    ) {
                                        navBackStackEntry?.arguments?.getString(
                                            "query",
                                        )!!
                                    } else {
                                        URLDecoder.decode(navBackStackEntry?.arguments?.getString("query")!!, "UTF-8")
                                    }
                                }
                            onQueryChange(TextFieldValue(searchQuery, TextRange(searchQuery.length)))
                        } else if (navigationItems.any { it.route == navBackStackEntry?.destination?.route }) {
                            onQueryChange(TextFieldValue())
                        }
                        searchBarScrollBehavior.state.resetHeightOffset()
                        topAppBarScrollBehavior.state.resetHeightOffset()
                    }
                    LaunchedEffect(active) {
                        if (active) {
                            searchBarScrollBehavior.state.resetHeightOffset()
                            topAppBarScrollBehavior.state.resetHeightOffset()
                        }
                    }

                    LaunchedEffect(playerConnection) {
                        val player = playerConnection?.player ?: return@LaunchedEffect
                        if (player.currentMediaItem == null) {
                            if (!playerBottomSheetState.isDismissed) {
                                playerBottomSheetState.dismiss()
                            }
                        } else {
                            if (playerBottomSheetState.isDismissed) {
                                playerBottomSheetState.collapseSoft()
                            }
                        }
                    }

                    DisposableEffect(playerConnection, playerBottomSheetState) {
                        val player = playerConnection?.player ?: return@DisposableEffect onDispose { }
                        val listener = object : Player.Listener {
                            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED && mediaItem != null && playerBottomSheetState.isDismissed) {
                                    playerBottomSheetState.collapseSoft()
                                }
                            }
                        }
                        player.addListener(listener)
                        onDispose {
                            player.removeListener(listener)
                        }
                    }

                    val coroutineScope = rememberCoroutineScope()
                    var sharedSong: SongItem? by remember {
                        mutableStateOf(null)
                    }
                    DisposableEffect(Unit) {
                        val listener = Consumer<Intent> { intent ->
                            handleIncomingIntent(intent)
                            val uri = intent.data ?: intent.extras?.getString(Intent.EXTRA_TEXT)?.toUri() ?: return@Consumer
                            when (val path = uri.pathSegments.firstOrNull()) {
                                "playlist" -> uri.getQueryParameter("list")?.let { playlistId ->
                                    if (playlistId.startsWith("OLAK5uy_")) {
                                        coroutineScope.launch {
                                            YouTube.albumSongs(playlistId).onSuccess { songs ->
                                                songs.firstOrNull()?.album?.id?.let { browseId ->
                                                    navController.navigate("album/$browseId")
                                                }
                                            }.onFailure {
                                                reportException(it)
                                            }
                                        }
                                    } else {
                                        navController.navigate("online_playlist/$playlistId")
                                    }
                                }

                                "channel", "c" -> uri.lastPathSegment?.let { artistId ->
                                    navController.navigate("artist/$artistId")
                                }

                                else -> when {
                                    path == "watch" -> uri.getQueryParameter("v")
                                    uri.host == "youtu.be" -> path
                                    else -> null
                                }?.let { videoId ->
                                    coroutineScope.launch {
                                        withContext(Dispatchers.IO) {
                                            YouTube.queue(listOf(videoId))
                                        }.onSuccess {
                                            playerConnection?.playQueue(YouTubeQueue(WatchEndpoint(videoId = it.firstOrNull()?.id), it.firstOrNull()?.toMediaMetadata()))
                                        }.onFailure {
                                            reportException(it)
                                        }
                                    }
                                }
                            }
                        }

                        addOnNewIntentListener(listener)
                        onDispose { removeOnNewIntentListener(listener) }
                    }

                    val updateAvailable = remember(latestVersionName) {
                        isNewerVersion(latestVersionName, BuildConfig.VERSION_NAME)
                    }

                    CompositionLocalProvider(
                        LocalDatabase provides database,
                        LocalContentColor provides contentColorFor(MaterialTheme.colorScheme.surface),
                        LocalPlayerConnection provides playerConnection,
                        LocalPlayerAwareWindowInsets provides playerAwareWindowInsets,
                        LocalDownloadUtil provides downloadUtil,
                        LocalShimmerTheme provides ShimmerTheme,
                        LocalSyncUtils provides syncUtils,
                        LocalSnackbarHostState provides snackbarHostState
                    ) {
                        if (appDesignVariant == AppDesignVariantType.NEW) {
                            NavHost(
                                navController = navController,
                                startDestination = when (tabOpenedFromShortcut ?: defaultOpenTab) {
                                    NavigationTab.HOME -> Screens.Home
                                    NavigationTab.EXPLORE -> Screens.Explore
                                    NavigationTabOld.SONGS -> Screens.Songs
                                    NavigationTabOld.ARTISTS -> Screens.Artists
                                    NavigationTabOld.ALBUMS -> Screens.Albums
                                    NavigationTabOld.PLAYLISTS -> Screens.Playlists
                                    else -> Screens.Library
                                }.route,
                                enterTransition = {
                                    if (initialState.destination.route in topLevelScreens && targetState.destination.route in topLevelScreens) {
                                        fadeIn(tween(250))
                                    } else {
                                        fadeIn(tween(250)) + slideInHorizontally { it / 2 }
                                    }
                                },
                                exitTransition = {
                                    if (initialState.destination.route in topLevelScreens && targetState.destination.route in topLevelScreens) {
                                        fadeOut(tween(200))
                                    } else {
                                        fadeOut(tween(200)) + slideOutHorizontally { -it / 2 }
                                    }
                                },
                                popEnterTransition = {
                                    if ((initialState.destination.route in topLevelScreens || initialState.destination.route?.startsWith(
                                            "search/"
                                        ) == true) && targetState.destination.route in topLevelScreens
                                    ) {
                                        fadeIn(tween(250))
                                    } else {
                                        fadeIn(tween(250)) + slideInHorizontally { -it / 2 }
                                    }
                                },
                                popExitTransition = {
                                    if ((initialState.destination.route in topLevelScreens || initialState.destination.route?.startsWith(
                                            "search/"
                                        ) == true) && targetState.destination.route in topLevelScreens
                                    ) {
                                        fadeOut(tween(200))
                                    } else {
                                        fadeOut(tween(200)) + slideOutHorizontally { it / 2 }
                                    }
                                },
                                modifier = Modifier
                                    .nestedScroll(
                                        if (navigationItems.any { it.route == navBackStackEntry?.destination?.route } ||
                                            navBackStackEntry?.destination?.route?.startsWith("search/") == true) {
                                            searchBarScrollBehavior.nestedScrollConnection
                                        } else {
                                            topAppBarScrollBehavior.nestedScrollConnection
                                        }
                                    )
                            ) {
                                navigationBuilder(
                                    navController,
                                    topAppBarScrollBehavior,
                                    updateAvailable,
                                    playerStyle
                                )
                            }
                        } else {
                            NavHost(
                                navController = navController,
                                startDestination = when (tabOpenedFromShortcut
                                    ?: defaultOpenTabOld) {
                                    NavigationTabOld.HOME -> Screens.Home
                                    NavigationTabOld.EXPLORE -> Screens.Explore
                                    NavigationTabOld.SONGS -> Screens.Songs
                                    NavigationTabOld.ARTISTS -> Screens.Artists
                                    NavigationTabOld.ALBUMS -> Screens.Albums
                                    NavigationTabOld.PLAYLISTS -> Screens.Playlists
                                    else -> Screens.Library
                                }.route,
                                enterTransition = {
                                    if (initialState.destination.route in topLevelScreens && targetState.destination.route in topLevelScreens) {
                                        fadeIn(tween(250))
                                    } else {
                                        fadeIn(tween(250)) + slideInHorizontally { it / 2 }
                                    }
                                },
                                exitTransition = {
                                    if (initialState.destination.route in topLevelScreens && targetState.destination.route in topLevelScreens) {
                                        fadeOut(tween(200))
                                    } else {
                                        fadeOut(tween(200)) + slideOutHorizontally { -it / 2 }
                                    }
                                },
                                popEnterTransition = {
                                    if ((initialState.destination.route in topLevelScreens || initialState.destination.route?.startsWith(
                                            "search/"
                                        ) == true) && targetState.destination.route in topLevelScreens
                                    ) {
                                        fadeIn(tween(250))
                                    } else {
                                        fadeIn(tween(250)) + slideInHorizontally { -it / 2 }
                                    }
                                },
                                popExitTransition = {
                                    if ((initialState.destination.route in topLevelScreens || initialState.destination.route?.startsWith(
                                            "search/"
                                        ) == true) && targetState.destination.route in topLevelScreens
                                    ) {
                                        fadeOut(tween(200))
                                    } else {
                                        fadeOut(tween(200)) + slideOutHorizontally { it / 2 }
                                    }
                                },
                                modifier = Modifier
                                    .nestedScroll(
                                        if (navigationItems.any { it.route == navBackStackEntry?.destination?.route } ||
                                            navBackStackEntry?.destination?.route?.startsWith("search/") == true) {
                                            searchBarScrollBehavior.nestedScrollConnection
                                        } else {
                                            topAppBarScrollBehavior.nestedScrollConnection
                                        }
                                    )
                            ) {
                                navigationBuilder(
                                    navController,
                                    topAppBarScrollBehavior,
                                    updateAvailable,
                                    playerStyle
                                )
                            }
                        }

                        val currentTitle = remember(navBackStackEntry) {
                            when (navBackStackEntry?.destination?.route) {
                                Screens.Home.route -> R.string.home
                                Screens.Explore.route -> R.string.explore
                                Screens.Library.route -> R.string.filter_library
                                Screens.Songs.route -> R.string.songs
                                Screens.Artists.route -> R.string.artists
                                Screens.Albums.route -> R.string.albums
                                Screens.Playlists.route -> R.string.playlists
                                else -> null
                            }
                        }

                        if (!active && navBackStackEntry?.destination?.route in topLevelScreens && navBackStackEntry?.destination?.route != "settings") {
                            TopAppBar(
                                title = {
                                    Text(
                                        text = currentTitle?.let { stringResource(it) } ?: "",
                                        style = MaterialTheme.typography.titleLarge,
                                    )
                                },
                                actions = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier.padding(end = 8.dp)
                                    ) {
                                        ActionChipButton(
                                            onClick = { navController.navigate("stats") },
                                            icon = {
                                                Icon(
                                                    painter = painterResource(R.drawable.trending_up),
                                                    contentDescription = stringResource(R.string.stats)
                                                )
                                            },
                                            text = stringResource(R.string.stats)
                                        )
                                        ActionChipButton(
                                            onClick = { navController.navigate("history") },
                                            icon = {
                                                Icon(
                                                    painter = painterResource(R.drawable.history),
                                                    contentDescription = stringResource(R.string.history)
                                                )
                                            }
                                        )
                                        ActionChipButton(
                                            onClick = { navController.navigate("settings") },
                                            icon = {
                                                BadgedBox(
                                                    badge = {
                                                        if (latestVersionName != BuildConfig.VERSION_NAME) {
                                                            Badge()
                                                        }
                                                    }
                                                ) {
                                                    Icon(
                                                        painterResource(R.drawable.settings),
                                                        contentDescription = stringResource(R.string.settings)
                                                    )
                                                }
                                            }
                                        )
                                    }
                                },
                                scrollBehavior = searchBarScrollBehavior
                            )
                        } else if (active || navBackStackEntry?.destination?.route?.startsWith("search/") == true) {
                            TopSearch(
                                query = query,
                                onQueryChange = onQueryChange,
                                onSearch = onSearch,
                                active = active,
                                onActiveChange = onActiveChange,
                                scrollBehavior = searchBarScrollBehavior,
                                placeholder = {
                                    Text(
                                        text = stringResource(
                                            when (searchSource) {
                                                SearchSource.LOCAL -> R.string.search_library
                                                SearchSource.ONLINE -> R.string.search_yt_music
                                            }
                                        )
                                    )
                                },
                                leadingIcon = {
                                    IconButton(
                                        onClick = {
                                            when {
                                                active -> onActiveChange(false)
                                                !navigationItems.any { it.route == navBackStackEntry?.destination?.route } -> {
                                                    navController.navigateUp()
                                                }

                                                else -> onActiveChange(true)
                                            }
                                        },
                                        onLongClick = {
                                            when {
                                                active -> {}
                                                !navigationItems.any { it.route == navBackStackEntry?.destination?.route } -> {
                                                    navController.backToMain()
                                                }

                                                else -> {}
                                            }
                                        }
                                    ) {
                                        Icon(
                                            painterResource(
                                                if (active || !navigationItems.any { it.route == navBackStackEntry?.destination?.route }) {
                                                    R.drawable.arrow_back
                                                } else {
                                                    R.drawable.search
                                                }
                                            ),
                                            contentDescription = null
                                        )
                                    }
                                },
                                trailingIcon = {
                                    if (active) {
                                        if (query.text.isNotEmpty()) {
                                            IconButton(
                                                onClick = { onQueryChange(TextFieldValue("")) }
                                            ) {
                                                Icon(
                                                    painter = painterResource(R.drawable.close),
                                                    contentDescription = null
                                                )
                                            }
                                        }
                                        IconButton(
                                            onClick = {
                                                searchSource = searchSource.toggle()
                                            }
                                        ) {
                                            Icon(
                                                painter = painterResource(
                                                    when (searchSource) {
                                                        SearchSource.LOCAL -> R.drawable.library_music
                                                        SearchSource.ONLINE -> R.drawable.language
                                                    }
                                                ),
                                                contentDescription = null
                                            )
                                        }
                                    } else if (navBackStackEntry?.destination?.route in topLevelScreens) {
                                        SettingsIconWithUpdateBadge(
                                            currentVersion = BuildConfig.VERSION_NAME,
                                            onSettingsClick = { navController.navigate("settings") }
                                        )
                                    }
                                },
                                modifier =
                                    Modifier
                                        .focusRequester(searchBarFocusRequester)
                                        .align(Alignment.TopCenter),
                                focusRequester = searchBarFocusRequester
                            ) {
                                Crossfade(
                                    targetState = searchSource,
                                    label = "",
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = if (!playerBottomSheetState.isDismissed) MiniPlayerHeight else 0.dp)
                                        .navigationBarsPadding()
                                ) { searchSource ->
                                    when (searchSource) {
                                        SearchSource.LOCAL -> LocalSearchScreen(
                                            query = query.text,
                                            navController = navController,
                                            onDismiss = { onActiveChange(false) }
                                        )

                                        SearchSource.ONLINE -> OnlineSearchScreen(
                                            query = query.text,
                                            onQueryChange = onQueryChange,
                                            navController = navController,
                                            onSearch = {
                                                navController.navigate("search/${it.urlEncode()}")
                                                if (dataStore[PauseSearchHistoryKey] != true) {
                                                    database.query {
                                                        insert(SearchHistory(query = it))
                                                    }
                                                }
                                            },
                                            onDismiss = { onActiveChange(false) }
                                        )
                                    }
                                }
                            }
                        }

                        Box(modifier = Modifier.fillMaxSize()) {

                            if (firstSetupPassed) {
                                BottomSheetPlayer(
                                    state = playerBottomSheetState,
                                    navController = navController,
                                    isScrolled = isScrolled
                                )
                            }
                            val isPlayerExpanded =
                                playerBottomSheetState.progress > 0.9f

                            if (shouldShowNavigationBar && !isPlayerExpanded) {
                                Box(modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .navigationBarsPadding()
                                ) {
                                    LoudlyBottomNavBar(
                                        navController = navController,
                                        currentDestination = navBackStackEntry?.destination,
                                        isScrolled = isScrolled,
                                        onSearchClick = {
                                            onActiveChange(true)
                                            coroutineScope.launch {
                                                delay(100)
                                                searchBarFocusRequester.requestFocus()
                                            }
                                        }
                                    )
                                }
                            }

                            BottomSheetMenu(
                                state = LocalMenuState.current,
                                modifier = Modifier.align(Alignment.BottomCenter)
                            )
                            sharedSong?.let { song ->
                                playerConnection?.let { _ ->
                                    Dialog(
                                        onDismissRequest = { sharedSong = null },
                                        properties = DialogProperties(usePlatformDefaultWidth = false)
                                    ) {
                                        Surface(
                                            modifier = Modifier.padding(24.dp),
                                            shape = RoundedCornerShape(16.dp),
                                            color = AlertDialogDefaults.containerColor,
                                            tonalElevation = AlertDialogDefaults.TonalElevation
                                        ) {
                                            Column(
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                YouTubeSongMenu(
                                                    song = song,
                                                    navController = navController,
                                                    onDismiss = { sharedSong = null }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    LaunchedEffect(shouldShowSearchBar, openSearchImmediately) {
                        if (shouldShowSearchBar && openSearchImmediately) {
                            onActiveChange(true)
                            try {
                                delay(100)
                                searchBarFocusRequester.requestFocus()
                            } catch (_: Exception) {
                            }
                            openSearchImmediately = false
                        }
                    }
                }
            }
        }
    }

    private fun setSystemBarAppearance(isDark: Boolean) {
        WindowCompat.getInsetsController(window, window.decorView.rootView).apply {
            isAppearanceLightStatusBars = !isDark
            isAppearanceLightNavigationBars = !isDark
        }
    }

    private fun handleIncomingIntent(intent: Intent) {
        lifecycleScope.launch {
            while (playerConnection == null) {
                delay(100)
            }

            val pc = playerConnection ?: return@launch

            when (intent.action) {
                "ACTION_PLAY_SONG" -> {
                    val songId = intent.getStringExtra("SONG_ID")
                    if (songId != null) {
                        val song = withContext(Dispatchers.IO) {
                            database.song(songId).firstOrNull()
                        }
                        if (song != null) {
                            pc.playQueue(YouTubeQueue.Companion.radio(song.toMediaMetadata()))
                        }
                    }
                }

                "ACTION_PLAY_CONTEXTUAL_PLAYLIST" -> {
                    val playlistId =
                        intent.getStringExtra("PAYLOAD")
                    if (playlistId != null) {
                        if (playlistId == ContextualWorker.LIKED_SONGS_FALLBACK_ID) {
                            val likedSongs = withContext(Dispatchers.IO) {
                                database.likedSongs(SongSortType.CREATE_DATE, true).firstOrNull()
                            }
                            if (!likedSongs.isNullOrEmpty()) {
                                pc.player.shuffleModeEnabled = true
                                pc.playQueue(
                                    ListQueue(
                                        title = "Beğenilenler Karışık",
                                        items = likedSongs.map { it.toMediaItem() }
                                    ))
                            }
                        } else {
                            val playlistEntity = withContext(Dispatchers.IO) {
                                database.getPlaylist(playlistId).firstOrNull()
                            }
                            if (playlistEntity != null) {
                                val songsInPlaylist = withContext(Dispatchers.IO) {
                                    database.playlistSongs(playlistId).firstOrNull()
                                }

                                if (!songsInPlaylist.isNullOrEmpty()) {
                                    pc.player.shuffleModeEnabled = false
                                    pc.playQueue(
                                        ListQueue(
                                            title = playlistEntity.name,
                                            items = songsInPlaylist.map { it.song.toMediaItem() }
                                        ))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val ACTION_SEARCH = "com.babelsoftware.loudly.action.SEARCH"
        const val ACTION_SONGS = "com.babelsoftware.loudly.action.SONGS"
        const val ACTION_ARTISTS = "com.babelsoftware.loudly.action.ARTISTS"
        const val ACTION_ALBUMS = "com.babelsoftware.loudly.action.ALBUMS"
        const val ACTION_PLAYLISTS = "com.babelsoftware.loudly.action.PLAYLISTS"
    }
}

val LocalDatabase = staticCompositionLocalOf<MusicDatabase> { error("No database provided") }
val LocalPlayerConnection = staticCompositionLocalOf<PlayerConnection?> { error("No PlayerConnection provided") }
val LocalPlayerAwareWindowInsets = compositionLocalOf<WindowInsets> { error("No WindowInsets provided") }
val LocalDownloadUtil = staticCompositionLocalOf<DownloadUtil> { error("No DownloadUtil provided") }
val LocalSyncUtils = staticCompositionLocalOf<SyncUtils> { error("No SyncUtils provided") }
val LocalSnackbarHostState = staticCompositionLocalOf<SnackbarHostState> { error("No SnackbarHostState provided") }
@Composable
fun SwitchPreference(
    title: @Composable () -> Unit,
    icon: @Composable () -> Unit,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(24.dp)) {
                icon()
            }
            Spacer(Modifier.width(16.dp))
            Box(Modifier.weight(1f)) {
                title()
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
@Composable
fun SettingsIconWithUpdateBadge(
    currentVersion: String,
    onSettingsClick: () -> Unit
) {
    var showUpdateBadge by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        val latestVersion = checkForUpdates()
        if (latestVersion != null) {
            showUpdateBadge = isNewerVersion(latestVersion, currentVersion)
        }
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clip(CircleShape)
            .clickable(onClick = onSettingsClick)
    ) {
        BadgedBox(
            badge = {
                if (showUpdateBadge) {
                    Badge()
                }
            }
        ) {
            Icon(
                painter = painterResource(R.drawable.settings),
                contentDescription = "Configuración"
            )
        }
    }
}
suspend fun checkForUpdates(): String? = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://api.github.com/repos/RRechz/Loudly/releases/latest")
        val connection = url.openConnection()
        connection.connect()
        val json = connection.getInputStream().bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(json)
        return@withContext jsonObject.getString("tag_name")
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext null
    }
}
fun isNewerVersion(remoteVersion: String, currentVersion: String): Boolean {
    val remote = remoteVersion.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
    val current = currentVersion.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
    for (i in 0 until maxOf(remote.size, current.size)) {
        val r = remote.getOrNull(i) ?: 0
        val c = current.getOrNull(i) ?: 0
        if (r > c) return true
        if (r < c) return false
    }
    return false
}