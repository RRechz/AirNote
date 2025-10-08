package com.babelsoftware.airnote.presentation.screens.settings.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.BugReport
import androidx.compose.material.icons.rounded.Build
import androidx.compose.material.icons.rounded.DesktopWindows
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.constant.ConnectionConst
import com.babelsoftware.airnote.presentation.screens.settings.SettingsScaffold
import com.babelsoftware.airnote.presentation.screens.settings.model.AppUpdateViewModel
import com.babelsoftware.airnote.presentation.screens.settings.model.IconResource
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.presentation.screens.settings.model.UpdateState
import com.babelsoftware.airnote.presentation.screens.settings.widgets.ActionType
import com.babelsoftware.airnote.presentation.screens.settings.widgets.SettingsBox
import com.babelsoftware.airnote.util.ReleaseInfo
import com.babelsoftware.airnote.util.getLatestReleaseInfo


@Composable
fun AboutScreen(
    navController: NavController,
    settingsViewModel: SettingsViewModel
) {
    val uriHandler = LocalUriHandler.current
    val cornerRadius = settingsViewModel.settings.value.cornerRadius.dp

    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = stringResource(id = R.string.about),
        onBackNavClicked = { navController.navigateUp() }
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxWidth()
        ) {
            item {
                UpdateCard(settingsViewModel = settingsViewModel)
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.build_type),
                    description = settingsViewModel.build,
                    icon = IconResource.Vector(Icons.Rounded.Build),
                    actionType = ActionType.TEXT,
                    radius = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.version),
                    description = settingsViewModel.version,
                    icon = IconResource.Vector(Icons.Rounded.Info),
                    actionType = ActionType.TEXT,
                    radius = RoundedCornerShape(0.dp)
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = "AirNote AI Version",
                    description = "v1.1.4 build v0.9.0",
                    icon = IconResource.Vector(Icons.Rounded.AutoAwesome),
                    actionType = ActionType.TEXT,
                    radius = RoundedCornerShape(0.dp)
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = "Desktop UI Version",
                    description = "v0.1.0-beta",
                    icon = IconResource.Vector(Icons.Rounded.DesktopWindows),
                    actionType = ActionType.TEXT,
                    radius = RoundedCornerShape(0.dp)
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.developer),
                    description = stringResource(id = R.string.info_dev),
                    icon = IconResource.Url("https://avatars.githubusercontent.com/u/178022701?v=4"),
                    actionType = ActionType.TEXT,
                    radius = RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.email),
                    icon = IconResource.Vector(Icons.Rounded.Email),
                    clipboardText = ConnectionConst.SUPPORT_MAIL,
                    actionType = ActionType.CLIPBOARD,
                    radius = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius)
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.source_code),
                    icon = IconResource.Vector(Icons.Rounded.Download),
                    actionType = ActionType.LINK,
                    radius = RoundedCornerShape(0.dp),
                    linkClicked = { uriHandler.openUri("https://github.com/RRechz/AirNote/") }
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    size = 8.dp,
                    title = stringResource(id = R.string.feature),
                    icon = IconResource.Vector(Icons.Rounded.BugReport),
                    linkClicked = { uriHandler.openUri(ConnectionConst.FEATURE_REQUEST) },
                    actionType = ActionType.LINK,
                    radius = RoundedCornerShape(bottomStart = cornerRadius, bottomEnd = cornerRadius)
                )
            }
        }
    }
}

@Composable
fun UpdateCard(
    settingsViewModel: SettingsViewModel,
    updateViewModel: AppUpdateViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val updateState by updateViewModel.updateState.collectAsState()

    var latestReleaseInfo by remember { mutableStateOf<ReleaseInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var changelogVisible by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        if (latestReleaseInfo == null) {
            isLoading = true
            latestReleaseInfo = getLatestReleaseInfo()
            isLoading = false
        }
    }

    val installPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (context.canInstallUnknownApps()) {
            (updateState as? UpdateState.ReadyToInstall)?.let {
                installApk(context, it.apkUri)
            }
        } else {
            Toast.makeText(context, context.getString(R.string.update_card_install_permission_needed), Toast.LENGTH_LONG).show()
        }
    }

    if (isLoading) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CircularProgressIndicator()
            Text(
                text = stringResource(id = R.string.update_card_checking_for_updates),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    } else {
        val isUpdateAvailable = settingsViewModel.updateAvailable.value && latestReleaseInfo != null
        val isError = updateState is UpdateState.Failed
        val currentCornerRadius = settingsViewModel.settings.value.cornerRadius.dp

        val updateBrush = Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary))
        val successBrush = Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.tertiary.copy(alpha = 0.7f)))
        val errorBrush = Brush.linearGradient(colors = listOf(MaterialTheme.colorScheme.error, MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.8f)))

        val backgroundBrush = when {
            isError -> errorBrush
            isUpdateAvailable -> updateBrush
            else -> successBrush
        }
        val contentColor = MaterialTheme.colorScheme.onPrimary

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(currentCornerRadius))
                .background(backgroundBrush)
                .border(BorderStroke(1.dp, contentColor.copy(alpha = 0.3f)), RoundedCornerShape(currentCornerRadius))
                .animateContentSize(animationSpec = tween(500))
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        val infiniteTransition = rememberInfiniteTransition(label = "icon animation")
                        val iconOffsetY by infiniteTransition.animateFloat(
                            initialValue = 0f,
                            targetValue = if (isUpdateAvailable && !changelogVisible) -6f else 0f,
                            animationSpec = infiniteRepeatable(animation = tween(1500), repeatMode = RepeatMode.Reverse), label = ""
                        )
                        val icon = when {
                            isError -> Icons.Filled.ErrorOutline
                            isUpdateAvailable -> Icons.Filled.CloudDownload
                            else -> Icons.Filled.CheckCircle
                        }
                        Icon(
                            imageVector = icon,
                            contentDescription = "Update Status Icon",
                            modifier = Modifier
                                .size(40.dp)
                                .offset(y = iconOffsetY.dp),
                            tint = contentColor
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                        val updateStateType = remember(updateState) {
                            when (updateState) {
                                is UpdateState.Downloading -> "Downloading"
                                is UpdateState.Failed -> "Failed"
                                is UpdateState.Idle -> "Idle"
                                is UpdateState.ReadyToInstall -> "ReadyToInstall"
                            }
                        }

                        Crossfade(targetState = updateStateType, animationSpec = tween(300), label = "state crossfade") { stateType ->
                            val currentState = updateState
                            Column {
                                if (isUpdateAvailable) {
                                    val info = latestReleaseInfo!!
                                    Text(text = stringResource(id = R.string.update_card_new_version_available), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = contentColor)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = "${settingsViewModel.version} → ${settingsViewModel.latestVersion.value}", style = MaterialTheme.typography.bodyMedium, color = contentColor.copy(alpha = 0.9f), fontWeight = FontWeight.SemiBold)
                                    Spacer(modifier = Modifier.height(16.dp))

                                    when (stateType) {
                                        "Idle" -> {
                                            Column(verticalArrangement = Arrangement.spacedBy(8.dp), horizontalAlignment = Alignment.Start) {
                                                Button(
                                                    onClick = { if (info.apkDownloadUrl != null) updateViewModel.downloadAndInstallApk(info.apkDownloadUrl) else Toast.makeText(context, context.getString(R.string.update_card_download_link_not_found), Toast.LENGTH_SHORT).show() },
                                                    colors = ButtonDefaults.buttonColors(containerColor = contentColor, contentColor = MaterialTheme.colorScheme.primary)
                                                ) {
                                                    Icon(Icons.Default.ArrowDownward, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                                                    Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                                    Text(stringResource(id = R.string.update_card_download_now))
                                                }
                                                TextButton(onClick = { changelogVisible = !changelogVisible }, colors = ButtonDefaults.textButtonColors(contentColor = contentColor)) {
                                                    Text(if (changelogVisible) stringResource(R.string.hide) else stringResource(R.string.whats_new))
                                                }
                                            }
                                        }
                                        "Downloading" -> {
                                            val progress = (currentState as? UpdateState.Downloading)?.progress ?: 0
                                            Column {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(text = stringResource(id = R.string.update_card_downloading, progress), style = MaterialTheme.typography.bodyMedium, color = contentColor, modifier = Modifier.weight(1f))
                                                    IconButton(onClick = { updateViewModel.resetState() }) {
                                                        Icon(Icons.Default.Cancel, contentDescription = "Cancel Download", tint = contentColor)
                                                    }
                                                }
                                                Spacer(modifier = Modifier.height(4.dp))
                                                LinearProgressIndicator(progress = { if (progress > 0) progress / 100f else 0f }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape), color = contentColor, trackColor = contentColor.copy(alpha = 0.3f))
                                            }
                                        }
                                        "ReadyToInstall" -> {
                                            val apkUri = (currentState as? UpdateState.ReadyToInstall)?.apkUri
                                            if (apkUri != null) {
                                                Column {
                                                    Text(text = stringResource(R.string.ready_to_setup), style = MaterialTheme.typography.bodyMedium, color = contentColor.copy(alpha = 0.9f))
                                                    Spacer(modifier = Modifier.height(12.dp))
                                                    Button(
                                                        onClick = {
                                                            if (context.canInstallUnknownApps()) installApk(context, apkUri)
                                                            else installPermissionLauncher.launch(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse("package:${context.packageName}")))
                                                        },
                                                        colors = ButtonDefaults.buttonColors(containerColor = contentColor, contentColor = MaterialTheme.colorScheme.primary)
                                                    ) {
                                                        Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                                                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                                        Text(stringResource(id = R.string.update_card_install))
                                                    }
                                                }
                                            }
                                        }
                                    }
                                } else if (isError && currentState is UpdateState.Failed) {
                                    Text(text = stringResource(id = R.string.update_card_update_failed), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = contentColor)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = currentState.error, style = MaterialTheme.typography.bodyMedium, color = contentColor.copy(alpha = 0.9f))
                                    Spacer(modifier = Modifier.height(12.dp))
                                    Button(
                                        onClick = { updateViewModel.resetState() },
                                        colors = ButtonDefaults.buttonColors(containerColor = contentColor, contentColor = MaterialTheme.colorScheme.error)
                                    ) {
                                        Icon(Icons.Rounded.Refresh, contentDescription = null, modifier = Modifier.size(ButtonDefaults.IconSize))
                                        Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                        Text(stringResource(id = R.string.update_card_try_again))
                                    }
                                } else {
                                    Text(text = stringResource(id = R.string.update_card_app_up_to_date), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = contentColor)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(text = stringResource(R.string.using_latest_features), style = MaterialTheme.typography.bodyMedium, color = contentColor.copy(alpha = 0.9f))
                                }
                            }
                        }
                    }
                }

                if (changelogVisible && latestReleaseInfo != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    ChangelogContent(releaseInfo = latestReleaseInfo!!, color = contentColor)
                }
            }
        }
    }
}

@Composable
private fun ChangelogContent(releaseInfo: ReleaseInfo, color: Color) {
    Column {
        Text(text = stringResource(R.string.whats_new_this_version), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Spacer(modifier = Modifier.height(8.dp))
        releaseInfo.changelog?.split("\n")?.forEach { line ->
            if (line.trim().startsWith("*") || line.trim().startsWith("-")) {
                Row(modifier = Modifier.padding(bottom = 4.dp)) {
                    Text("• ", color = color.copy(alpha = 0.9f), style = MaterialTheme.typography.bodyMedium)
                    Text(text = line.trim().removePrefix("*").removePrefix("-").trim(), style = MaterialTheme.typography.bodyMedium, color = color.copy(alpha = 0.9f), lineHeight = 20.sp)
                }
            }
        }
    }
}


private fun installApk(context: Context, uri: Uri) {
    val intent = Intent(Intent.ACTION_VIEW).apply {
        setDataAndType(uri, "application/vnd.android.package-archive")
        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(intent)
}

private fun Context.canInstallUnknownApps(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        packageManager.canRequestPackageInstalls()
    } else {
        true
    }
}