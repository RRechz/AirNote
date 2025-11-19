package com.babelsoftware.airnote.presentation.screens.settings.settings

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Password
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavController
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.presentation.navigation.NavRoutes
import com.babelsoftware.airnote.presentation.screens.settings.SettingsScaffold
import com.babelsoftware.airnote.presentation.screens.settings.model.IconResource
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.presentation.screens.settings.widgets.ActionType
import com.babelsoftware.airnote.presentation.screens.settings.widgets.SettingsBox

@Composable
fun PrivacyScreen(navController: NavController, settingsViewModel: SettingsViewModel) {
    val settings = settingsViewModel.settings.value
    val context = LocalContext.current

    SettingsScaffold(
        settingsViewModel = settingsViewModel,
        title = stringResource(id = R.string.privacy),
        onBackNavClicked = { navController.navigateUp() }
    ) {
        LazyColumn {
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.screen_protection),
                    description = stringResource(id = R.string.screen_protection_description),
                    icon = IconResource.Vector(Icons.Filled.RemoveRedEye),
                    radius = shapeManager(radius = settings.cornerRadius, isBoth = true),
                    actionType = ActionType.SWITCH,
                    variable = settings.screenProtection,
                    switchEnabled = { settingsViewModel.update(settings.copy(screenProtection = it)) }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }

            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(id = R.string.vault),
                    description = stringResource(id = R.string.vault_description),
                    icon = IconResource.Vector(Icons.Rounded.Security),
                    radius = shapeManager(radius = settings.cornerRadius, isFirst = true),
                    actionType = ActionType.CUSTOM,
                    customAction = { onDismiss ->
                        if (settings.vaultSettingEnabled) {
                            settingsViewModel.update(settings.copy(vaultSettingEnabled = false))
                            onDismiss()
                        } else {
                            var password by remember { mutableStateOf("") }
                            var encryptionType by remember { mutableStateOf("AES-256") }

                            AlertDialog(
                                onDismissRequest = { onDismiss() },
                                title = { Text(stringResource(R.string.vault_setup)) },
                                text = {
                                    Column {
                                        Text(stringResource(R.string.set_a_password_vault))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        OutlinedTextField(
                                            value = password,
                                            onValueChange = { password = it },
                                            label = { Text(stringResource(R.string.vault_password)) },
                                            visualTransformation = PasswordVisualTransformation(),
                                            singleLine = true
                                        )
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(stringResource(R.string.encryption_type))
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(selected = encryptionType == "AES-256", onClick = { encryptionType = "AES-256" })
                                            Text("AES-256")
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            RadioButton(selected = encryptionType == "ChaCha20", onClick = { encryptionType = "ChaCha20" })
                                            Text("ChaCha20")
                                        }
                                    }
                                },
                                confirmButton = {
                                    Button(onClick = {
                                        if (password.isNotBlank()) {
                                            settingsViewModel.update(settings.copy(
                                                vaultSettingEnabled = true,
                                                vaultEnabled = true,
                                                vaultEncryptionType = encryptionType
                                            ))
                                            Toast.makeText(context, context.getString(R.string.vault_created), Toast.LENGTH_SHORT).show()
                                            onDismiss()
                                        } else {
                                            Toast.makeText(context, context.getString(R.string.password_not_empty), Toast.LENGTH_SHORT).show()
                                        }
                                    }) { Text(stringResource(R.string.activate)) }
                                },
                                dismissButton = {
                                    TextButton(onClick = { onDismiss() }) { Text(stringResource(R.string.cancel)) }
                                }
                            )
                        }
                    }
                )
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    title = stringResource(R.string.note_lock_password),
                    description = if (settings.noteLockPassword != null) "********" else stringResource(R.string.not_set),
                    icon = IconResource.Vector(Icons.Rounded.Lock),
                    radius = shapeManager(radius = settings.cornerRadius, isLast = true),
                    actionType = ActionType.CUSTOM,
                    customAction = { onDismiss ->
                        var notePass by remember { mutableStateOf("") }
                        AlertDialog(
                            onDismissRequest = { onDismiss() },
                            title = { Text(stringResource(R.string.note_lock_password)) },
                            text = {
                                Column {
                                    Text(stringResource(R.string.set_a_password_individual_notes))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    OutlinedTextField(
                                        value = notePass,
                                        onValueChange = { notePass = it },
                                        label = { Text(stringResource(R.string.note_password)) },
                                        visualTransformation = PasswordVisualTransformation(),
                                        singleLine = true
                                    )
                                }
                            },
                            confirmButton = {
                                Button(onClick = {
                                    if (notePass.isNotEmpty()) {
                                        settingsViewModel.update(settings.copy(noteLockPassword = notePass))
                                        Toast.makeText(context, context.getString(R.string.passoword_updated), Toast.LENGTH_SHORT).show()
                                        onDismiss()
                                    }
                                }) { Text(stringResource(R.string.save)) }
                            },
                            dismissButton = {
                                TextButton(onClick = { onDismiss() }) { Text(stringResource(R.string.cancel)) }
                            }
                        )
                    }
                )
                Spacer(modifier = Modifier.height(18.dp))
            }
            item {
                SettingsBox(
                    settingsViewModel = settingsViewModel,
                    radius = shapeManager(
                        radius = settings.cornerRadius,
                        isBoth = !settings.appLockEnabled,
                        isFirst = settings.appLockEnabled
                    ),
                    title = stringResource(id = R.string.app_lock),
                    description = if (settings.appLockEnabled) stringResource(id = R.string.app_lock_description_active) else stringResource(id = R.string.app_lock_description),
                    icon = IconResource.Vector(Icons.Filled.Lock),
                    actionType = ActionType.SWITCH,
                    variable = settings.appLockEnabled,
                    switchEnabled = { enabled ->
                        settingsViewModel.update(settings.copy(appLockEnabled = enabled))
                        if (!enabled) settingsViewModel.defaultRoute = NavRoutes.Home.route
                    }
                )
            }
            item {
                AnimatedVisibility(
                    visible = settings.appLockEnabled,
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Column {
                        SettingsBox(
                            settingsViewModel = settingsViewModel,
                            title = stringResource(id = R.string.passcode),
                            description = if (!settings.passcode.isNullOrBlank()) stringResource(id = R.string.active) else stringResource(R.string.tap_to_setup),
                            icon = IconResource.Vector(Icons.Filled.Password),
                            radius = shapeManager(radius = settings.cornerRadius),
                            actionType = ActionType.CUSTOM,
                            customAction = { onDismiss ->
                                var pass by remember { mutableStateOf("") }
                                AlertDialog(
                                    onDismissRequest = { onDismiss() },
                                    title = { Text(stringResource(R.string.applock_password)) },
                                    text = {
                                        OutlinedTextField(
                                            value = pass,
                                            onValueChange = { pass = it },
                                            label = { Text(stringResource(R.string.new_password)) },
                                            visualTransformation = PasswordVisualTransformation(),
                                            singleLine = true
                                        )
                                    },
                                    confirmButton = {
                                        Button(onClick = {
                                            if (pass.length >= 4) {
                                                settingsViewModel.update(settings.copy(passcode = pass))
                                                settingsViewModel.updateDefaultRoute(NavRoutes.LockScreen.createRoute(null))
                                                Toast.makeText(context, context.getString(R.string.password_set), Toast.LENGTH_SHORT).show()
                                                onDismiss()
                                            } else {
                                                Toast.makeText(context, context.getString(R.string.enter_least_4), Toast.LENGTH_SHORT).show()
                                            }
                                        }) { Text(stringResource(R.string.save)) }
                                    },
                                    dismissButton = {
                                        TextButton(onClick = { onDismiss() }) { Text(stringResource(R.string.cancel)) }
                                    }
                                )
                            }
                        )
                        SettingsBox(
                            settingsViewModel = settingsViewModel,
                            title = stringResource(id = R.string.fingerprint),
                            description = if (settings.fingerprint) stringResource(id = R.string.active) else stringResource(R.string.tap_to_activate),
                            icon = IconResource.Vector(Icons.Filled.Fingerprint),
                            radius = shapeManager(radius = settings.cornerRadius),
                            actionType = ActionType.SWITCH,
                            variable = settings.fingerprint,
                            switchEnabled = { isChecked ->
                                if (isChecked) {
                                    checkBiometricSupport(context) {
                                        settingsViewModel.update(settings.copy(fingerprint = true))
                                        Toast.makeText(context, context.getString(R.string. fingerprint_activate), Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    settingsViewModel.update(settings.copy(fingerprint = false))
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

private fun checkBiometricSupport(context: android.content.Context, onSuccess: () -> Unit) {
    val biometricManager = BiometricManager.from(context)
    if (biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
        val activity = context as? FragmentActivity ?: return
        val executor = ContextCompat.getMainExecutor(context)
        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onSuccess()
            }
            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                Toast.makeText(context, "Error: $errString", Toast.LENGTH_SHORT).show()
            }
        }
        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(context.getString(R.string.fingerprint_verification))
            .setSubtitle(context.getString(R.string.verify_to_enable))
            .setNegativeButtonText(context.getString(R.string.cancel))
            .build()
        BiometricPrompt(activity, executor, callback).authenticate(promptInfo)
    } else {
        Toast.makeText(context, context.getString(R.string.not_support_fingerprint), Toast.LENGTH_LONG).show()
    }
}
