package com.babelsoftware.loudly.ui.player

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import coil.compose.AsyncImage
import com.babelsoftware.loudly.LocalDatabase
import com.babelsoftware.loudly.LocalDownloadUtil
import com.babelsoftware.loudly.LocalPlayerConnection
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.MiniPlayerAction
import com.babelsoftware.loudly.constants.MiniPlayerActionButtonKey
import com.babelsoftware.loudly.extensions.togglePlayPause
import com.babelsoftware.loudly.playback.ExoDownloadService
import com.babelsoftware.loudly.utils.dataStore
import kotlinx.coroutines.flow.map

@Composable
fun MiniPlayer(
    position: Long,
    duration: Long,
    isScrolled: Boolean,
    onExpand: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val playerConnection = LocalPlayerConnection.current ?: return
    val mediaMetadata by playerConnection.mediaMetadata.collectAsState()
    val isPlaying by playerConnection.isPlaying.collectAsState()
    val currentSong by playerConnection.currentSong.collectAsState(initial = null)
    val canSkipNext by playerConnection.canSkipNext.collectAsState()
    val canSkipPrevious by playerConnection.canSkipPrevious.collectAsState()

    val context = LocalContext.current
    val downloadUtil = LocalDownloadUtil.current
    val database = LocalDatabase.current

    val download by downloadUtil.getDownload(mediaMetadata?.id ?: "")
        .collectAsState(initial = null)

    val actionButtonType by context.dataStore.data
        .map { preferences ->
            val actionName = preferences[MiniPlayerActionButtonKey] ?: MiniPlayerAction.Like.name
            try {
                MiniPlayerAction.valueOf(actionName)
            } catch (e: IllegalArgumentException) {
                MiniPlayerAction.Like
            }
        }
        .collectAsState(initial = MiniPlayerAction.Like)

    val liquidAnimationSpec = spring<Dp>(
        dampingRatio = 0.8f,
        stiffness = Spring.StiffnessMedium
    )

    val cardHeight by animateDpAsState(targetValue = 60.dp, animationSpec = liquidAnimationSpec, "cardHeight")
    val cardShape = RoundedCornerShape(16.dp)
    val cardPadding by animateDpAsState(if (isScrolled) 8.dp else 12.dp, liquidAnimationSpec, label = "cardPadding")

    val progress = if (duration > 0) (position.toFloat() / duration).coerceIn(0f, 1f) else 0f

    val cardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
    )
    val cardBorder = BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
    val cardElevation = CardDefaults.cardElevation(defaultElevation = 0.dp)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(cardHeight)
            .padding(horizontal = cardPadding)
            .pointerInput(Unit) {
                detectTapGestures(onTap = { onExpand() })
            },
        shape = cardShape,
        colors = cardColors,
        border = cardBorder,
        elevation = cardElevation
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                mediaMetadata?.let {
                    AsyncImage(
                        model = it.thumbnailUrl,
                        contentDescription = "Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    mediaMetadata?.let {
                        Text(
                            text = it.title,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.basicMarquee(),
                        )
                        Text(
                            text = it.artists.joinToString { artist -> artist.name },
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.basicMarquee(),
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    when(actionButtonType) {
                        MiniPlayerAction.Like -> {
                            IconButton(onClick = { playerConnection.toggleLike() }) {
                                Icon(
                                    painter = if (currentSong?.song?.liked == true) painterResource(R.drawable.favorite) else painterResource(R.drawable.favorite_border),
                                    tint = if (currentSong?.song?.liked == true) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    contentDescription = "Like",
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                        MiniPlayerAction.Next -> {
                            IconButton(enabled = canSkipNext, onClick = { playerConnection.player.seekToNext() }) {
                                Icon(
                                    painter = painterResource(R.drawable.skip_next),
                                    contentDescription = "Next",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        MiniPlayerAction.Previous -> {
                            IconButton(enabled = canSkipPrevious, onClick = { playerConnection.player.seekToPrevious() }) {
                                Icon(
                                    painter = painterResource(R.drawable.skip_previous),
                                    contentDescription = "Previous",
                                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        MiniPlayerAction.Download -> {
                            if (mediaMetadata?.isLocal == false) {
                                val downloadState = download?.state
                                val isDownloaded = downloadState == Download.STATE_COMPLETED
                                val isDownloading = downloadState == Download.STATE_DOWNLOADING

                                IconButton(
                                    enabled = !isDownloading,
                                    onClick = {
                                        mediaMetadata?.let { meta ->
                                            if (isDownloaded) {
                                                DownloadService.sendRemoveDownload(context, ExoDownloadService::class.java, meta.id, false)
                                            } else {
                                                database.transaction { insert(meta) }
                                                val downloadRequest = DownloadRequest.Builder(meta.id, meta.id.toUri())
                                                    .setCustomCacheKey(meta.id)
                                                    .setData(meta.title.toByteArray())
                                                    .build()
                                                DownloadService.sendAddDownload(context, ExoDownloadService::class.java, downloadRequest, false)
                                            }
                                        }
                                    }
                                ) {
                                    if (isDownloading) {
                                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                                    } else {
                                        Icon(
                                            painter = if (isDownloaded) painterResource(R.drawable.download_done) else painterResource(R.drawable.download),
                                            contentDescription = if (isDownloaded) "Remove Download" else "Download",
                                            tint = if (isDownloaded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                }
                            }
                        }
                        MiniPlayerAction.None -> { /* Do not show anything */ }
                    }

                    IconButton(onClick = { playerConnection.player.togglePlayPause() }) {
                        Icon(
                            painter = painterResource(if (isPlaying) R.drawable.pause else R.drawable.play),
                            contentDescription = "Play/Pause",
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.9f),
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.dp)
                    .align(Alignment.BottomCenter),
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                trackColor = Color.Transparent,
                strokeCap = StrokeCap.Round
            )
        }
    }
}
