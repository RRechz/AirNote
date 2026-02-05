package com.babelsoftware.loudly.playback

import android.content.Context
import android.content.Intent
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Player.COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SEEK_TO_NEXT_MEDIA_ITEM
import androidx.media3.common.Player.COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM
import androidx.media3.common.Player.REPEAT_MODE_OFF
import androidx.media3.common.Player.STATE_ENDED
import androidx.media3.common.Timeline
import com.babelsoftware.loudly.MusicWidget
import com.babelsoftware.loudly.constants.TranslateLyricsKey
import com.babelsoftware.loudly.db.MusicDatabase
import com.babelsoftware.loudly.extensions.currentMetadata
import com.babelsoftware.loudly.extensions.getCurrentQueueIndex
import com.babelsoftware.loudly.extensions.getQueueWindows
import com.babelsoftware.loudly.extensions.metadata
import com.babelsoftware.loudly.playback.queues.Queue
import com.babelsoftware.loudly.TranslationHelper
import com.babelsoftware.loudly.db.entities.LyricsEntity
import com.babelsoftware.loudly.models.MediaMetadata
import com.babelsoftware.loudly.utils.dataStore
import com.babelsoftware.loudly.reportException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SleepTimerState(
    val isActive: Boolean = false,
    val remainingSeconds: Long = 0
)

sealed class StreakInfo {
    object None : StreakInfo()
    data class AlbumStreak(val album: MediaMetadata.Album) : StreakInfo()
    data class ArtistStreak(val artist: MediaMetadata.Artist) : StreakInfo()
}

@OptIn(ExperimentalCoroutinesApi::class)
class PlayerConnection(
    private val context: Context,
    binder: MusicService.MusicBinder,
    val database: MusicDatabase,
    private val scope: CoroutineScope,
) : Player.Listener {
    val service = binder.service
    val player = service.player

    val playbackState = MutableStateFlow(player.playbackState)
    private val playWhenReady = MutableStateFlow(player.playWhenReady)
    val isPlaying = combine(playbackState, playWhenReady) { playbackState, playWhenReady ->
        playWhenReady && playbackState != STATE_ENDED
    }.stateIn(scope, SharingStarted.Lazily, player.playWhenReady && player.playbackState != STATE_ENDED)
    val mediaMetadata = MutableStateFlow(player.currentMetadata)
    val currentSong = mediaMetadata.flatMapLatest {
        database.song(it?.id)
    }
    internal val translating = MutableStateFlow(false)
    val currentLyrics = combine(
        context.dataStore.data.map {
            it[TranslateLyricsKey] ?: false
        }.distinctUntilChanged(),
        mediaMetadata.flatMapLatest { mediaMetadata ->
            database.lyrics(mediaMetadata?.id)
        }
    ) { translateEnabled, lyrics ->
        if (!translateEnabled || lyrics == null || lyrics.lyrics == LyricsEntity.Companion.LYRICS_NOT_FOUND) return@combine lyrics
        translating.value = true
        try {
            TranslationHelper.translate(lyrics)
        } catch (e: Exception) {
            reportException(e)
            lyrics
        }.also {
            translating.value = false
        }
    }.stateIn(scope, SharingStarted.Lazily, null)
    val currentFormat = service.currentPlaybackFormat.stateIn(scope, SharingStarted.Lazily, null)

    val queueTitle = MutableStateFlow<String?>(null)
    val queueWindows = MutableStateFlow<List<Timeline.Window>>(emptyList())
    private val currentMediaItemIndex = MutableStateFlow(-1)
    val currentWindowIndex = MutableStateFlow(-1)

    val shuffleModeEnabled = MutableStateFlow(false)
    val repeatMode = MutableStateFlow(REPEAT_MODE_OFF)

    val canSkipPrevious = MutableStateFlow(true)
    val canSkipNext = MutableStateFlow(true)

    val error = MutableStateFlow<PlaybackException?>(null)

    val isRetryingSong = service.isRetryingSong.stateIn(scope, SharingStarted.Lazily, false)

    private val _sleepTimerState = MutableStateFlow(SleepTimerState())
    val sleepTimerState: StateFlow<SleepTimerState> = _sleepTimerState

    private var previousMediaMetadata: MediaMetadata? = null
    private var streakTimerJob: Job? = null

    private val _streakState = MutableStateFlow<StreakInfo>(StreakInfo.None)
    val streakState: StateFlow<StreakInfo> = _streakState

    fun startSleepTimer(minutes: Int) {
        service.sleepTimer.start(minutes)
    }

    fun cancelSleepTimer() {
        service.sleepTimer.clear()
    }

    init {
        player.addListener(this)

        playbackState.value = player.playbackState
        playWhenReady.value = player.playWhenReady
        mediaMetadata.value = player.currentMetadata
        queueTitle.value = service.queueTitle
        queueWindows.value = player.getQueueWindows()
        currentWindowIndex.value = player.getCurrentQueueIndex()
        currentMediaItemIndex.value = player.currentMediaItemIndex
        shuffleModeEnabled.value = player.shuffleModeEnabled
        repeatMode.value = player.repeatMode

        scope.launch {
            while (true) {
                val timer = service.sleepTimer
                val isActive = timer.isActive
                val remaining = if (isActive) {
                    if (timer.pauseWhenSongEnd) {
                        (player.duration - player.currentPosition).coerceAtLeast(0) / 1000
                    } else {
                        (timer.triggerTime - System.currentTimeMillis()).coerceAtLeast(0) / 1000
                    }
                } else 0

                _sleepTimerState.value = SleepTimerState(isActive, remaining)
                delay(1000)
            }
        }
    }

    fun playQueue(queue: Queue) {
        service.playQueue(queue)
    }

    fun playNext(item: MediaItem) = playNext(listOf(item))
    fun playNext(items: List<MediaItem>) {
        service.playNext(items)
    }

    fun addToQueue(item: MediaItem) = addToQueue(listOf(item))
    fun addToQueue(items: List<MediaItem>) {
        service.addToQueue(items)
    }

    fun toggleLike() {
        service.toggleLike()
    }

    fun seekToNext() {
        player.seekToNext()
        player.prepare()
        player.playWhenReady = true
    }

    fun seekToPrevious() {
        player.seekToPrevious()
        player.prepare()
        player.playWhenReady = true
    }

    fun startRadioFromStreak(streakInfo: StreakInfo) {
        val radioMediaItem = when (streakInfo) {
            is StreakInfo.AlbumStreak -> mediaMetadata.value?.takeIf { it.album?.id == streakInfo.album.id }
            is StreakInfo.ArtistStreak -> mediaMetadata.value?.takeIf { it.artists.any { artist -> artist.id == streakInfo.artist.id } }
            else -> null
        }
        radioMediaItem?.let {
            service.startRadioSeamlessly()
        }
    }

    override fun onPlaybackStateChanged(state: Int) {
        playbackState.value = state
        error.value = player.playerError
        sendWidgetUpdateBroadcast()
    }

    override fun onPlayWhenReadyChanged(newPlayWhenReady: Boolean, reason: Int) {
        playWhenReady.value = newPlayWhenReady
        sendWidgetUpdateBroadcast()
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        val currentMetadata = mediaItem?.metadata
        mediaMetadata.value = currentMetadata
        currentMediaItemIndex.value = player.currentMediaItemIndex
        currentWindowIndex.value = player.getCurrentQueueIndex()
        updateCanSkipPreviousAndNext()
        sendWidgetUpdateBroadcast()
        streakTimerJob?.cancel()

        val previous = previousMediaMetadata
        if (currentMetadata != null && previous != null && reason != Player.MEDIA_ITEM_TRANSITION_REASON_SEEK) {
            val currentAlbumId = currentMetadata.album?.id
            if (currentAlbumId != null && currentAlbumId == previous.album?.id) {
                _streakState.value = StreakInfo.AlbumStreak(currentMetadata.album)
            }
            else {
                val currentArtistId = currentMetadata.artists.firstOrNull()?.id
                if (currentArtistId != null && currentArtistId == previous.artists.firstOrNull()?.id) {
                    _streakState.value = StreakInfo.ArtistStreak(currentMetadata.artists.first())
                } else {
                    _streakState.value = StreakInfo.None
                }
            }
        } else {
            _streakState.value = StreakInfo.None
        }
        if (_streakState.value !is StreakInfo.None) {
            streakTimerJob = scope.launch {
                delay(7000)
                _streakState.value = StreakInfo.None
            }
        }
        previousMediaMetadata = currentMetadata
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        queueWindows.value = player.getQueueWindows()
        queueTitle.value = service.queueTitle
        currentMediaItemIndex.value = player.currentMediaItemIndex
        currentWindowIndex.value = player.getCurrentQueueIndex()
        updateCanSkipPreviousAndNext()
        sendWidgetUpdateBroadcast()
    }

    override fun onShuffleModeEnabledChanged(enabled: Boolean) {
        shuffleModeEnabled.value = enabled
        queueWindows.value = player.getQueueWindows()
        currentWindowIndex.value = player.getCurrentQueueIndex()
        updateCanSkipPreviousAndNext()
    }

    override fun onRepeatModeChanged(mode: Int) {
        repeatMode.value = mode
        updateCanSkipPreviousAndNext()
    }

    override fun onPlayerErrorChanged(playbackError: PlaybackException?) {
        if (playbackError != null) {
            reportException(playbackError)
        }
        error.value = playbackError
    }

    private fun updateCanSkipPreviousAndNext() {
        if (!player.currentTimeline.isEmpty) {
            val window = player.currentTimeline.getWindow(player.currentMediaItemIndex, Timeline.Window())
            canSkipPrevious.value = player.isCommandAvailable(COMMAND_SEEK_IN_CURRENT_MEDIA_ITEM)
                    || !window.isLive
                    || player.isCommandAvailable(COMMAND_SEEK_TO_PREVIOUS_MEDIA_ITEM)
            canSkipNext.value = window.isLive && window.isDynamic
                    || player.isCommandAvailable(COMMAND_SEEK_TO_NEXT_MEDIA_ITEM)
        } else {
            canSkipPrevious.value = false
            canSkipNext.value = false
        }
    }

    fun saveCurrentLyrics() {
        val lyricsToSave = currentLyrics.value ?: return
        database.query {
            upsert(lyricsToSave)
        }
    }

    fun togglePlayPause() {
        instance?.player?.playWhenReady =
            !instance?.player?.playWhenReady!!
    }

    fun toggleShuffle() {
        player.shuffleModeEnabled = !(player.shuffleModeEnabled)
    }

    fun toggleReplayMode() {
        player.repeatMode = if (player.repeatMode == Player.REPEAT_MODE_ONE)
            REPEAT_MODE_OFF else Player.REPEAT_MODE_ONE
    }

    fun dispose() {
        instance = null
        player.removeListener(this)
    }
    companion object {
        @Volatile
        var instance: PlayerConnection? = null
            private set
    }
    private fun sendWidgetUpdateBroadcast() {
        context.sendBroadcast(Intent(MusicWidget.ACTION_STATE_CHANGED).apply {
            setPackage(context.packageName)
        })
    }
    init {
        instance = this
        player.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                if (events.containsAny(
                        Player.EVENT_PLAYBACK_STATE_CHANGED,
                        Player.EVENT_PLAY_WHEN_READY_CHANGED,
                        Player.EVENT_MEDIA_ITEM_TRANSITION
                    )) {
                    sendStateChangedBroadcast(context)
                }
            }
        })
    }
    private fun sendStateChangedBroadcast(context: Context) {
        context.sendBroadcast(Intent(MusicWidget.Companion.ACTION_STATE_CHANGED).apply {
            setPackage(context.packageName)
        })
    }
}