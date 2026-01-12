package com.babelsoftware.loudly.playback.queues

import androidx.media3.common.MediaItem
import com.babelsoftware.loudly.models.MediaMetadata

object EmptyQueue : Queue {
    override val preloadItem: MediaMetadata? = null
    override suspend fun getInitialStatus() = Queue.Status(null, emptyList(), -1)
    override fun hasNextPage() = false
    override suspend fun nextPage() = emptyList<MediaItem>()
}