/**
 * Babel Software 2025
 *
 * The concept and features of "Comrade" are wholly owned by Babel Software.
 * Are made available for use under an open source license.
 */

package com.babelsoftware.loudly.background

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.babelsoftware.loudly.db.MusicDatabase
import com.babelsoftware.loudly.utils.NotificationHelper
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

class HeadsetPlugReceiver : BroadcastReceiver() {
    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface HeadsetPlugReceiverEntryPoint {
        fun database(): MusicDatabase
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_HEADSET_PLUG) return

        val state = intent.getIntExtra("state", -1)
        if (state == 1) {
            val hiltEntryPoint = EntryPointAccessors.fromApplication(
                context.applicationContext,
                HeadsetPlugReceiverEntryPoint::class.java
            )
            val database = hiltEntryPoint.database()

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val lastPlayedEntry = database.getLatestHistoryEntry().firstOrNull()
                    NotificationHelper.showHeadsetPlugNotification(context, lastPlayedEntry?.song)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}