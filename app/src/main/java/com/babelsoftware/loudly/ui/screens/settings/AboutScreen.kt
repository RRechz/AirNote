package com.babelsoftware.loudly.ui.screens.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.InstallMobile
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.babelsoftware.loudly.BuildConfig
import com.babelsoftware.loudly.R
import com.babelsoftware.loudly.ui.component.ErrorDetailDialog
import com.babelsoftware.loudly.ui.screens.settings.card_design.ActionType
import com.babelsoftware.loudly.ui.screens.settings.card_design.IconResource
import com.babelsoftware.loudly.ui.screens.settings.card_design.SettingsBox
import com.babelsoftware.loudly.ui.screens.settings.viewmodel.AppUpdateViewModel
import com.babelsoftware.loudly.ui.screens.settings.viewmodel.UpdateState
import com.babelsoftware.loudly.utils.ReleaseInfo
import com.babelsoftware.loudly.utils.getLatestReleaseInfo
import com.babelsoftware.loudly.utils.isNewerVersion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior
) {
    val uriHandler = LocalUriHandler.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.about)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(painterResource(R.drawable.arrow_back), null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                UpdateCard()
            }

            item {
                SettingsBox(
                    title = stringResource(id = R.string.app_version),
                    description = BuildConfig.VERSION_NAME,
                    icon = IconResource.Drawable(painterResource(id = R.drawable.tag)),
                    actionType = ActionType.TEXT,
                    onClick = {}
                )
            }
            item {
                SettingsBox(
                    title = stringResource(R.string.app_type),
                    description = BuildConfig.FLAVOR.uppercase(),
                    icon = IconResource.Vector(Icons.Default.Build),
                    actionType = ActionType.TEXT,
                    onClick = {}
                )
            }
            item {
                SettingsBox(
                    title = stringResource(R.string.Developer),
                    description = "Babel Software founder & Android developer",
                    icon = IconResource.Drawable(painterResource(id = R.drawable.person)),
                    onClick = { uriHandler.openUri("https://github.com/RRechz") }
                )
            }
            item {
                SettingsBox(
                    title = stringResource(R.string.code),
                    description = stringResource(R.string.code_text),
                    icon = IconResource.Drawable(painterResource(id = R.drawable.codigo)),
                    onClick = { uriHandler.openUri("https://github.com/RRechz/Loudly") }
                )
            }
            item {
                SettingsBox(
                    title = stringResource(R.string.bugs),
                    description = stringResource(R.string.bugs_text),
                    icon = IconResource.Drawable(painterResource(id = R.drawable.bug_report)),
                    onClick = { uriHandler.openUri("t.me/by_BabelSoftware") }
                )
            }
        }
    }
}

@Composable
fun UpdateCard(
    updateViewModel: AppUpdateViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val updateState by updateViewModel.updateState.collectAsState()

    var latestReleaseInfo by remember { mutableStateOf<ReleaseInfo?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var updateAvailable by remember { mutableStateOf(false) }
    var errorToShow by remember { mutableStateOf<String?>(null) }

    errorToShow?.let { error ->
        ErrorDetailDialog(
            errorTitle = stringResource(R.string.update_error_details),
            errorMessage = error,
            onDismiss = { errorToShow = null }
        )
    }

    LaunchedEffect(key1 = true) {
        isLoading = true
        val info = getLatestReleaseInfo()
        latestReleaseInfo = info
        if (info != null) {
            updateAvailable = isNewerVersion(info.tagName, BuildConfig.VERSION_NAME)
        }
        isLoading = false
    }

    val installPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) {
        if (context.canInstallUnknownApps()) {
            (updateState as? UpdateState.ReadyToInstall)?.let {
                installApk(context, it.apkUri)
            }
        } else {
            Toast.makeText(context, "Please authorize the installation.", Toast.LENGTH_LONG).show()
        }
    }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        shape = RoundedCornerShape(24.dp),
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(vertical = 8.dp))
                Text(text = stringResource(R.string.checking_for_update), style = MaterialTheme.typography.bodyMedium)
            } else if (updateAvailable && latestReleaseInfo != null) {
                UpdateAvailableContent(
                    info = latestReleaseInfo!!,
                    state = updateState,
                    updateViewModel = updateViewModel,
                    context = context,
                    installPermissionLauncher = installPermissionLauncher,
                    onShowError = { error ->
                        errorToShow = error
                    }
                )
            } else {
                UpToDateContent()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UpdateAvailableContent(
    info: ReleaseInfo,
    state: UpdateState,
    updateViewModel: AppUpdateViewModel,
    context: Context,
    installPermissionLauncher: androidx.activity.result.ActivityResultLauncher<Intent>,
    onShowError: (String) -> Unit
) {
    val uriHandler = LocalUriHandler.current

    Icon(
        imageVector = if (state is UpdateState.Failed) Icons.Filled.Error else Icons.Filled.CloudDownload,
        contentDescription = stringResource(R.string.new_version_available),
        modifier = Modifier.size(48.dp),
        tint = if (state is UpdateState.Failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    )

    Text(
        text = if (state is UpdateState.Failed) stringResource(R.string.download_failed) else stringResource(R.string.new_version_available),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold
    )
    Text(
        text = stringResource(R.string.update_card_version_ready_to_download, info.tagName),
        style = MaterialTheme.typography.bodyLarge,
        textAlign = TextAlign.Center
    )

    when (state) {
        is UpdateState.Idle -> {
            Button(onClick = { info.apkDownloadUrl?.let { updateViewModel.downloadAndInstallApk(it) } }) {
                Icon(Icons.Filled.Download, contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.download_now))
            }
        }
        is UpdateState.Downloading -> {
            Text(stringResource(R.string.update_card_downloading), style = MaterialTheme.typography.bodyMedium)
            LinearProgressIndicator(progress = { state.progress / 100f }, modifier = Modifier.fillMaxWidth())
        }
        is UpdateState.ReadyToInstall -> {
            Button(onClick = {
                if (context.canInstallUnknownApps()) {
                    installApk(context, state.apkUri)
                } else {
                    val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                        .setData(Uri.parse("package:${context.packageName}"))
                    installPermissionLauncher.launch(intent)
                }
            }) {
                Icon(Icons.Filled.InstallMobile, contentDescription = null)
                Spacer(Modifier.size(ButtonDefaults.IconSpacing))
                Text(stringResource(R.string.install))
            }
        }
        is UpdateState.Failed -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                AssistChip(
                    onClick = { onShowError(state.error) },
                    label = {
                        Text(
                            text = stringResource(R.string.show_error),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.basicMarquee()
                        )
                    },
                    leadingIcon = { Icon(Icons.Filled.Error, null, modifier = Modifier.size(18.dp)) }
                )
                AssistChip(
                    onClick = { info.apkDownloadUrl?.let { updateViewModel.downloadAndInstallApk(it) } },
                    label = {
                        Text(
                            text = stringResource(R.string.try_again),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.basicMarquee()
                        )
                    },
                    leadingIcon = { Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(18.dp)) }
                )
                AssistChip(
                    onClick = { uriHandler.openUri("https://github.com/RRechz/Loudly/releases/latest") },
                    label = {
                        Text(
                            text = stringResource(R.string.go_to_update_link),
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.basicMarquee()
                        )
                    },
                    leadingIcon = { Icon(Icons.Filled.Link, null, modifier = Modifier.size(18.dp)) }
                )
            }
        }
    }
}

@Composable
private fun UpToDateContent() {
    Icon(
        imageVector = Icons.Filled.CheckCircle,
        contentDescription = "App up to date",
        modifier = Modifier.size(24.dp),
        tint = MaterialTheme.colorScheme.secondary
    )
    Text(
        text = stringResource(R.string.app_up_to_date),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold
    )
}

// Helper fun
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