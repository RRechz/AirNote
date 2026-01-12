package com.babelsoftware.loudly

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import androidx.core.graphics.drawable.toBitmap
import coil.ImageLoader
import coil.request.ImageRequest
import com.babelsoftware.loudly.playback.PlayerConnection
import com.babelsoftware.loudly.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MusicWidget : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        manageProgressUpdater(context)
    }

    override fun onDisabled(context: Context) {
        stopProgressUpdater()
    }

    // ---> This function is triggered when widget buttons are pressed or a status change is reported.
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        val player = PlayerConnection.instance

        when (intent.action) {
            ACTION_PLAY_PAUSE -> player?.togglePlayPause()
            ACTION_PREV -> player?.seekToPrevious()
            ACTION_NEXT -> player?.seekToNext()
            ACTION_STATE_CHANGED -> { /* Only updates are triggered */ }
        }
        updateAllWidgets(context)
        manageProgressUpdater(context)
    }
    // <---

    companion object {
        private const val ACTION_PLAY_PAUSE = "com.babelsoftware.loudly.ACTION_PLAY_PAUSE"
        private const val ACTION_PREV = "com.babelsoftware.loudly.ACTION_PREV"
        private const val ACTION_NEXT = "com.babelsoftware.loudly.ACTION_NEXT"
        const val ACTION_STATE_CHANGED = "com.babelsoftware.loudly.ACTION_STATE_CHANGED"

        private val handler = Handler(Looper.getMainLooper())
        private var progressUpdater: Runnable? = null

        // ---> Updates all music widgets on the screen.
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, MusicWidget::class.java)
            appWidgetManager.getAppWidgetIds(componentName)?.forEach { appWidgetId ->
                updateWidget(context, appWidgetManager, appWidgetId)
            }
        }
        // <---

        // ---> The main function that updates a specific widget.
        private fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_music)
            val player = PlayerConnection.instance?.player

            if (player == null || player.currentMediaItem == null) {
                views.setTextViewText(R.id.widget_track_title, "Music Not Playing")
                views.setTextViewText(R.id.widget_artist, "")
                views.setImageViewResource(R.id.widget_play_pause, R.drawable.play)
                views.setImageViewResource(R.id.widget_album_art, R.drawable.widget_album_art_background)
                appWidgetManager.updateAppWidget(appWidgetId, views)
                return
            }

            // 1. Set up information such as text, buttons, and progress that will be updated instantly.
            views.setTextViewText(R.id.widget_track_title, player.mediaMetadata.title)
            views.setTextViewText(R.id.widget_artist, player.mediaMetadata.artist)

            val playPauseIcon = if (player.isPlaying) R.drawable.pause else R.drawable.play
            views.setImageViewResource(R.id.widget_play_pause, playPauseIcon)

            val progress = if (player.duration > 0) (player.currentPosition * 100 / player.duration).toInt() else 0
            views.setProgressBar(R.id.widget_progress, 100, progress, false)

            // 2. Enable the rounded corners feature
            views.setBoolean(R.id.widget_album_art, "setClipToOutline", true)

            // 3. Upload the album cover
            val imageUrl = player.mediaMetadata.artworkUri
            if (imageUrl != null) {
                CoroutineScope(Dispatchers.IO).launch {
                    val request = ImageRequest.Builder(context).data(imageUrl).build()
                    val bitmap = try { ImageLoader(context).execute(request).drawable?.toBitmap() } catch (e: Exception) { null }

                    withContext(Dispatchers.Main) {
                        if (bitmap != null) {
                            views.setImageViewBitmap(R.id.widget_album_art, bitmap)
                        } else {
                            views.setImageViewResource(R.id.widget_album_art, R.drawable.widget_album_art_background)
                        }
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                }
            } else {
                views.setImageViewResource(R.id.widget_album_art, R.drawable.widget_album_art_background)
            }

            // 4. Set up click events (PendingIntent)
            views.setOnClickPendingIntent(R.id.widget_play_pause, getBroadcastPendingIntent(context, ACTION_PLAY_PAUSE))
            views.setOnClickPendingIntent(R.id.widget_prev, getBroadcastPendingIntent(context, ACTION_PREV))
            views.setOnClickPendingIntent(R.id.widget_next, getBroadcastPendingIntent(context, ACTION_NEXT))

            val openAppIntent = Intent(context, MainActivity::class.java)
            val openAppPendingIntent = PendingIntent.getActivity(context, 0, openAppIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            views.setOnClickPendingIntent(R.id.widget_container, openAppPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views) // 5. UPDATE the widget
        }
        // <---

        private fun getBroadcastPendingIntent(context: Context, action: String): PendingIntent {
            val intent = Intent(context, MusicWidget::class.java).apply { this.action = action }
            return PendingIntent.getBroadcast(
                context, action.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        }

        // ---> Manages the mechanism that updates the progress bar only when music is playing.
        private fun manageProgressUpdater(context: Context) {
            stopProgressUpdater()
            if (PlayerConnection.instance?.player?.isPlaying == true) {
                progressUpdater = Runnable {
                    updateAllWidgets(context)
                    if (PlayerConnection.instance?.player?.isPlaying == true) {
                        handler.postDelayed(progressUpdater!!, 1000)
                    }
                }.also {
                    handler.post(it)
                }
            }
        }

        private fun stopProgressUpdater() {
            progressUpdater?.let { handler.removeCallbacks(it) }
            progressUpdater = null
        }
        // <---
    }
}