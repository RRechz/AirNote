package com.babelsoftware.loudly.ui.screens.settings

import android.annotation.SuppressLint
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import coil.annotation.ExperimentalCoilApi
import coil.imageLoader
import com.babelsoftware.loudly.BuildConfig
import com.babelsoftware.loudly.LocalPlayerConnection
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.MaxImageCacheSizeKey
import com.babelsoftware.loudly.constants.MaxSongCacheSizeKey
import com.babelsoftware.loudly.extensions.tryOrNull
import com.babelsoftware.loudly.ui.component.IconButton
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingsBox
import com.babelsoftware.loudly.ui.utils.backToMain
import com.babelsoftware.loudly.ui.utils.formatFileSize
import com.babelsoftware.loudly.utils.rememberPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@SuppressLint("PrivateResource")
@OptIn(ExperimentalMaterial3Api::class, ExperimentalCoilApi::class)
@Composable
fun StorageSettings(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
) {
    val context = LocalContext.current
    val imageDiskCache = remember { context.imageLoader.diskCache }
    val playerConnection = LocalPlayerConnection.current
    val playerCache = remember { playerConnection?.service?.playerCache }
    val downloadCache = remember { playerConnection?.service?.downloadCache }
    val coroutineScope = rememberCoroutineScope()
    val (maxImageCacheSize, onMaxImageCacheSizeChange) = rememberPreference(key = MaxImageCacheSizeKey, defaultValue = 512)
    val (maxSongCacheSize, onMaxSongCacheSizeChange) = rememberPreference(key = MaxSongCacheSizeKey, defaultValue = 1024)
    var imageCacheSize by remember { mutableLongStateOf(0L) }
    var playerCacheSize by remember { mutableLongStateOf(0L) }
    var downloadCacheSize by remember { mutableLongStateOf(0L) }
    var translationModelsSize by remember { mutableLongStateOf(0L) }

    val refreshCacheSizes: () -> Unit = {
        coroutineScope.launch(Dispatchers.IO) {
            imageCacheSize = imageDiskCache?.size ?: 0L
            playerCacheSize = tryOrNull { playerCache?.cacheSpace } ?: 0L
            downloadCacheSize = tryOrNull { downloadCache?.cacheSpace } ?: 0L
            translationModelsSize = 123 * 1024 * 1024L
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                refreshCacheSizes()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    var showClearDownloadsDialog by remember { mutableStateOf(false) }
    var showClearSongCacheDialog by remember { mutableStateOf(false) }
    var showClearImagesCacheDialog by remember { mutableStateOf(false) }
    var showClearTranslationModelsDialog by remember { mutableStateOf(false) }
    var showSelectImageCacheSizeDialog by remember { mutableStateOf(false) }
    var showSelectSongCacheSizeDialog by remember { mutableStateOf(false) }

    if (showClearDownloadsDialog) {
        ConfirmationDialog(
            icon = Icons.Outlined.DeleteForever,
            title = stringResource(R.string.clear_all_downloads),
            text = stringResource(R.string.clear_all_downloads_confirmation),
            onConfirm = {
                coroutineScope.launch(Dispatchers.IO) {
                    downloadCache?.keys?.forEach { downloadCache.removeResource(it) }
                    refreshCacheSizes()
                }
            },
            onDismiss = { showClearDownloadsDialog = false }
        )
    }

    if (showSelectSongCacheSizeDialog) {
        CacheSizeSelectionDialog(
            title = stringResource(R.string.max_song_cache_size),
            onDismiss = { showSelectSongCacheSizeDialog = false },
            onSelect = onMaxSongCacheSizeChange
        )
    }
    if (showSelectImageCacheSizeDialog) {
        CacheSizeSelectionDialog(
            title = stringResource(R.string.max_image_cache_size),
            onDismiss = { showSelectImageCacheSizeDialog = false },
            onSelect = onMaxImageCacheSizeChange
        )
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(R.string.storage)) },
                navigationIcon = {
                    IconButton(onClick = navController::navigateUp, onLongClick = navController::backToMain) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                val totalUsed = downloadCacheSize + playerCacheSize + imageCacheSize + translationModelsSize
                val vibrantColors = listOf(
                    Color(0xFF6C63FF), Color(0xFFFF6584), Color(0xFF36C1C1), Color(0xFFFFBD4A)
                )
                val storageItems = listOf(
                    StorageItemData(stringResource(R.string.downloaded_songs), downloadCacheSize, vibrantColors[0]),
                    StorageItemData(stringResource(R.string.song_cache), playerCacheSize, vibrantColors[1]),
                    StorageItemData(stringResource(R.string.image_cache), imageCacheSize, vibrantColors[2]),
                    StorageItemData(stringResource(R.string.translation_models), translationModelsSize, vibrantColors[3])
                )
                StorageOverview(totalUsed = totalUsed, items = storageItems.filter { it.size > 0 })
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val vibrantColors = listOf(
                        Color(0xFF6C63FF), Color(0xFFFF6584), Color(0xFF36C1C1), Color(0xFFFFBD4A)
                    )
                    val storageItems = listOf(
                        StorageItemData(stringResource(R.string.downloaded_songs), downloadCacheSize, vibrantColors[0]),
                        StorageItemData(stringResource(R.string.song_cache), playerCacheSize, vibrantColors[1]),
                        StorageItemData(stringResource(R.string.image_cache), imageCacheSize, vibrantColors[2]),
                        StorageItemData(stringResource(R.string.translation_models), translationModelsSize, vibrantColors[3])
                    ).filter { it.size > 0 }

                    storageItems.forEach { item ->
                        StorageLegend(color = item.color, text = item.label, size = item.size)
                    }
                }
            }

            item {
                Text(
                    text = stringResource(R.string.caches),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(top = 16.dp, bottom = 8.dp, start = 4.dp)
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    CacheControlBox(
                        icon = Icons.Rounded.MusicNote,
                        title = stringResource(R.string.song_cache),
                        usedSpace = playerCacheSize,
                        maxSize = maxSongCacheSize,
                        onMaxSizeClick = { showSelectSongCacheSizeDialog = true },
                        onClearClick = { showClearSongCacheDialog = true }
                    )

                    CacheControlBox(
                        icon = Icons.Rounded.Image,
                        title = stringResource(R.string.image_cache),
                        usedSpace = imageCacheSize,
                        maxSize = maxImageCacheSize,
                        onMaxSizeClick = { showSelectImageCacheSizeDialog = true },
                        onClearClick = { showClearImagesCacheDialog = true }
                    )

                    CacheControlBox(
                        icon = Icons.Outlined.FileDownload,
                        title = stringResource(R.string.downloaded_songs),
                        usedSpace = downloadCacheSize,
                        onClearClick = { showClearDownloadsDialog = true }
                    )

                    if (BuildConfig.FLAVOR != "foss") {
                        CacheControlBox(
                            icon = Icons.Rounded.Translate,
                            title = stringResource(R.string.translation_models),
                            usedSpace = translationModelsSize,
                            onClearClick = { showClearTranslationModelsDialog = true }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Advanced settings card containing the title, field used, maximum size setting, and delete button.
 */
@Composable
private fun CacheControlBox(
    icon: ImageVector,
    title: String,
    usedSpace: Long,
    maxSize: Int? = null,
    onMaxSizeClick: (() -> Unit)? = null,
    onClearClick: () -> Unit
) {
    SettingsBox(
        content = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh, RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

                // Title and Field Used
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.SemiBold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Text(
                        text = stringResource(R.string.currently_used_formatted, formatFileSize(usedSpace)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Actions (Max Size and Delete)
                if (onMaxSizeClick != null && maxSize != null) {
                    TextButton(onClick = onMaxSizeClick) {
                        Text(formatCacheSizeLabel(maxSize))
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                }

                IconButton(onClick = onClearClick) {
                    Icon(
                        imageVector = Icons.Rounded.DeleteForever,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}

@Composable
private fun formatCacheSizeLabel(size: Int): String {
    return when (size) {
        0 -> stringResource(R.string.off)
        -1 -> stringResource(R.string.unlimited)
        else -> formatFileSize(size * 1024 * 1024L)
    }
}

@Composable
private fun CacheSizeSelectionDialog(
    title: String,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            LazyColumn {
                items(listOf(0, 128, 256, 512, 1024, 2048, 4096, 8192, -1).count()) { index ->
                    val size = listOf(0, 128, 256, 512, 1024, 2048, 4096, 8192, -1)[index]
                    TextButton(
                        onClick = { onSelect(size); onDismiss() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(formatCacheSizeLabel(size), modifier = Modifier.fillMaxWidth())
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}

@Composable
private fun StorageOverview(
    totalUsed: Long,
    items: List<StorageItemData>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            val totalForProgress = items.sumOf { it.size }.toFloat().coerceAtLeast(1f)

            val animatedProgress = items.map {
                animateFloatAsState(targetValue = it.size / totalForProgress, label = "${it.label}Progress").value
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                var startAngle = -90f
                animatedProgress.forEachIndexed { index, progress ->
                    val sweepAngle = progress * 360f
                    if (sweepAngle > 0.1f) {
                        drawArc(
                            color = items[index].color,
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            style = Stroke(width = 32.dp.toPx(), cap = StrokeCap.Butt)
                        )
                    }
                    startAngle += sweepAngle
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = stringResource(R.string.total_used_space),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatFileSize(totalUsed),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }
}

data class StorageItemData(val label: String, val size: Long, val color: Color)

@Composable
private fun StorageLegend(color: Color, text: String, size: Long) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Text(text = formatFileSize(size), style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ConfirmationDialog(
    icon: ImageVector,
    title: String,
    text: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = { Icon(icon, null, tint = MaterialTheme.colorScheme.error) },
        title = { Text(title) },
        text = { Text(text) },
        confirmButton = {
            TextButton(
                onClick = {
                    onConfirm()
                    onDismiss()
                },
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text(stringResource(android.R.string.ok))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(android.R.string.cancel))
            }
        }
    )
}
