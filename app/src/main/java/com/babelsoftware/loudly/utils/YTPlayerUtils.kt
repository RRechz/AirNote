package com.babelsoftware.loudly.utils

import android.net.ConnectivityManager
import androidx.media3.common.PlaybackException
import com.babelsoftware.loudly.constants.AudioQuality
import com.babelsoftware.innertube.YouTube
import com.babelsoftware.innertube.models.YouTubeClient
import com.babelsoftware.innertube.models.YouTubeClient.Companion.IOS
import com.babelsoftware.innertube.models.YouTubeClient.Companion.TVHTML5_SIMPLY_EMBEDDED_PLAYER
import com.babelsoftware.innertube.models.YouTubeClient.Companion.WEB_REMIX
import com.babelsoftware.innertube.models.response.PlayerResponse
import com.babelsoftware.innertube.pages.NewPipeUtils
import kotlinx.coroutines.delay
import okhttp3.OkHttpClient
import okhttp3.Request

object YTPlayerUtils {
    private val httpClient = OkHttpClient.Builder()
        .proxy(YouTube.proxy)
        .build()

    private val MAIN_CLIENT: YouTubeClient = WEB_REMIX

    private val STREAM_FALLBACK_CLIENTS: Array<YouTubeClient> = arrayOf(
        TVHTML5_SIMPLY_EMBEDDED_PLAYER,
        IOS,
        YouTubeClient.WEB,
        YouTubeClient.MOBILE,
        YouTubeClient.ANDROID_MUSIC
    )

    data class PlaybackData(
        val audioConfig: PlayerResponse.PlayerConfig.AudioConfig?,
        val videoDetails: PlayerResponse.VideoDetails?,
        val playbackTracking: PlayerResponse.PlaybackTracking?,
        val format: PlayerResponse.StreamingData.Format,
        val streamUrl: String,
        val streamExpiresInSeconds: Int,
    )

    suspend fun playerResponseForPlayback(
        videoId: String,
        playlistId: String? = null,
        audioQuality: AudioQuality,
        connectivityManager: ConnectivityManager,
    ): Result<PlaybackData> = runCatching {
        val signatureTimestamp = NewPipeUtils.getSignatureTimestamp(videoId).getOrNull()
        val isLoggedIn = YouTube.cookie != null

        var audioConfig: PlayerResponse.PlayerConfig.AudioConfig? = null
        var videoDetails: PlayerResponse.VideoDetails? = null
        var playbackTracking: PlayerResponse.PlaybackTracking? = null
        var format: PlayerResponse.StreamingData.Format? = null
        var streamUrl: String? = null
        var streamExpiresInSeconds: Int? = null
        val allClients = (listOf(MAIN_CLIENT) + STREAM_FALLBACK_CLIENTS).shuffled()
        var lastError: Throwable? = null

        for (client in allClients) {
            if (client.loginRequired && !isLoggedIn && YouTube.cookie == null) continue

            try {
                val streamPlayerResponse = YouTube.player(videoId, playlistId, client, signatureTimestamp).getOrThrow()

                if (streamPlayerResponse.playabilityStatus?.status != "OK") {
                    lastError = IllegalStateException("Playability status is: ${streamPlayerResponse.playabilityStatus?.status}")
                    continue
                }

                if (videoDetails == null) {
                    audioConfig = streamPlayerResponse.playerConfig?.audioConfig
                    videoDetails = streamPlayerResponse.videoDetails
                    playbackTracking = streamPlayerResponse.playbackTracking
                }

                val audioFormats = streamPlayerResponse.streamingData?.adaptiveFormats?.filter { it.isAudio } ?: continue
                val bestFormat = when (audioQuality) {
                    // Low Quality (LOW) Setting: Bitrate is kept between 45 - 52 Kbps.
                    AudioQuality.LOW -> {
                        audioFormats
                            .filter { it.bitrate in 45_000..52_000 }
                            .maxByOrNull { it.bitrate }
                    }

                    // High Quality (HIGH) Setting: Bitrate is kept between 128 - 256 Kbps.
                    AudioQuality.HIGH -> {
                        audioFormats
                            .filter { it.bitrate in 128_000..256_000 }
                            .maxByOrNull { it.bitrate }
                    }

                    // Maximum Quality (MAX) Setting: Smart selection up to 512 Kbps.
                    AudioQuality.MAX -> {
                        audioFormats
                            .filter { it.bitrate <= 512_000 }
                            // Select the one with the highest bitrate; if equal, give priority to WEBM/OPUS.
                            .maxByOrNull { it.bitrate + if (it.mimeType.startsWith("audio/webm")) 1 else 0 }
                    }

                    // Automatic Quality (AUTO) Setting: Smart selection based on network conditions.
                    AudioQuality.AUTO -> {
                        if (connectivityManager.isActiveNetworkMetered) {
                            // Mobile network: 45 - 128 Kbps range is targeted.
                            audioFormats
                                .filter { it.bitrate in 45_000..128_000 }
                                .maxByOrNull { it.bitrate }
                        } else {
                            // Wi-Fi: A range of 45 - 512 kbps is targeted.
                            audioFormats
                                .filter { it.bitrate in 45_000..512_000 }
                                .maxByOrNull { it.bitrate + if (it.mimeType.startsWith("audio/webm")) 1 else 0 }
                        }
                    }
                } ?: continue // If no suitable format is found, try the next client
                val currentStreamUrl = NewPipeUtils.getStreamUrl(bestFormat, videoId).getOrNull()

                if (currentStreamUrl != null && validateStatus(currentStreamUrl)) {
                    streamUrl = currentStreamUrl
                    format = bestFormat
                    streamExpiresInSeconds = streamPlayerResponse.streamingData?.expiresInSeconds ?: 3600
                    break
                }

            } catch (e: Exception) {
                lastError = e
                delay(500)
            }
        }

        if (streamUrl == null || format == null) {
            throw PlaybackException(
                "Sorry, buddy, despite all attempts, a valid publication URL could not be obtained...",
                lastError,
                PlaybackException.ERROR_CODE_IO_NETWORK_CONNECTION_FAILED // Bu hata kodu daha anlamlı
            )
        }

        PlaybackData(
            audioConfig,
            videoDetails,
            playbackTracking,
            format,
            streamUrl,
            streamExpiresInSeconds!!
        )
    }

    suspend fun playerResponseForMetadata(
        videoId: String,
        playlistId: String? = null
    ): Result<PlayerResponse> =
        YouTube.player(videoId, playlistId, client = WEB_REMIX)

    private fun validateStatus(url: String): Boolean {
        return try {
            val request = Request.Builder()
                .head()
                .url(url)
                .build()
            val response = httpClient.newCall(request).execute()
            response.use { it.isSuccessful }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}