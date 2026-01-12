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
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.android.gms.location.ActivityTransitionResult

class ActivityTransitionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            result?.transitionEvents?.forEach { event ->
                val workRequest = OneTimeWorkRequestBuilder<ContextualWorker>()
                    .setInputData(workDataOf("ACTIVITY_TYPE" to event.activityType))
                    .build()
                WorkManager.getInstance(context).enqueue(workRequest)
            }
        }
    }
}