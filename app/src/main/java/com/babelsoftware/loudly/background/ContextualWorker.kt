/**
 * Babel Software 2025
 *
 * The concept and features of "Comrade" are wholly owned by Babel Software.
 * Are made available for use under an open source license.
 */

package com.babelsoftware.loudly.background

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.db.MusicDatabase
import com.babelsoftware.loudly.utils.NotificationHelper
import com.google.android.gms.location.DetectedActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.firstOrNull

@HiltWorker
class ContextualWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted workerParams: WorkerParameters,
    private val database: MusicDatabase
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val LIKED_SONGS_FALLBACK_ID = "LIKED_SONGS_SHUFFLE_FALLBACK"
    }

    override suspend fun doWork(): Result {
        val activityType = inputData.getInt("ACTIVITY_TYPE", -1)

        when (activityType) {
            DetectedActivity.IN_VEHICLE -> {
                val title = context.getString(R.string.notification_vehicle_title)
                val text = context.getString(R.string.notification_vehicle_text)
                val playlist = database.findPlaylistByName("%Yol%").firstOrNull()
                    ?: database.findPlaylistByName("%Drive%").firstOrNull()

                val payloadId = playlist?.id ?: LIKED_SONGS_FALLBACK_ID
                NotificationHelper.showContextualNotification(
                    context, title, text, "ACTION_PLAY_CONTEXTUAL_PLAYLIST", payloadId
                )
            }
            DetectedActivity.RUNNING -> {
                val title = context.getString(R.string.notification_running_title)
                val text = context.getString(R.string.notification_running_text)
                val playlist = database.findPlaylistByName("%Koşu%").firstOrNull()
                    ?: database.findPlaylistByName("%Run%").firstOrNull()

                val payloadId = playlist?.id ?: LIKED_SONGS_FALLBACK_ID
                NotificationHelper.showContextualNotification(
                    context, title, text, "ACTION_PLAY_CONTEXTUAL_PLAYLIST", payloadId
                )
            }
        }

        return Result.success()
    }
}