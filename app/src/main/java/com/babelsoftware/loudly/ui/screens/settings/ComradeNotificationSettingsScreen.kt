/**
 * Babel Software 2025
 *
 * The concept and features of "Comrade" are wholly owned by Babel Software.
 * Are made available for use under an open source license.
 */

package com.babelsoftware.loudly.ui.screens.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.constants.ComradeActivityDetectionEnabledKey
import com.babelsoftware.loudly.constants.ComradeNotificationsEnabledKey
import com.babelsoftware.loudly.constants.ComradeVehicleDetectionEnabledKey
import com.babelsoftware.loudly.ui.screens.settings.card_design.ActionType
import com.babelsoftware.loudly.ui.screens.settings.card_design.IconResource
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingCategory
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingsBox
import com.babelsoftware.loudly.ui.screens.settings.card_design.shapeManager
import com.babelsoftware.loudly.utils.NotificationHelper
import com.babelsoftware.loudly.utils.rememberPreference
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ComradeNotificationSettingsScreen(navController: NavController) {
    val context = LocalContext.current
    var masterSwitch by rememberPreference(ComradeNotificationsEnabledKey, false)
    var vehicleSwitch by rememberPreference(ComradeVehicleDetectionEnabledKey, true)
    var activitySwitch by rememberPreference(ComradeActivityDetectionEnabledKey, true)

    var permissionGranted by remember { mutableStateOf(false) }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        permissionGranted = isGranted
    }

    val checkNotificationPermission = {
        permissionGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    // We manage Activity Recognition permission for Android 10 (Q) and above
    val activityPermissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        rememberPermissionState(permission = Manifest.permission.ACTIVITY_RECOGNITION)
    } else {
        null // This permission is not required in older versions.
    }

    LaunchedEffect(Unit) {
        checkNotificationPermission()
    }

    LaunchedEffect(permissionGranted) {}

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.comrade_notifications_title)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(painterResource(id = R.drawable.arrow_back), contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsBox(
                    title = stringResource(R.string.comrade_notifications_enable),
                    description = stringResource(R.string.comrade_notifications_enable_desc),
                    icon = IconResource.Drawable(painterResource(id = R.drawable.ic_smart_hub)),
                    actionType = ActionType.SWITCH,
                    isChecked = masterSwitch,
                    onCheckedChange = { masterSwitch = it },
                    shape = shapeManager(isBoth = true)
                )
            }

            if (masterSwitch && activityPermissionState != null) {
                item {
                    SettingCategory(title = stringResource(R.string.permissions_category))
                    SettingsBox(
                        title = stringResource(R.string.comrade_permission_activity_title),
                        description = stringResource(R.string.comrade_permission_activity_desc),
                        icon = IconResource.Drawable(painterResource(id = R.drawable.directions_run)),
                        actionType = if (activityPermissionState.status.isGranted) ActionType.TEXT else ActionType.NAVIGATION,
                        actionText = if (activityPermissionState.status.isGranted) stringResource(R.string.permission_button_granted) else "",
                        onClick = if (activityPermissionState.status.isGranted) null else { { activityPermissionState.launchPermissionRequest() } },
                        shape = shapeManager(isFirst = true)
                    )
                }
                item {
                    SettingsBox(
                        title = stringResource(R.string.enable_notifications),
                        icon = IconResource.Drawable(painterResource(id = if (permissionGranted) R.drawable.notification_on else R.drawable.notifications_off)),
                        actionType = ActionType.SWITCH,
                        isChecked = permissionGranted,
                        onCheckedChange = { checked ->
                            if (checked && !permissionGranted) {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                }
                            } else if (!checked) {
                                checkNotificationPermission()
                            }
                        },
                        shape = shapeManager(isLast = true)
                    )
                }
            }

            if (masterSwitch && (activityPermissionState == null || activityPermissionState.status.isGranted)) {
                item {
                    SettingCategory(title = stringResource(R.string.comrade_recommendation_types))
                    SettingsBox(
                        shape = shapeManager(isFirst = true)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = stringResource(R.string.comrade_driving_detection), style = MaterialTheme.typography.bodyLarge)
                                    Text(text = stringResource(R.string.comrade_driving_detection_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = vehicleSwitch, onCheckedChange = { vehicleSwitch = it })
                            }
                            OutlinedButton(
                                onClick = {
                                    NotificationHelper.showContextualNotification(
                                        context = context,
                                        title = context.getString(R.string.notification_vehicle_title),
                                        text = context.getString(R.string.notification_vehicle_text),
                                        playlistId = "DRIVING_PLAYLIST_ID_DEMO",
                                        payloadId = "PAYLOAD"
                                    )
                                },
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                            ) {
                                Text(stringResource(R.string.demo_button_text))
                            }
                        }
                    }
                    SettingsBox(
                        shape = shapeManager(isLast = true)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = stringResource(R.string.comrade_activity_detection), style = MaterialTheme.typography.bodyLarge)
                                    Text(text = stringResource(R.string.comrade_activity_detection_desc), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(checked = activitySwitch, onCheckedChange = { activitySwitch = it })
                            }
                            OutlinedButton(
                                onClick = {
                                    NotificationHelper.showContextualNotification(
                                        context = context,
                                        title = context.getString(R.string.notification_running_title),
                                        text = context.getString(R.string.notification_running_text),
                                        playlistId = "DRIVING_PLAYLIST_ID_DEMO",
                                        payloadId = "PAYLOAD"
                                    )
                                },
                                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 8.dp)
                            ) {
                                Text(stringResource(R.string.demo_button_text))
                            }
                        }
                    }
                }
            }
        }
    }
}