package com.babelsoftware.airnote.presentation.screens.settings.settings

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavController
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.data.repository.GeminiModels
import com.babelsoftware.airnote.presentation.screens.settings.SettingsScaffold
import com.babelsoftware.airnote.presentation.screens.settings.model.IconResource
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.presentation.screens.settings.widgets.ActionType
import com.babelsoftware.airnote.presentation.screens.settings.widgets.SettingsBox
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirNoteAiSettingsScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    var showBottomSheet by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = true) {
        settingsViewModel.uiEvent.collect { message ->
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    if (showBottomSheet) {
        ModalBottomSheet(onDismissRequest = { showBottomSheet = false }, sheetState = sheetState) {
            ApiKeyGuide()
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = stringResource(R.string.airnote_ai),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        onBackNavClicked = { navController.navigateUp() }
    ) {
        LazyColumn(modifier = Modifier.padding(top = 16.dp, start = 12.dp, end = 12.dp)) {
            item {
                ApiKeySetting(settingsViewModel = settingsViewModel, onHelpClick = { showBottomSheet = true })
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                ModelChoiceSetting(settingsViewModel = settingsViewModel)
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                OfflineTranslationSettings(settingsViewModel = settingsViewModel)
            }
        }
    }
}

@Composable
private fun ApiKeySetting(settingsViewModel: SettingsViewModel, onHelpClick: () -> Unit) {
    val userApiKey by settingsViewModel.userApiKey

    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = stringResource(R.string.your_api_key),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 4.dp)
            )
            IconButton(onClick = onHelpClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                    contentDescription = stringResource(R.string.api_key_guide_title)
                )
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        SettingsBox(
            settingsViewModel = settingsViewModel,
            title = stringResource(id = R.string.gemini_api_key),
            description = if (userApiKey.isNotBlank()) "••••••••••••••••••••" else stringResource(R.string.not_set),
            icon = IconResource.Vector(Icons.Rounded.Key),
            actionType = ActionType.CUSTOM,
            radius = shapeManager(
                isBoth = true,
                radius = settingsViewModel.settings.value.cornerRadius
            ),
            customAction = { onDismiss ->
                ApiKeyPopup(
                    settingsViewModel = settingsViewModel,
                    onDismiss = onDismiss
                )
            }
        )
    }
}

@Composable
private fun ModelChoiceSetting(settingsViewModel: SettingsViewModel) {
    val settings = settingsViewModel.settings.value

    Column {
        Text(
            text = stringResource(id = R.string.model_choice),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        SettingsBox(
            settingsViewModel = settingsViewModel,
            title = stringResource(id = R.string.model_to_use),
            description = settings.selectedModelName,
            icon = IconResource.Vector(Icons.Rounded.Memory),
            actionType = ActionType.CUSTOM,
            radius = shapeManager(
                isBoth = true,
                radius = settings.cornerRadius
            ),
            customAction = { onDismiss ->
                ModelChoicePopup(
                    settingsViewModel = settingsViewModel,
                    onDismiss = onDismiss
                )
            }
        )
    }
}

@Composable
private fun ApiKeyPopup(
    settingsViewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val userApiKey by settingsViewModel.userApiKey
    val isApiKeyVerified by settingsViewModel.isApiKeyVerified
    val isVerifyingApiKey by settingsViewModel.isVerifyingApiKey
    var tempApiKey by remember { mutableStateOf(userApiKey) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 4.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.gemini_api_key),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = tempApiKey,
                    onValueChange = { tempApiKey = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.gemini_api_key)) },
                    singleLine = true,
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        settingsViewModel.updateUserApiKey(tempApiKey)
                        settingsViewModel.verifyUserApiKey()
                    },
                    enabled = !isVerifyingApiKey && tempApiKey.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isVerifyingApiKey) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(stringResource(R.string.save_and_check))
                    }
                }
                AnimatedVisibility(
                    visible = isApiKeyVerified && !isVerifyingApiKey,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    Row(
                        modifier = Modifier.padding(top = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.CheckCircle,
                            contentDescription = stringResource(R.string.successfully_verified_icon_cd),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = stringResource(R.string.successfully_verified),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelChoicePopup(
    settingsViewModel: SettingsViewModel,
    onDismiss: () -> Unit
) {
    val settings = settingsViewModel.settings.value
    val models = GeminiModels.supportedModels

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .background(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    shape = RoundedCornerShape(32.dp)
                )
                .padding(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.model_choice),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                fontSize = 20.sp,
            )
            LazyColumn(
                modifier = Modifier
                    .padding(bottom = 12.dp)
                    .clip(RoundedCornerShape(32.dp))
            ) {
                itemsIndexed(models) { index, model ->
                    val isFirst = index == 0
                    val isLast = index == models.lastIndex
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(shapeManager(
                                isFirst = isFirst, isLast = isLast,
                                radius = settings.cornerRadius
                            ))
                            .clickable {
                                settingsViewModel.updateSelectedModel(model.name)
                                onDismiss()
                            },
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        tonalElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = model.icon,
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = model.name,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            RadioButton(
                                selected = settings.selectedModelName == model.name,
                                onClick = {
                                    settingsViewModel.updateSelectedModel(model.name)
                                    onDismiss()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun OfflineTranslationSettings(settingsViewModel: SettingsViewModel) {
    val downloadedModels by settingsViewModel.downloadedModels.collectAsState()
    val processingLanguageCode by settingsViewModel.processingLanguageCode.collectAsState()
    val context = LocalContext.current
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }

    if (showDeleteDialog != null) {
        val langCodeToDelete = showDeleteDialog!!
        val langNameToDelete = settingsViewModel.geminiRepository.supportedLanguages[langCodeToDelete] ?: langCodeToDelete

        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text(text = stringResource(R.string.delete_language_model_title, langNameToDelete)) },
            text = { Text(text = stringResource(R.string.delete_language_model_body)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        settingsViewModel.deleteLanguageModel(langCodeToDelete) { _, message ->
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                        showDeleteDialog = null
                    }
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = stringResource(id = R.string.offline_translation_models),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            Column {
                settingsViewModel.geminiRepository.supportedLanguages.forEach { (code, name) ->
                    val isDownloaded = downloadedModels.contains(code)

                    ListItem(
                        headlineContent = { Text(name) },
                        supportingContent = {
                            Text(if (isDownloaded) stringResource(R.string.downloaded) else stringResource(R.string.not_downloaded))
                        },
                        trailingContent = {
                            if (processingLanguageCode == code) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                if (isDownloaded) {
                                    IconButton(
                                        onClick = { showDeleteDialog = code },
                                        enabled = processingLanguageCode == null
                                    ) {
                                        Icon(
                                            imageVector = Icons.Rounded.Delete,
                                            contentDescription = stringResource(R.string.delete_language_model_cd, name),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }
                                } else {
                                    TextButton(
                                        onClick = {
                                            settingsViewModel.downloadLanguageModel(code) { _, message ->
                                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        enabled = processingLanguageCode == null
                                    ) {
                                        Text(stringResource(id = R.string.download))
                                    }
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}


@Composable
private fun ApiKeyGuide() {
    val uriHandler = LocalUriHandler.current
    val geminiStudioUrl = "https://aistudio.google.com/app/apikey"
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.api_key_guide_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyColumn {
            item {
                Text(
                    text = stringResource(id = R.string.api_key_guide_intro),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
            item { Text(stringResource(id = R.string.api_key_guide_step_1)) }
            item { Spacer(modifier = Modifier.height(12.dp)) }
            item {
                Button(
                    onClick = { uriHandler.openUri(geminiStudioUrl) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(id = R.string.api_key_guide_button))
                }
            }
            item { Spacer(modifier = Modifier.height(12.dp)) }

            items(
                listOf(
                    R.string.api_key_guide_step_2,
                    R.string.api_key_guide_step_3,
                    R.string.api_key_guide_step_4,
                    R.string.api_key_guide_step_5,
                    R.string.api_key_guide_step_6
                )
            ) {
                Text(
                    stringResource(id = it),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
