package com.babelsoftware.airnote.presentation.screens.settings

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.rounded.Key
import androidx.compose.material.icons.rounded.Lan
import androidx.compose.material.icons.rounded.Memory
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.SnackbarHost
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.data.repository.GeminiModels
import com.babelsoftware.airnote.presentation.screens.settings.model.IconResource
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.presentation.screens.settings.settings.shapeManager
import com.babelsoftware.airnote.presentation.screens.settings.widgets.ActionType
import com.babelsoftware.airnote.presentation.screens.settings.widgets.SettingsBox
import kotlinx.coroutines.launch

@Composable
fun AirNoteAiSettingsScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    val settings = settingsViewModel.settings.value
    val userApiKey by settingsViewModel.userApiKey

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    LaunchedEffect(key1 = true) {
        settingsViewModel.uiEvent.collect { message ->
            scope.launch { snackbarHostState.showSnackbar(message) }
        }
    }

    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = stringResource(R.string.airnote_ai),
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        onBackNavClicked = { navController.navigateUp() }
    ) {
        LazyColumn {
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(R.string.use_airnote_api),
                    description = stringResource(R.string.use_airnote_api_description),
                    icon = IconResource.Vector(Icons.Rounded.Lan),
                    radius = shapeManager(
                        radius = settings.cornerRadius,
                        isBoth = true
                    ),
                    actionType = ActionType.SWITCH,
                    variable = settings.useAirNoteApi,
                    switchEnabled = { useAirNoteApi ->
                        settingsViewModel.update(
                            settings.copy(useAirNoteApi = useAirNoteApi)
                        )
                    }
                )
            }
            item {
                AnimatedVisibility(visible = !settings.useAirNoteApi) {
                    Column(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Rounded.Key, contentDescription = "API Key Icon")
                            Spacer(modifier = Modifier.size(16.dp))
                            Text(text = stringResource(R.string.your_api_key),
                                style = MaterialTheme.typography.bodyLarge)
                        }
                        OutlinedTextField(
                            value = userApiKey,
                            onValueChange = { settingsViewModel.updateUserApiKey(it) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            label = { Text("API Key") },
                            singleLine = true
                        )
                        Button(
                            onClick = { settingsViewModel.verifyUserApiKey() },
                            modifier = Modifier
                                .align(Alignment.End)
                                .padding(end = 16.dp, bottom = 8.dp),
                            enabled = !settingsViewModel.isVerifyingApiKey.value
                        ) {
                            if (settingsViewModel.isVerifyingApiKey.value) {
                                CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            } else {
                                Text(stringResource(R.string.check))
                            }
                        }
                    }
                }
                ApiKeyGuide()
            }

            item { Spacer(modifier = Modifier.height(18.dp)) }

            item {
                var expanded by remember { mutableStateOf(false) }
                val models = GeminiModels.supportedModels

                Box(modifier = Modifier.fillMaxWidth()) {
                    ListItem(
                        headlineContent = { Text(stringResource(R.string.model_to_use)) },
                        supportingContent = { Text(settings.selectedModelName) },
                        leadingContent = {
                            Icon(
                                imageVector = Icons.Rounded.Memory,
                                contentDescription = stringResource(R.string.model_choice)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { expanded = true }
                    )

                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
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

@Composable
private fun ApiKeyGuide() {
    val uriHandler = LocalUriHandler.current
    val geminiStudioUrl = "https://aistudio.google.com/app/apikey"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.api_key_guide_title),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = stringResource(id = R.string.api_key_guide_intro),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
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