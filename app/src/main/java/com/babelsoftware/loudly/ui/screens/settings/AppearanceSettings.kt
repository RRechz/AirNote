package com.babelsoftware.loudly.ui.screens.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.RenderEffect
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.Cached
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.DesignServices
import androidx.compose.material.icons.rounded.East
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.FavoriteBorder
import androidx.compose.material.icons.rounded.FilterCenterFocus
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.North
import androidx.compose.material.icons.rounded.NorthEast
import androidx.compose.material.icons.rounded.NorthWest
import androidx.compose.material.icons.rounded.QueryStats
import androidx.compose.material.icons.rounded.South
import androidx.compose.material.icons.rounded.SouthEast
import androidx.compose.material.icons.rounded.SouthWest
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.West
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.lerp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.babelsoftware.innertube.utils.parseCookieString
import com.babelsoftware.loudly.LocalPlayerAwareWindowInsets
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.AccountNameKey
import com.babelsoftware.loudly.constants.AppDesignVariantKey
import com.babelsoftware.loudly.constants.AppDesignVariantType
import com.babelsoftware.loudly.constants.AutoPlaylistCachedPlaylistShowKey
import com.babelsoftware.loudly.constants.AutoPlaylistDownloadShowKey
import com.babelsoftware.loudly.constants.AutoPlaylistLikedShowKey
import com.babelsoftware.loudly.constants.AutoPlaylistLocalPlaylistShowKey
import com.babelsoftware.loudly.constants.AutoPlaylistTopPlaylistShowKey
import com.babelsoftware.loudly.constants.AutoPlaylistsCustomizationKey
import com.babelsoftware.loudly.constants.ChipSortTypeKey
import com.babelsoftware.loudly.constants.DarkModeKey
import com.babelsoftware.loudly.constants.DefaultOpenTabKey
import com.babelsoftware.loudly.constants.DefaultOpenTabOldKey
import com.babelsoftware.loudly.constants.DynamicThemeKey
import com.babelsoftware.loudly.constants.GridCellSize
import com.babelsoftware.loudly.constants.GridCellSizeKey
import com.babelsoftware.loudly.constants.HeaderCardAlignmentKey
import com.babelsoftware.loudly.constants.HeaderCardCornerRadiusKey
import com.babelsoftware.loudly.constants.HeaderCardGradientIntensityKey
import com.babelsoftware.loudly.constants.HeaderCardProfilePictureUriKey
import com.babelsoftware.loudly.constants.HeaderImageKey
import com.babelsoftware.loudly.constants.InnerTubeCookieKey
import com.babelsoftware.loudly.constants.LibraryFilter
import com.babelsoftware.loudly.constants.MiniPlayerAction
import com.babelsoftware.loudly.constants.MiniPlayerActionButtonKey
import com.babelsoftware.loudly.constants.PlayerBackgroundStyle
import com.babelsoftware.loudly.constants.PlayerBackgroundStyleKey
import com.babelsoftware.loudly.constants.PlayerStyle
import com.babelsoftware.loudly.constants.PlayerStyleKey
import com.babelsoftware.loudly.constants.PureBlackKey
import com.babelsoftware.loudly.constants.ShowContentFilterKey
import com.babelsoftware.loudly.constants.ShowHeaderCardTextKey
import com.babelsoftware.loudly.constants.ShowRecentActivityKey
import com.babelsoftware.loudly.constants.SliderStyle
import com.babelsoftware.loudly.constants.SliderStyleKey
import com.babelsoftware.loudly.constants.SlimNavBarKey
import com.babelsoftware.loudly.constants.SwipeSongToDismissKey
import com.babelsoftware.loudly.constants.SwipeThumbnailKey
import com.babelsoftware.loudly.constants.ThumbnailCornerRadiusV2Key
import com.babelsoftware.loudly.constants.UiPresetKey
import com.babelsoftware.loudly.ui.component.CounterDialog
import com.babelsoftware.loudly.ui.component.DefaultDialog
import com.babelsoftware.loudly.ui.component.EnumListPreference
import com.babelsoftware.loudly.ui.component.IconButton
import com.babelsoftware.loudly.ui.component.ListPreference
import com.babelsoftware.loudly.ui.component.PlayerSliderTrack
import com.babelsoftware.loudly.ui.screens.settings.card_design.ActionType
import com.babelsoftware.loudly.ui.screens.settings.card_design.IconResource
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingCategory
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingsBox
import com.babelsoftware.loudly.ui.screens.settings.card_design.shapeManager
import com.babelsoftware.loudly.ui.utils.backToMain
import com.babelsoftware.loudly.utils.rememberEnumPreference
import com.babelsoftware.loudly.utils.rememberPreference
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import kotlinx.coroutines.launch
import me.saket.squiggles.SquigglySlider
import kotlin.math.absoluteValue

@SuppressLint("DefaultLocale")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val (appDesignVariant, onAppDesignVariantChange) = rememberEnumPreference(
        AppDesignVariantKey,
        defaultValue = AppDesignVariantType.NEW
    )
    val (dynamicTheme, onDynamicThemeChange) = rememberPreference(
        DynamicThemeKey,
        defaultValue = true
    )
    val (darkMode, onDarkModeChange) = rememberEnumPreference(
        DarkModeKey,
        defaultValue = DarkMode.AUTO
    )
    val (pureBlack, onPureBlackChange) = rememberPreference(PureBlackKey, defaultValue = false)
    val (autoPlaylistsCustomization, onAutoPlaylistsCustomizationChange) = rememberPreference(
        AutoPlaylistsCustomizationKey, defaultValue = false
    )
    val (autoPlaylistLiked, onAutoPlaylistLikedChange) = rememberPreference(
        AutoPlaylistLikedShowKey,
        defaultValue = true
    )
    val (autoPlaylistDownload, onAutoPlaylistDownloadChange) = rememberPreference(
        AutoPlaylistDownloadShowKey, defaultValue = true
    )
    val (autoPlaylistTopPlaylist, onAutoPlaylistTopPlaylistChange) = rememberPreference(
        AutoPlaylistTopPlaylistShowKey, defaultValue = true
    )
    val (autoPlaylistCached, onAutoPlaylistCachedChange) = rememberPreference(
        AutoPlaylistCachedPlaylistShowKey, defaultValue = true
    )
    val (autoPlaylistLocal, onAutoPlaylistLocalChange) = rememberPreference(
        AutoPlaylistLocalPlaylistShowKey, defaultValue = true
    )
    val (swipeSongToDismiss, onSwipeSongToDismissChange) = rememberPreference(
        SwipeSongToDismissKey,
        defaultValue = true
    )
    val (sliderStyle, onSliderStyleChange) = rememberEnumPreference(
        SliderStyleKey,
        defaultValue = SliderStyle.COMPOSE
    )
    val (defaultOpenTabOld, onDefaultOpenTabOldChange) = rememberEnumPreference(
        DefaultOpenTabOldKey,
        defaultValue = NavigationTabOld.HOME
    )
    val (defaultOpenTab, onDefaultOpenTabChange) = rememberEnumPreference(
        DefaultOpenTabKey,
        defaultValue = NavigationTab.HOME
    )
    val (gridCellSize, onGridCellSizeChange) = rememberEnumPreference(
        GridCellSizeKey,
        defaultValue = GridCellSize.SMALL
    )
    val (defaultChip, onDefaultChipChange) = rememberEnumPreference(
        key = ChipSortTypeKey,
        defaultValue = LibraryFilter.LIBRARY
    )
    val (swipeThumbnail, onSwipeThumbnailChange) = rememberPreference(
        SwipeThumbnailKey,
        defaultValue = true
    )
    val (slimNav, onSlimNavChange) = rememberPreference(SlimNavBarKey, defaultValue = true)
    val (thumbnailCornerRadius, onThumbnailCornerRadius) = rememberPreference(
        ThumbnailCornerRadiusV2Key,
        defaultValue = 6
    )
    val (showContentFilter, onShowContentFilterChange) = rememberPreference(
        ShowContentFilterKey,
        defaultValue = true
    )
    val (showRecentActivity,onShowRecentActivityChange) = rememberPreference(
        ShowRecentActivityKey,
        defaultValue = true
    )
    val (playerStyle, onPlayerStyle) = rememberEnumPreference(
        PlayerStyleKey,
        defaultValue = PlayerStyle.NEW
    )
    val (showHeaderCardText, onShowHeaderCardTextChange) = rememberPreference(
        ShowHeaderCardTextKey, defaultValue = true
    )
    val (headerCardAlignment, onHeaderCardAlignmentChange) = rememberEnumPreference(
        HeaderCardAlignmentKey,
        defaultValue = HeaderCardContentAlignment.BottomStart
    )
    var showAlignmentDialog by remember { mutableStateOf(false) }
    val (gradientIntensity, onGradientIntensityChange) = rememberPreference(
        HeaderCardGradientIntensityKey,
        defaultValue = 0.6f
    )
    val (headerCardCornerRadius, onHeaderCardCornerRadiusChange) = rememberPreference(
        HeaderCardCornerRadiusKey,
        defaultValue = 24
    )
    var showHeaderCardCornerDialog by remember { mutableStateOf(false) }
    val (profilePictureUri, onProfilePictureUriChange) = rememberPreference(
        HeaderCardProfilePictureUriKey,
        defaultValue = ""
    )
    var showProfilePictureDialog by remember { mutableStateOf(false) }
    var showGradientDialog by remember { mutableStateOf(false) }

    val (miniPlayerAction, onMiniPlayerActionChange) = rememberEnumPreference(
        MiniPlayerActionButtonKey,
        defaultValue = MiniPlayerAction.Like
    )

    val (innerTubeCookie) = rememberPreference(InnerTubeCookieKey, "")
    val isLoggedIn = remember(innerTubeCookie) {
        "SAPISID" in parseCookieString(innerTubeCookie)
    }

    val isSystemInDarkTheme = isSystemInDarkTheme()
    val useDarkTheme = remember(darkMode, isSystemInDarkTheme) { if (darkMode == DarkMode.AUTO) isSystemInDarkTheme else darkMode == DarkMode.ON }
    val (playerBackground, onPlayerBackgroundChange) = rememberEnumPreference( PlayerBackgroundStyleKey, defaultValue = PlayerBackgroundStyle.DEFAULT,)
    var showCornerRadiusDialog by remember { mutableStateOf(false) }

    val (headerImage, onHeaderImageChange) = rememberPreference(HeaderImageKey, "Loudly-1")
    var showHeaderImageDialog by remember { mutableStateOf(false) }

    var tempImageUri by remember { mutableStateOf<Uri?>(null) }

    var showMiscSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()

    var showDarkModeDialog by remember { mutableStateOf(false) }
    var showPlayerBackgroundDialog by remember { mutableStateOf(false) }
    var showAppDesignDialog by remember { mutableStateOf(false) }
    var showPlayerStyleDialog by remember { mutableStateOf(false) }

    var showMiniPlayerActionDialog by remember { mutableStateOf(false) }

    val cropActivityTitle = stringResource(id = R.string.edit_photo)
    val cropButtonTitle = stringResource(id = R.string.save)
    val toolbarColor = MaterialTheme.colorScheme.surface
    val toolbarWidgetColor = MaterialTheme.colorScheme.onSurface

    val imageCropper = rememberLauncherForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            tempImageUri = result.uriContent
        }
    }

    val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let {
            val cropOptions = CropImageContractOptions(
                uri = it,
                cropImageOptions = CropImageOptions(
                    aspectRatioX = 16,
                    aspectRatioY = 9,
                    fixAspectRatio = true,
                    guidelines = CropImageView.Guidelines.ON,
                    outputCompressQuality = 70,
                    activityTitle = cropActivityTitle,
                    toolbarColor = toolbarColor.toArgb(),
                    toolbarBackButtonColor = toolbarWidgetColor.toArgb(),
                    activityMenuIconColor = toolbarWidgetColor.toArgb(),
                    borderLineColor = toolbarWidgetColor.toArgb(),
                    cropMenuCropButtonTitle = cropButtonTitle
                )
            )
            imageCropper.launch(cropOptions)
        }
    }

    val contentResolver = LocalContext.current.contentResolver
    val profilePicturePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia(),
        onResult = { uri ->
            uri?.let {
                contentResolver.takePersistableUriPermission(it, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                onProfilePictureUriChange(it.toString())
            }
        }
    )

    if (showHeaderImageDialog) {
        HeaderImagePickerDialog(
            initialValue = headerImage,
            tempSelectedUri = tempImageUri,
            onDismiss = {
                showHeaderImageDialog = false
                tempImageUri = null
            },
            onSave = { selectedValue ->
                onHeaderImageChange(selectedValue)
                showHeaderImageDialog = false
                tempImageUri = null
            },
            onGalleryClick = {
                imagePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }
        )
    }

    if (showAppDesignDialog) {
        AlertDialog(
            onDismissRequest = { showAppDesignDialog = false },
            title = { Text(stringResource(R.string.app_design_variant)) },
            text = {
                Column {
                    val options = mapOf(
                        AppDesignVariantType.NEW to stringResource(R.string.app_design_ui_1_5),
                        AppDesignVariantType.OLD to stringResource(R.string.app_design_ui_1_0)
                    )
                    options.forEach { (option, description) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable {
                                onAppDesignVariantChange(option)
                                showAppDesignDialog = false
                            }.padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = (appDesignVariant == option), onClick = {
                                onAppDesignVariantChange(option)
                                showAppDesignDialog = false
                            })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = description)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showAppDesignDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showPlayerStyleDialog) {
        AlertDialog(
            onDismissRequest = { showPlayerStyleDialog = false },
            title = { Text(stringResource(R.string.player_style)) },
            text = {
                Column {
                    val options = mapOf(
                        PlayerStyle.OLD to stringResource(R.string.player_style_old_ui),
                        PlayerStyle.NEW to stringResource(R.string.player_style_ui_1_0),
                        PlayerStyle.UI_2_0 to stringResource(R.string.player_style_ui_2_0)
                    )
                    options.forEach { (option, description) ->
                        Row(
                            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(8.dp)).clickable {
                                onPlayerStyle(option)
                                showPlayerStyleDialog = false
                            }.padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(selected = (playerStyle == option), onClick = {
                                onPlayerStyle(option)
                                showPlayerStyleDialog = false
                            })
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = description)
                        }
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showPlayerStyleDialog = false }) { Text(stringResource(R.string.cancel)) } }
        )
    }

    if (showPlayerBackgroundDialog) {
        AlertDialog(
            onDismissRequest = { showPlayerBackgroundDialog = false },
            title = { Text(stringResource(R.string.player_background_style)) },
            text = {
                Column {
                    val options = listOf(
                        PlayerBackgroundStyle.DEFAULT,
                        PlayerBackgroundStyle.GRADIENT,
                        PlayerBackgroundStyle.BLURMOV,
                        PlayerBackgroundStyle.BLUR
                    )
                    options.forEach { option ->
                        val description = when (option) {
                            PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
                            PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
                            PlayerBackgroundStyle.BLURMOV -> stringResource(R.string.blurmv)
                            PlayerBackgroundStyle.BLUR -> stringResource(R.string.blur)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onPlayerBackgroundChange(option)
                                    showPlayerBackgroundDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (playerBackground == option),
                                onClick = {
                                    onPlayerBackgroundChange(option)
                                    showPlayerBackgroundDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = description)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showPlayerBackgroundDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showDarkModeDialog) {
        AlertDialog(
            onDismissRequest = { showDarkModeDialog = false },
            title = { Text(stringResource(R.string.dark_theme)) },
            text = {
                Column {
                    val options = listOf(DarkMode.ON, DarkMode.OFF, DarkMode.AUTO)
                    options.forEach { option ->
                        val description = when (option) {
                            DarkMode.ON -> stringResource(R.string.dark_theme_on)
                            DarkMode.OFF -> stringResource(R.string.dark_theme_off)
                            DarkMode.AUTO -> stringResource(R.string.dark_theme_follow_system)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    onDarkModeChange(option)
                                    showDarkModeDialog = false
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (darkMode == option),
                                onClick = {
                                    onDarkModeChange(option)
                                    showDarkModeDialog = false
                                }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = description)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDarkModeDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showMiniPlayerActionDialog) {
        MiniPlayerActionDialog(
            initialAction = miniPlayerAction,
            onDismiss = { showMiniPlayerActionDialog = false },
            onSave = { selectedAction ->
                onMiniPlayerActionChange(selectedAction)
                showMiniPlayerActionDialog = false
            }
        )
    }

    if (showMiscSheet) {
        ModalBottomSheet(onDismissRequest = { showMiscSheet = false }, sheetState = sheetState) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                Text(text = stringResource(R.string.misc), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 16.dp))

                SettingsBox(
                    title = stringResource(R.string.auto_playlists_customization),
                    icon = IconResource.Drawable(painterResource(R.drawable.playlist_play)),
                    shape = shapeManager(isFirst = true, isLast = !autoPlaylistsCustomization),
                    actionType = ActionType.SWITCH,
                    isChecked = autoPlaylistsCustomization,
                    onCheckedChange = onAutoPlaylistsCustomizationChange,
                    onClick = { onAutoPlaylistsCustomizationChange(!autoPlaylistsCustomization) }
                )

                AnimatedVisibility(autoPlaylistsCustomization) {
                    Column {
                        SettingsBox(
                            title = stringResource(R.string.show_liked_auto_playlist),
                            icon = IconResource.Vector(Icons.Rounded.Favorite),
                            shape = shapeManager(),
                            actionType = ActionType.SWITCH,
                            isChecked = autoPlaylistLiked,
                            onCheckedChange = onAutoPlaylistLikedChange,
                            onClick = { onAutoPlaylistLikedChange(!autoPlaylistLiked) }
                        )
                        SettingsBox(
                            title = stringResource(R.string.show_download_auto_playlist),
                            icon = IconResource.Vector(Icons.Rounded.CloudDownload),
                            shape = shapeManager(),
                            actionType = ActionType.SWITCH,
                            isChecked = autoPlaylistDownload,
                            onCheckedChange = onAutoPlaylistDownloadChange,
                            onClick = { onAutoPlaylistDownloadChange(!autoPlaylistDownload) }
                        )
                        SettingsBox(
                            title = stringResource(R.string.show_top_auto_playlist),
                            icon = IconResource.Vector(Icons.AutoMirrored.Rounded.TrendingUp),
                            shape = shapeManager(),
                            actionType = ActionType.SWITCH,
                            isChecked = autoPlaylistTopPlaylist,
                            onCheckedChange = onAutoPlaylistTopPlaylistChange,
                            onClick = { onAutoPlaylistTopPlaylistChange(!autoPlaylistTopPlaylist) }
                        )
                        SettingsBox(
                            title = stringResource(R.string.show_cached_auto_playlist),
                            icon = IconResource.Vector(Icons.Rounded.Cached),
                            shape = shapeManager(),
                            actionType = ActionType.SWITCH,
                            isChecked = autoPlaylistCached,
                            onCheckedChange = onAutoPlaylistCachedChange,
                            onClick = { onAutoPlaylistCachedChange(!autoPlaylistCached) }
                        )
                        SettingsBox(
                            title = stringResource(R.string.show_local_auto_playlist),
                            icon = IconResource.Vector(Icons.Rounded.MusicNote),
                            shape = shapeManager(isLast = true),
                            actionType = ActionType.SWITCH,
                            isChecked = autoPlaylistLocal,
                            onCheckedChange = onAutoPlaylistLocalChange,
                            onClick = { onAutoPlaylistLocalChange(!autoPlaylistLocal) }
                        )
                    }
                }

                SettingsBox(
                    title = stringResource(R.string.swipe_song_to_dismiss),
                    icon = IconResource.Drawable(painterResource(R.drawable.queue_music)),
                    shape = shapeManager(isFirst = !autoPlaylistsCustomization),
                    actionType = ActionType.SWITCH,
                    isChecked = swipeSongToDismiss,
                    onCheckedChange = onSwipeSongToDismissChange,
                    onClick = { onSwipeSongToDismissChange(!swipeSongToDismiss) }
                )

                if (appDesignVariant == AppDesignVariantType.NEW) {
                    SettingsBox(shape = shapeManager()) {
                        EnumListPreference(title = { Text(stringResource(R.string.default_open_tab)) }, icon = { Icon(painterResource(R.drawable.tab), null) }, selectedValue = defaultOpenTab, onValueSelected = onDefaultOpenTabChange, valueText = { stringResource(it.stringId) })
                    }
                } else {
                    SettingsBox(shape = shapeManager()) {
                        EnumListPreference(title = { Text(stringResource(R.string.default_open_tab)) }, icon = { Icon(painterResource(R.drawable.tab), null) }, selectedValue = defaultOpenTabOld, onValueSelected = onDefaultOpenTabOldChange, valueText = { stringResource(it.stringId) })
                    }
                }

                SettingsBox(
                    title = stringResource(R.string.slim_navbar),
                    icon = IconResource.Drawable(painterResource(R.drawable.nav_bar)),
                    shape = shapeManager(),
                    actionType = ActionType.SWITCH,
                    isChecked = slimNav,
                    onCheckedChange = onSlimNavChange,
                    onClick = { onSlimNavChange(!slimNav) }
                )

                SettingsBox(shape = shapeManager()) {
                    EnumListPreference(
                        title = { Text(stringResource(R.string.grid_cell_size)) },
                        icon = { Icon(painterResource(R.drawable.grid_view), null) },
                        selectedValue = gridCellSize,
                        onValueSelected = onGridCellSizeChange,
                        valueText = {
                            when (it) {
                                GridCellSize.SMALL -> stringResource(R.string.small)
                                GridCellSize.BIG -> stringResource(R.string.big)
                            }
                        },
                    )
                }

                SettingsBox(shape = shapeManager(isLast = true)) {
                    ListPreference(
                        title = { Text(stringResource(R.string.default_lib_chips)) },
                        icon = { Icon(painterResource(R.drawable.list), null) },
                        selectedValue = defaultChip,
                        values = listOf(
                            LibraryFilter.LIBRARY,
                            LibraryFilter.PLAYLISTS,
                            LibraryFilter.SONGS,
                            LibraryFilter.ALBUMS,
                            LibraryFilter.ARTISTS,
                        ),
                        valueText = {
                            when (it) {
                                LibraryFilter.SONGS -> stringResource(R.string.songs)
                                LibraryFilter.ARTISTS -> stringResource(R.string.artists)
                                LibraryFilter.ALBUMS -> stringResource(R.string.albums)
                                LibraryFilter.PLAYLISTS -> stringResource(R.string.playlists)
                                LibraryFilter.LIBRARY -> stringResource(R.string.filter_library)
                            }
                        },
                        onValueSelected = onDefaultChipChange,
                    )
                }
                Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom)))
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }

    if (showAlignmentDialog) {
        var tempAlignment by remember { mutableStateOf(headerCardAlignment) }
        val (gradientIntensity, _) = rememberPreference(HeaderCardGradientIntensityKey, 0.6f)
        val (headerCardCornerRadius, _) = rememberPreference(HeaderCardCornerRadiusKey, 24)
        val (profilePictureUri, _) = rememberPreference(HeaderCardProfilePictureUriKey, "")
        val (headerImageKey, _) = rememberPreference(HeaderImageKey, "Loudly-1")
        val accountName by rememberPreference(AccountNameKey, "")

        val painter = when {
            headerImageKey.startsWith("content://") -> rememberAsyncImagePainter(model = Uri.parse(headerImageKey))
            headerImageKey == "Loudly-1" -> painterResource(id = R.drawable.loudly_picutre_1)
            headerImageKey == "Loudly-2" -> painterResource(id = R.drawable.loudly_picutre_2)
            headerImageKey == "Loudly-3" -> painterResource(id = R.drawable.loudly_picutre_3)
            else -> painterResource(id = R.drawable.loudly_picutre_1)
        }

        AlertDialog(
            onDismissRequest = { showAlignmentDialog = false },
            title = { Text(stringResource(R.string.text_alignment)) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16 / 9f),
                        shape = RoundedCornerShape(headerCardCornerRadius.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = tempAlignment.alignment
                        ) {
                            Image(painter = painter, contentDescription = null, contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
                            Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(colorStops = arrayOf(0.5f to Color.Transparent, 1.0f to Color.Black.copy(alpha = gradientIntensity)))))
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (profilePictureUri.isNotBlank()) {
                                    Image(painter = rememberAsyncImagePainter(model = Uri.parse(profilePictureUri)), contentDescription = null, modifier = Modifier.size(24.dp).clip(CircleShape), contentScale = ContentScale.Crop)
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(
                                    text = accountName.ifBlank { "User Name" },
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))

                    Column(
                        modifier = Modifier.width(150.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { tempAlignment = HeaderCardContentAlignment.TopStart }) {
                                Icon(Icons.Rounded.NorthWest, contentDescription = "Sol Üst")
                            }
                            IconButton(onClick = { tempAlignment = HeaderCardContentAlignment.TopCenter }) {
                                Icon(Icons.Rounded.North, contentDescription = "Orta Üst")
                            }
                            IconButton(onClick = { tempAlignment = HeaderCardContentAlignment.TopEnd }) {
                                Icon(Icons.Rounded.NorthEast, contentDescription = "Sağ Üst")
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { tempAlignment = HeaderCardContentAlignment.CenterStart }) {
                                Icon(Icons.Rounded.West, contentDescription = "Sol Orta")
                            }
                            IconButton(onClick = { tempAlignment = HeaderCardContentAlignment.Center }) {
                                Icon(Icons.Rounded.FilterCenterFocus, contentDescription = "Orta")
                            }
                            IconButton(onClick = { tempAlignment = HeaderCardContentAlignment.CenterEnd }) {
                                Icon(Icons.Rounded.East, contentDescription = "Sağ Orta")
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(onClick = { tempAlignment = HeaderCardContentAlignment.BottomStart }) {
                                Icon(Icons.Rounded.SouthWest, contentDescription = "Sol Alt")
                            }
                            IconButton(onClick = { tempAlignment = HeaderCardContentAlignment.BottomCenter }) {
                                Icon(Icons.Rounded.South, contentDescription = "Orta Alt")
                            }
                            IconButton(onClick = { tempAlignment = HeaderCardContentAlignment.BottomEnd }) {
                                Icon(Icons.Rounded.SouthEast, contentDescription = "Sağ Alt")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onHeaderCardAlignmentChange(tempAlignment)
                        showAlignmentDialog = false
                    }
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showAlignmentDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (showGradientDialog) {
        var tempIntensity by remember { mutableFloatStateOf(gradientIntensity) }
        AlertDialog(
            onDismissRequest = { showGradientDialog = false },
            title = { Text(stringResource(R.string.gradient_intensity)) },
            text = {
                Column {
                    Text("${"%.0f".format(tempIntensity * 100)}%", modifier = Modifier.padding(bottom = 8.dp))
                    Slider(
                        value = tempIntensity,
                        onValueChange = { tempIntensity = it },
                        valueRange = 0f..1f
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onGradientIntensityChange(tempIntensity)
                        showGradientDialog = false
                    }
                ) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                TextButton(onClick = { showGradientDialog = false }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }

    if (showHeaderCardCornerDialog) {
        CounterDialog(
            title = stringResource(R.string.corner_round),
            onDismiss = { showHeaderCardCornerDialog = false },
            initialValue = headerCardCornerRadius,
            upperBound = 40,
            lowerBound = 0,
            resetValue = 24,
            unitDisplay = "dp",
            onConfirm = {
                onHeaderCardCornerRadiusChange(it)
                showHeaderCardCornerDialog = false
            },
            onCancel = {
                showHeaderCardCornerDialog = false
            },
            onReset = { onHeaderCardCornerRadiusChange(24) },
        )
    }

    if (showProfilePictureDialog) {
        AlertDialog(
            onDismissRequest = { showProfilePictureDialog = false },
            title = { Text(stringResource(R.string.profile_picture)) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Image(
                        painter = if (profilePictureUri.isBlank()) {
                            painterResource(id = R.drawable.person)
                        } else {
                            rememberAsyncImagePainter(model = Uri.parse(profilePictureUri))
                        },
                        contentDescription = "Profile Picture",
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = {
                        profilePicturePicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    }) {
                        Text(stringResource(R.string.select_from_gallery))
                    }
                    if (profilePictureUri.isNotBlank()) {
                        TextButton(onClick = {
                            onProfilePictureUriChange("")
                        }) {
                            Text(stringResource(R.string.remove_profile_picture))
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showProfilePictureDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showCornerRadiusDialog) {
            CounterDialog(
                title = stringResource(R.string.thumbnail_corner_radius),
                onDismiss = { showCornerRadiusDialog = false },
                initialValue = thumbnailCornerRadius,
                upperBound = 10,
                lowerBound = 0,
                resetValue = 6,
                unitDisplay = "0%",
                onConfirm = {
                    showCornerRadiusDialog = false
                    onThumbnailCornerRadius(it)
                },
                onCancel = {
                    showCornerRadiusDialog = false
                },
                onReset = { onThumbnailCornerRadius(6) },
            )
        }
    }

    var showSliderOptionDialog by rememberSaveable {
        mutableStateOf(false)
    }

    if (showSliderOptionDialog) {
        DefaultDialog(
            buttons = {
                TextButton(
                    onClick = { showSliderOptionDialog = false }
                ) {
                    Text(text = stringResource(android.R.string.cancel))
                }
            },
            onDismiss = {
                showSliderOptionDialog = false
            }
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            if (sliderStyle == SliderStyle.DEFAULT) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            onSliderStyleChange(SliderStyle.DEFAULT)
                            showSliderOptionDialog = false
                        }
                        .padding(16.dp)
                ) {
                    var sliderValue by remember {
                        mutableFloatStateOf(0.5f)
                    }
                    Slider(
                        value = sliderValue,
                        valueRange = 0f..1f,
                        onValueChange = {
                            sliderValue = it
                        },
                        thumb = { Spacer(modifier = Modifier.size(0.dp)) },
                        track = { sliderState ->
                            PlayerSliderTrack(
                                sliderState = sliderState,
                                colors = SliderDefaults.colors()
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {}
                                )
                            }
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            if (sliderStyle == SliderStyle.SQUIGGLY) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            onSliderStyleChange(SliderStyle.SQUIGGLY)
                            showSliderOptionDialog = false
                        }
                        .padding(16.dp)
                ) {
                    var sliderValue by remember {
                        mutableFloatStateOf(0.5f)
                    }
                    SquigglySlider(
                        value = sliderValue,
                        valueRange = 0f..1f,
                        onValueChange = {
                            sliderValue = it
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier
                        .aspectRatio(1f)
                        .weight(1f)
                        .clip(RoundedCornerShape(16.dp))
                        .border(
                            1.dp,
                            if (sliderStyle == SliderStyle.COMPOSE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
                            RoundedCornerShape(16.dp)
                        )
                        .clickable {
                            onSliderStyleChange(SliderStyle.COMPOSE)
                            showSliderOptionDialog = false
                        }
                        .padding(16.dp)
                ) {
                    var sliderValue by remember {
                        mutableFloatStateOf(0.5f)
                    }
                    Slider(
                        value = sliderValue,
                        valueRange = 0f..1f,
                        onValueChange = {
                            sliderValue = it
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
    val (selectedPreset, onSelectedPresetChange) = rememberPreference(UiPresetKey, "1.5")

    var showUiDialog by remember { mutableStateOf(false) }

    if (showUiDialog) {
        UiPresetDialog(
            onDismiss = { showUiDialog = false },
            onPresetSelected = { version ->
                onSelectedPresetChange(version)
                when (version) {
                    "1.0" -> {
                        onAppDesignVariantChange(AppDesignVariantType.OLD)
                        onPlayerStyle(PlayerStyle.OLD)
                    }
                    "1.5" -> {
                        onAppDesignVariantChange(AppDesignVariantType.NEW)
                        onPlayerStyle(PlayerStyle.NEW)
                    }
                    "2.0" -> {
                        onPlayerStyle(PlayerStyle.UI_2_0)
                        onAppDesignVariantChange(AppDesignVariantType.NEW)
                    }
                }
            },
            activePreset = selectedPreset
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.appearance)) },
                navigationIcon = {
                    IconButton(
                        onClick = navController::navigateUp,
                        onLongClick = navController::backToMain
                    ) {
                        Icon(
                            painterResource(R.drawable.arrow_back),
                            contentDescription = stringResource(R.string.back)
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Horizontal))
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            SettingCategory(title = stringResource(R.string.theme))
            SettingsBox(
                title = stringResource(R.string.enable_dynamic_theme),
                icon = IconResource.Drawable(painterResource(R.drawable.palette)),
                shape = shapeManager(isFirst = true),
                actionType = ActionType.SWITCH,
                isChecked = dynamicTheme,
                onCheckedChange = onDynamicThemeChange,
                onClick = { onDynamicThemeChange(!dynamicTheme) }
            )
            val darkModeDesc = when (darkMode) {
                DarkMode.ON -> stringResource(R.string.dark_theme_on)
                DarkMode.OFF -> stringResource(R.string.dark_theme_off)
                DarkMode.AUTO -> stringResource(R.string.dark_theme_follow_system)
            }
            SettingsBox(
                title = stringResource(R.string.dark_theme),
                description = darkModeDesc,
                icon = IconResource.Drawable(painterResource(R.drawable.dark_mode)),
                shape = shapeManager(),
                onClick = { showDarkModeDialog = true }
            )
            AnimatedVisibility(useDarkTheme) {
                SettingsBox(
                    title = stringResource(R.string.pure_black),
                    icon = IconResource.Drawable(painterResource(R.drawable.contrast)),
                    shape = shapeManager(isLast = !useDarkTheme),
                    actionType = ActionType.SWITCH,
                    isChecked = pureBlack,
                    onCheckedChange = onPureBlackChange,
                    onClick = { onPureBlackChange(!pureBlack) }
                )
            }

            if (selectedPreset == "Custom") {
                SettingsBox(
                    title = stringResource(R.string.app_ui),
                    description = "v$selectedPreset",
                    icon = IconResource.Vector(Icons.Rounded.DesignServices),
                    shape = shapeManager(isFirst = false, isLast = false),
                    onClick = { showUiDialog = true }
                )
                Column {
                    val appDesignDesc = when (appDesignVariant) {
                        AppDesignVariantType.NEW -> stringResource(R.string.app_design_ui_1_5)
                        AppDesignVariantType.OLD -> stringResource(R.string.app_design_ui_1_0)
                    }
                    SettingsBox(
                        title = stringResource(R.string.app_design_variant),
                        description = appDesignDesc,
                        icon = IconResource.Vector(Icons.Rounded.DesignServices),
                        shape = shapeManager(isFirst = false),
                        onClick = { showAppDesignDialog = true }
                    )

                    val playerStyleDesc = when (playerStyle) {
                        PlayerStyle.OLD -> stringResource(R.string.player_style_old_ui)
                        PlayerStyle.NEW -> stringResource(R.string.player_style_ui_1_0)
                        PlayerStyle.UI_2_0 -> stringResource(R.string.player_style_ui_2_0)
                    }
                    SettingsBox(
                        title = stringResource(R.string.player_style),
                        description = playerStyleDesc,
                        icon = IconResource.Drawable(painterResource(R.drawable.play)),
                        shape = shapeManager(isLast = true),
                        onClick = { showPlayerStyleDialog = true }
                    )
                }

            } else {
                SettingsBox(
                    title = stringResource(R.string.app_ui),
                    description = "v$selectedPreset",
                    icon = IconResource.Vector(Icons.Rounded.DesignServices),
                    shape = shapeManager(isFirst = false, isLast = true),
                    onClick = { showUiDialog = true }
                )
            }

            SettingCategory(title = stringResource(R.string.player))
            val sliderStyleDesc = when (sliderStyle) {
                SliderStyle.DEFAULT -> stringResource(R.string.default_)
                SliderStyle.SQUIGGLY -> stringResource(R.string.squiggly)
                SliderStyle.COMPOSE -> stringResource(R.string.compose)
            }
            SettingsBox(
                title = stringResource(R.string.slider_style),
                description = sliderStyleDesc,
                icon = IconResource.Drawable(painterResource(R.drawable.sliders)),
                shape = shapeManager(isFirst = true),
                onClick = { showSliderOptionDialog = true }
            )
            val playerBackgroundDesc = when (playerBackground) {
                PlayerBackgroundStyle.DEFAULT -> stringResource(R.string.follow_theme)
                PlayerBackgroundStyle.GRADIENT -> stringResource(R.string.gradient)
                PlayerBackgroundStyle.BLURMOV -> stringResource(R.string.blurmv)
                PlayerBackgroundStyle.BLUR -> stringResource(R.string.blur)
            }
            SettingsBox(
                title = stringResource(R.string.player_background_style),
                description = playerBackgroundDesc,
                icon = IconResource.Drawable(painterResource(R.drawable.gradient)),
                shape = shapeManager(),
                onClick = { showPlayerBackgroundDialog = true }
            )

            val miniPlayerActionDesc = when (miniPlayerAction) {
                MiniPlayerAction.Like -> stringResource(R.string.like)
                MiniPlayerAction.Next -> stringResource(R.string.next)
                MiniPlayerAction.Previous -> stringResource(R.string.previous)
                MiniPlayerAction.Download -> stringResource(R.string.download)
                MiniPlayerAction.None -> stringResource(R.string.none)
            }
            SettingsBox(
                title = stringResource(R.string.mini_player_action_button),
                description = miniPlayerActionDesc,
                icon = IconResource.Drawable(painterResource(R.drawable.play)),
                shape = shapeManager(),
                onClick = { showMiniPlayerActionDialog = true }
            )

            SettingsBox(
                title = stringResource(R.string.enable_swipe_thumbnail),
                icon = IconResource.Drawable(painterResource(R.drawable.swipe)),
                shape = shapeManager(),
                actionType = ActionType.SWITCH,
                isChecked = swipeThumbnail,
                onCheckedChange = onSwipeThumbnailChange,
                onClick = { onSwipeThumbnailChange(!swipeThumbnail) }
            )
            SettingsBox(
                title = stringResource(R.string.thumbnail_corner_radius),
                description = "$thumbnailCornerRadius" + "0%",
                icon = IconResource.Vector(Icons.Rounded.Image),
                shape = shapeManager(isLast = true),
                onClick = { showCornerRadiusDialog = true }
            )

            SettingCategory(title = stringResource(R.string.profile_card))
            SettingsBox(
                title = stringResource(R.string.show_texts),
                description = stringResource(R.string.show_texts_description),
                icon = IconResource.Drawable(painterResource(R.drawable.badge)),
                shape = shapeManager(isFirst = true, isLast = false),
                actionType = ActionType.SWITCH,
                isChecked = showHeaderCardText,
                onCheckedChange = onShowHeaderCardTextChange,
                onClick = { onShowHeaderCardTextChange(!showHeaderCardText) }
            )
            SettingsBox(
                title = stringResource(R.string.header_background),
                description = stringResource(R.string.header_background_description),
                icon = IconResource.Vector(Icons.Rounded.Image),
                shape = shapeManager(isFirst = false, isLast = false),
                onClick = { showHeaderImageDialog = true }
            )
            SettingsBox(
                title = stringResource(R.string.text_alignment),
                description = stringResource(R.string.text_alignment_description),
                icon = IconResource.Drawable(painterResource(R.drawable.format_align_left)),
                shape = shapeManager(isFirst = false, isLast = false),
                onClick = { showAlignmentDialog = true }
            )
            SettingsBox(
                title = stringResource(R.string.gradient_intensity),
                description = "%${"%.0f".format(gradientIntensity * 100)}",
                icon = IconResource.Drawable(painterResource(R.drawable.gradient)),
                shape = shapeManager(isFirst = false, isLast = false),
                onClick = { showGradientDialog = true }
            )
            SettingsBox(
                title = stringResource(R.string.corner_round),
                description = "${headerCardCornerRadius}dp",
                icon = IconResource.Drawable(painterResource(R.drawable.rounded_corner)),
                shape = shapeManager(isFirst = false, isLast = false),
                onClick = { showHeaderCardCornerDialog = true }
            )
            SettingsBox(
                title = stringResource(R.string.profile_picture),
                description = if (profilePictureUri.isBlank()) stringResource(R.string.not_selected) else stringResource(R.string.tap_to_change),
                icon = IconResource.Drawable(painterResource(R.drawable.account_circle)),
                shape = shapeManager(isFirst = false, isLast = true),
                onClick = { showProfilePictureDialog = true }
            )

            SettingCategory(title = stringResource(R.string.home))

            if (isLoggedIn) {
                SettingsBox(
                    title = stringResource(R.string.show_content_filter),
                    icon = IconResource.Vector(Icons.Rounded.FilterList),
                    shape = shapeManager(isFirst = true, isLast = false),
                    actionType = ActionType.SWITCH,
                    isChecked = showContentFilter,
                    onCheckedChange = onShowContentFilterChange,
                    onClick = { onShowContentFilterChange(!showContentFilter) }
                )
                SettingsBox(
                    title = stringResource(R.string.recent_activity),
                    icon = IconResource.Vector(Icons.Rounded.QueryStats),
                    shape = shapeManager(isFirst = false, isLast = true),
                    actionType = ActionType.SWITCH,
                    isChecked = showRecentActivity,
                    onCheckedChange = onShowRecentActivityChange,
                    onClick = { onShowRecentActivityChange(!showRecentActivity) }
                )
            } else {
                SettingsBox(
                    title = stringResource(R.string.show_content_filter),
                    icon = IconResource.Vector(Icons.Rounded.FilterList),
                    shape = shapeManager(isBoth = true),
                    actionType = ActionType.SWITCH,
                    isChecked = showContentFilter,
                    onCheckedChange = onShowContentFilterChange,
                    onClick = { onShowContentFilterChange(!showContentFilter) }
                )
            }

            SettingCategory(title = stringResource(R.string.misc))
            SettingsBox(
                title = stringResource(R.string.other_settings),
                description = stringResource(R.string.other_settings_description),
                icon = IconResource.Vector(Icons.Rounded.Tune),
                shape = shapeManager(isBoth = true),
                onClick = { showMiscSheet = true }
            )
            Spacer(Modifier.windowInsetsPadding(LocalPlayerAwareWindowInsets.current.only(WindowInsetsSides.Bottom)))
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun MiniPlayerActionDialog(
    initialAction: MiniPlayerAction,
    onDismiss: () -> Unit,
    onSave: (MiniPlayerAction) -> Unit
) {
    var selectedAction by remember { mutableStateOf(initialAction) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.mini_player_action_button)) },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // --- MiniPlayer PREVIEW ---
                Text(
                    text = stringResource(R.string.preview),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                MiniPlayerPreview(action = selectedAction)
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                Column {
                    val options = MiniPlayerAction.values()
                    options.forEach { option ->
                        val description = when (option) {
                            MiniPlayerAction.Like -> stringResource(R.string.like)
                            MiniPlayerAction.Next -> stringResource(R.string.next)
                            MiniPlayerAction.Previous -> stringResource(R.string.previous)
                            MiniPlayerAction.Download -> stringResource(R.string.download)
                            MiniPlayerAction.None -> stringResource(R.string.none)
                        }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedAction = option }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (selectedAction == option),
                                onClick = { selectedAction = option }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = description)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(selectedAction) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun MiniPlayerPreview(action: MiniPlayerAction) {
    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    )
    val cardBorder = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp),
        shape = RoundedCornerShape(16.dp),
        colors = cardColors,
        border = cardBorder,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Placeholder Album Art
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.loudly_monochrome),
                    contentDescription = "Album Art",
                    modifier = Modifier.align(Alignment.Center).size(24.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            // Placeholder Song Info
            Column(modifier = Modifier.weight(1f)) {
                Text("Song Name", fontWeight = FontWeight.SemiBold)
                Text("Artist", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
            }

            // Placeholder Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                when (action) {
                    MiniPlayerAction.Like -> {
                        Icon(
                            imageVector = Icons.Rounded.FavoriteBorder,
                            contentDescription = "Like",
                            modifier = Modifier.size(26.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                    MiniPlayerAction.Next -> {
                        Icon(
                            painter = painterResource(R.drawable.skip_next),
                            contentDescription = "Next",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                    MiniPlayerAction.Previous -> {
                        Icon(
                            painter = painterResource(R.drawable.skip_previous),
                            contentDescription = "Previous",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                    MiniPlayerAction.None -> {}
                    MiniPlayerAction.Download -> {
                        Icon(
                            painter = painterResource(R.drawable.download),
                            contentDescription = "Download",
                            modifier = Modifier.size(28.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                        )
                    }
                }
                Icon(
                    painter = painterResource(R.drawable.play),
                    contentDescription = "Play/Pause",
                    modifier = Modifier.size(32.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f)
                )
            }
        }
    }
}
private data class PredefinedImage(val key: String, val drawableRes: Int)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HeaderImagePickerDialog(
    initialValue: String,
    tempSelectedUri: Uri?,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onGalleryClick: () -> Unit,
) {
    val predefinedImages = remember {
        listOf(
            PredefinedImage("Loudly-1", R.drawable.loudly_picutre_1),
            PredefinedImage("Loudly-2", R.drawable.loudly_picutre_2),
            PredefinedImage("Loudly-3", R.drawable.loudly_picutre_3),
            PredefinedImage("Loudly-4", R.drawable.loudly_picutre_4),
            PredefinedImage("Loudly-5", R.drawable.loudly_picutre_5),
            PredefinedImage("Loudly-6", R.drawable.loudly_picutre_6)
        )
    }

    val initialPageIndex = remember {
        predefinedImages.indexOfFirst { it.key == initialValue }.coerceAtLeast(0)
    }

    val pagerState = rememberPagerState(initialPage = initialPageIndex) {
        predefinedImages.size
    }

    val scope = rememberCoroutineScope()

    var hasGallerySelection by remember { mutableStateOf(tempSelectedUri != null) }
    LaunchedEffect(tempSelectedUri) {
        hasGallerySelection = tempSelectedUri != null
    }

    val currentSelectionKey by remember {
        derivedStateOf {
            if (hasGallerySelection) {
                tempSelectedUri.toString()
            } else {
                predefinedImages[pagerState.currentPage].key
            }
        }
    }

    val (profilePictureUri) = rememberPreference(HeaderCardProfilePictureUriKey, "")
    val (accountName) = rememberPreference(AccountNameKey, "")
    val (headerCardAlignment) = rememberEnumPreference(HeaderCardAlignmentKey, HeaderCardContentAlignment.BottomStart)
    val (gradientIntensity) = rememberPreference(HeaderCardGradientIntensityKey, 0.6f)


    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(id = R.string.select_header_image),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // --- PREVIEW ---
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .aspectRatio(16 / 9f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = headerCardAlignment.alignment
                    ) {
                        val currentImageSource: Any by remember {
                            derivedStateOf {
                                if (hasGallerySelection) {
                                    tempSelectedUri ?: R.drawable.loudly_picutre_1
                                } else {
                                    predefinedImages[pagerState.currentPage].drawableRes
                                }
                            }
                        }

                        Crossfade(targetState = currentImageSource, animationSpec = tween(durationMillis = 500), label = "imageCrossfade") { imageSource ->
                            Image(
                                painter = when (imageSource) {
                                    is Uri -> rememberAsyncImagePainter(model = imageSource)
                                    is Int -> painterResource(id = imageSource)
                                    else -> painterResource(id = R.drawable.loudly_picutre_1)
                                },
                                contentDescription = "Preview",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        colorStops = arrayOf(
                                            0.5f to Color.Transparent,
                                            1.0f to Color.Black.copy(alpha = gradientIntensity)
                                        )
                                    )
                                )
                        )

                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (profilePictureUri.isNotBlank()) {
                                Image(
                                    painter = rememberAsyncImagePainter(model = Uri.parse(profilePictureUri)),
                                    contentDescription = "Profile Picture",
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(
                                text = accountName.ifBlank { "Your Account Name" },
                                color = Color.White,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                maxLines = 2
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                val itemWidth = 200.dp // Width of items
                val itemHeight = itemWidth * (9f / 16f) // Height in 16:9 aspect ratio
                val itemPadding = (-16).dp // Negative space between elements, to make them closer together
                val horizontalPadding = 40.dp // Total padding from the left and right edges

                HorizontalPager(
                    state = pagerState,
                    pageSize = PageSize.Fixed(itemWidth),
                    contentPadding = PaddingValues(horizontal = horizontalPadding),
                    pageSpacing = itemPadding,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(itemHeight + 32.dp)
                ) { page ->
                    val image = predefinedImages[page]
                    val pageOffset = (
                            (pagerState.currentPage - page) + pagerState
                                .currentPageOffsetFraction
                            ).absoluteValue

                    // Material 3 Expressive animations
                    val scaleFactor = lerp(0.7f, 1f, 1f - pageOffset.coerceIn(0f, 1f)) // Scale factor
                    val alphaFactor = lerp(0.3f, 1f, 1f - pageOffset.coerceIn(0f, 1f)) // Visibility
                    val blurFactor = lerp(8f, 0f, 1f - pageOffset.coerceIn(0f, 1f)) // Blur (center 0)

                    Card(
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier
                            .width(itemWidth)
                            .height(itemHeight)
                            .graphicsLayer {
                                scaleX = scaleFactor
                                scaleY = scaleFactor
                                transformOrigin = TransformOrigin.Center
                                this.alpha = alphaFactor

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                    renderEffect = if (blurFactor > 0) {
                                        RenderEffect.createBlurEffect(
                                            blurFactor,
                                            blurFactor,
                                            android.graphics.Shader.TileMode.DECAL
                                        ).asComposeRenderEffect()
                                    } else {
                                        null
                                    }
                                } else if (blurFactor > 0) {
                                    this.alpha = alphaFactor * (1f - blurFactor / 8f)
                                }
                            }
                            .clickable {
                                hasGallerySelection = false
                                if (pagerState.currentPage != page) {
                                    scope.launch {
                                        pagerState.animateScrollToPage(page)
                                    }
                                }
                            }
                    ) {
                        Image(
                            painter = painterResource(id = image.drawableRes),
                            contentDescription = image.key,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // --- GALLERY BUTTON ---
                Button(
                    onClick = {
                        hasGallerySelection = true
                        onGalleryClick()
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(
                        text = stringResource(id = R.string.select_from_gallery),
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(currentSelectionKey) }) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
private fun UiPresetDialog(
    onDismiss: () -> Unit,
    onPresetSelected: (String) -> Unit,
    activePreset: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.ui_version_selection)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                mapOf(
                    "1.0" to stringResource(R.string.ui_version_1_0_desc),
                    "1.5" to stringResource(R.string.ui_version_1_5_desc),
                    "2.0" to stringResource(R.string.ui_version_2_0_desc),
                    "Custom" to stringResource(R.string.ui_version_custom_desc)
                ).forEach { (version, description) ->
                    val isSelected = activePreset == version
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                onPresetSelected(version)
                                onDismiss()
                            }
                            .border(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("App UI v$version", fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(4.dp))
                            Text(description, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

enum class DarkMode {
    ON, OFF, AUTO
}

enum class NavigationTabOld(val stringId: Int) {
    HOME(R.string.home),
    EXPLORE(R.string.explore),
    SONGS(R.string.songs),
    ARTISTS(R.string.artists),
    ALBUMS(R.string.albums),
    PLAYLISTS(R.string.playlists)
}

enum class NavigationTab(val stringId: Int) {
    HOME(R.string.home),
    LIBRARY(R.string.library),
    EXPLORE(R.string.explore),
    SONGS(R.string.songs),
    ARTISTS(R.string.artists),
    ALBUMS(R.string.albums),
    PLAYLISTS(R.string.playlists)
}

enum class LyricsPosition {
    LEFT, CENTER, RIGHT
}

enum class HeaderCardContentAlignment(val alignment: Alignment) {
    TopStart(Alignment.TopStart),
    TopCenter(Alignment.TopCenter),
    TopEnd(Alignment.TopEnd),
    CenterStart(Alignment.CenterStart),
    Center(Alignment.Center),
    CenterEnd(Alignment.CenterEnd),
    BottomStart(Alignment.BottomStart),
    BottomCenter(Alignment.BottomCenter),
    BottomEnd(Alignment.BottomEnd)
}