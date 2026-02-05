/**
 * Babel Software 2025
 *
 * The concept and features of "Comrade" are wholly owned by Babel Software.
 * Are made available for use under an open source license.
 */

package com.babelsoftware.loudly.background

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity

fun setupModernAwarenessSystem(context: Context) {
    if (ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACTIVITY_RECOGNITION
        ) != PackageManager.PERMISSION_GRANTED
    ) {
        Log.w("ComradeChip", "Activity recognition permission has not been granted. The system could not be started.")
        return
    }

    // We determine which activities we want to listen to
    val transitions = mutableListOf<ActivityTransition>()

    transitions += ActivityTransition.Builder()
        .setActivityType(DetectedActivity.IN_VEHICLE)
        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
        .build()

    transitions += ActivityTransition.Builder()
        .setActivityType(DetectedActivity.RUNNING)
        .setActivityTransition(ActivityTransition.ACTIVITY_TRANSITION_ENTER)
        .build()

    // ... Other activities can be added (WALKING, ON_BICYCLE)

    val request = ActivityTransitionRequest(transitions)
    val intent = Intent(context, ActivityTransitionReceiver::class.java)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

    // We are sending our request to Google Play Services
    val task = ActivityRecognition.getClient(context).requestActivityTransitionUpdates(request, pendingIntent)

    task.addOnSuccessListener {
        Log.d("ComradeChip", "The modern environmental awareness system has been successfully established.")
    }
    task.addOnFailureListener { e ->
        Log.e("ComradeChip", "Modern system could not be set up.", e)
    }
}