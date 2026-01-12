/**
 * Babel Software 2025
 *
 * The concept and features of "Comrade" are wholly owned by Babel Software.
 * Are made available for use under an open source license.
 */

package com.babelsoftware.loudly.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.babelsoftware.loudly.MainActivity
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.db.entities.Song

object NotificationHelper {
    private const val CHANNEL_ID = "yoldas_chip_channel"
    private const val CHANNEL_NAME = "Yoldaş Bildirimleri"

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Loudly için akıllı öneriler ve bildirimler."
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Creates and sends the notification to be displayed when wired headphones are plugged in.
     * This function now takes the ‘Song’ object as a parameter and does not perform database operations.
     */
    fun showHeadsetPlugNotification(context: Context, lastPlayedSong: Song?) {
        createNotificationChannel(context)

        val title = context.getString(R.string.notification_headset_title)
        val text = if (lastPlayedSong != null) {
            context.getString(R.string.notification_headset_continue, lastPlayedSong.song.title)
        } else {
            context.getString(R.string.notification_headset_generic)
        }

        // Intent that will open the app and potentially the song when the notification is clicked
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "ACTION_PLAY_SONG"
            putExtra("SONG_ID", lastPlayedSong?.song?.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            context,
            1,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.small_icon)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Set the notification ID to a fixed value (e.g., 1001)
        notificationManager.notify(1001, builder.build())
    }

    fun showContextualNotification(
        context: Context,
        title: String,
        text: String,
        playlistId: String,
        payloadId: String
    ) {
        createNotificationChannel(context)

        // When the notification is clicked, it will open MainActivity and send the play command Intent
        val intent = Intent(context, MainActivity::class.java).apply {
            action = "ACTION_PLAY_CONTEXTUAL_PLAYLIST"
            putExtra("PLAYLIST_ID", playlistId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(context, playlistId.hashCode(), intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.small_icon)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(playlistId.hashCode(), builder.build())
    }
}