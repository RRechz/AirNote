package com.babelsoftware.airnote.presentation.screens.settings.settings

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.data.repository.GeminiModels
import com.babelsoftware.airnote.presentation.screens.settings.SettingsScaffold
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AirNoteAiSettingsScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    val settings = settingsViewModel.settings.value
    val userApiKey by settingsViewModel.userApiKey
    val isApiKeyVerified by settingsViewModel.isApiKeyVerified

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
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState
        ) {
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
        LazyColumn(
            modifier = Modifier.padding(top = 16.dp)
        ) {
            // --- API Key Entry Field ---
            item {
                Row(
                    modifier = Modifier.padding(start = 16.dp, end = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.your_api_key),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = { showBottomSheet = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.HelpOutline,
                            contentDescription = stringResource(R.string.api_key_guide_title)
                        )
                    }
                }
            }

            item {
                ListItem(
                    colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                    headlineContent = {
                        OutlinedTextField(
                            value = userApiKey,
                            onValueChange = { settingsViewModel.updateUserApiKey(it) },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Gemini API Key") },
                            placeholder = { Text("Enter Your API...") },
                            singleLine = true,
                        )
                    },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Rounded.Key,
                            contentDescription = "API Key Icon",
                            modifier = Modifier.size(32.dp)
                        )
                    },
                    trailingContent = {
                        Button(
                            onClick = { settingsViewModel.verifyUserApiKey() },
                            enabled = !settingsViewModel.isVerifyingApiKey.value && userApiKey.isNotBlank()
                        ) {
                            if (settingsViewModel.isVerifyingApiKey.value) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Text(stringResource(R.string.check))
                            }
                        }
                    }
                )
            }

            item { Spacer(modifier = Modifier.height(24.dp)) }

            // --- Model Selection (Only visible if API Key is verified) ---
            item {
                AnimatedVisibility(
                    visible = isApiKeyVerified,
                    enter = slideInVertically { it } + fadeIn(),
                    exit = slideOutVertically { it } + fadeOut()
                ) {
                    Column {
                        var expanded by remember { mutableStateOf(false) }
                        val models = GeminiModels.supportedModels

                        Text(
                            text = stringResource(id = R.string.model_choice),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            ListItem(
                                headlineContent = { Text(stringResource(R.string.model_to_use)) },
                                supportingContent = { Text(settings.selectedModelName) },
                                leadingContent = {
                                    Icon(
                                        imageVector = Icons.Rounded.Memory,
                                        contentDescription = stringResource(R.string.model_choice),
                                        modifier = Modifier.size(32.dp)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { expanded = true }
                            )

                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false },
                                modifier = Modifier.fillMaxWidth(0.8f)
                            ) {
                                models.forEach { modelName ->
                                    DropdownMenuItem(
                                        text = { Text(modelName) },
                                        onClick = {
                                            settingsViewModel.updateSelectedModel(modelName)
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
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
        Text(
            text = stringResource(id = R.string.api_key_guide_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(stringResource(id = R.string.api_key_guide_step_1))
            Button(
                onClick = { uriHandler.openUri(geminiStudioUrl) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(id = R.string.api_key_guide_button))
            }
            Text(stringResource(id = R.string.api_key_guide_step_2))
            Text(stringResource(id = R.string.api_key_guide_step_3))
            Text(stringResource(id = R.string.api_key_guide_step_4))
            Text(stringResource(id = R.string.api_key_guide_step_5))
            Text(stringResource(id = R.string.api_key_guide_step_6))
        }
    }
}