package com.babelsoftware.airnote.presentation.screens.settings.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.InstallMobile
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
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.constant.ConnectionConst
import com.babelsoftware.airnote.presentation.components.UpdateScreen
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
    val context = LocalContext.current
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
                    radius = shapeManager(
                        isFirst = true,
                        radius = settingsViewModel.settings.value.cornerRadius
                    )
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.version),
                    description = settingsViewModel.version,
                    icon = IconResource.Vector(Icons.Rounded.Info),
                    actionType = ActionType.TEXT,
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius
                    ),
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = "AirNote AI Version",
                    description = "v1.1.4 build v0.9.0",
                    icon = IconResource.Vector(Icons.Rounded.AutoAwesome),
                    actionType = ActionType.TEXT,
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius
                    ),
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = "Desktop UI Version",
                    description = "v0.1.0-beta",
                    icon = IconResource.Vector(Icons.Rounded.DesktopWindows),
                    actionType = ActionType.TEXT,
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius
                    ),
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.developer),
                    description = stringResource(id = R.string.info_dev),
                    icon = IconResource.Url("https://avatars.githubusercontent.com/u/178022701?v=4"),
                    actionType = ActionType.TEXT,
                    radius = shapeManager(
                        isLast = true,
                        radius = settingsViewModel.settings.value.cornerRadius
                    ),
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
                    radius = shapeManager(
                        isFirst = true,
                        radius = settingsViewModel.settings.value.cornerRadius
                    ),
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.source_code),
                    icon = IconResource.Vector(Icons.Rounded.Download),
                    actionType = ActionType.LINK,
                    radius = shapeManager(
                        radius = settingsViewModel.settings.value.cornerRadius
                    ),
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
                    radius = shapeManager(
                        isLast = true,
                        radius = settingsViewModel.settings.value.cornerRadius
                    ),
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
    var showChangelog by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        if (latestReleaseInfo == null) {
            isLoading = true
            latestReleaseInfo = getLatestReleaseInfo()
            isLoading = false
        }
    }

    if (showChangelog) {
        val versionForChangelog = if (settingsViewModel.updateAvailable.value) {
            settingsViewModel.latestVersion.value
        } else {
            settingsViewModel.version
        }
        UpdateScreen(
            latestVersion = versionForChangelog,
            onDismiss = { showChangelog = false },
            onNavigateToAbout = { /* Zaten bu ekranda olduğumuz için boş bırakıldı */ }
        )
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
    val currentCornerRadius = settingsViewModel.settings.value.cornerRadius

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(currentCornerRadius.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 8.dp))
                Text(
                    text = stringResource(id = R.string.update_card_checking_for_updates),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            } else if (settingsViewModel.updateAvailable.value && latestReleaseInfo != null) {
                val info = latestReleaseInfo!!

                Icon(
                    imageVector = Icons.Filled.CloudDownload,
                    contentDescription = stringResource(id = R.string.update_card_update_available_icon_description),
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = stringResource(id = R.string.update_card_new_version_available),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = stringResource(id = R.string.update_card_version_ready_to_download, settingsViewModel.latestVersion.value),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                when (val state = updateState) {
                    is UpdateState.Idle -> {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = {
                                    if (info.apkDownloadUrl != null) {
                                        updateViewModel.downloadAndInstallApk(info.apkDownloadUrl)
                                    } else {
                                        Toast.makeText(context, context.getString(R.string.update_card_download_link_not_found), Toast.LENGTH_SHORT).show()
                                    }
                                },
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 5.dp)
                            ) {
                                Icon(
                                    Icons.Filled.ArrowDownward,
                                    contentDescription = stringResource(id = R.string.update_card_download_icon_description),
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                                Text(stringResource(id = R.string.update_card_download_now))
                            }
                            OutlinedButton(onClick = { showChangelog = true }) {
                                Text(stringResource(R.string.about_update_news))
                            }
                        }
                    }
                    is UpdateState.Downloading -> {
                        Text(
                            text = stringResource(id = R.string.update_card_downloading, state.progress),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        LinearProgressIndicator(
                            progress = { if (state.progress > 0) state.progress / 100f else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .padding(vertical = 4.dp)
                        )
                    }
                    is UpdateState.ReadyToInstall -> {
                        Button(
                            onClick = {
                                if (context.canInstallUnknownApps()) {
                                    installApk(context, state.apkUri)
                                } else {
                                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                                        .setData(Uri.parse("package:${context.packageName}"))
                                    installPermissionLauncher.launch(intent)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                        ) {
                            Icon(
                                Icons.Filled.InstallMobile,
                                contentDescription = stringResource(id = R.string.update_card_install_icon_description),
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(stringResource(id = R.string.update_card_install))
                        }
                    }
                    is UpdateState.Failed -> {
                        Icon(
                            imageVector = Icons.Filled.ErrorOutline,
                            contentDescription = stringResource(id = R.string.update_card_error_icon_description),
                            modifier = Modifier.size(36.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                        Text(
                            text = stringResource(id = R.string.update_card_update_failed),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = state.error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = { updateViewModel.resetState() }
                        ) {
                            Icon(
                                Icons.Rounded.Refresh,
                                contentDescription = stringResource(id = R.string.update_card_try_again_icon_description),
                                modifier = Modifier.size(ButtonDefaults.IconSize)
                            )
                            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                            Text(stringResource(id = R.string.update_card_try_again))
                        }
                    }
                }

            } else { // App current status
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = stringResource(id = R.string.update_card_up_to_date_icon_description),
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.secondary
                )
                Text(
                    text = stringResource(id = R.string.update_card_app_up_to_date),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = {
                    if (latestReleaseInfo != null) {
                        showChangelog = true
                    } else {
                        Toast.makeText(context, "Değişiklik günlüğü bilgisi alınamadı.", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text(stringResource(R.string.about_latest_changes))
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