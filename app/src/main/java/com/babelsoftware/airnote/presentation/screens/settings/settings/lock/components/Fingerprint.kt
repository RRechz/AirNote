package com.babelsoftware.airnote.presentation.screens.settings.settings.lock.components

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.babelsoftware.airnote.presentation.navigation.NavRoutes
import com.babelsoftware.airnote.presentation.popUpToTop
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.R
import java.util.concurrent.Executor



@Composable
fun FingerprintLock(
    settingsViewModel: SettingsViewModel,
    navController: NavController
) {
    val context = LocalContext.current

    BackHandler {
        if (settingsViewModel.settings.value.fingerprint) {
            (context as? ComponentActivity)?.finish()
        } else {
            navController.navigateUp()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.1f)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Fingerprint,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.scale(2f)
        )
        LaunchedEffect(Unit) {
            customizedPrompt(context, settingsViewModel, navController)
        }
    }
}

fun customizedPrompt(
    context: Context,
    settingsViewModel: SettingsViewModel,
    navController: NavController
) {
    showBiometricPrompt(
        context,
        context as AppCompatActivity,
        onAuthError = {
            customizedPrompt(context, settingsViewModel, navController)
        },
        onAuthSuccess = {
            settingsViewModel.update(
                settingsViewModel.settings.value.copy(
                    passcode = null,
                    fingerprint = true,
                    pattern = null,
                    defaultRouteType = NavRoutes.LockScreen.route
                )
            )
            settingsViewModel.updateDefaultRoute(NavRoutes.LockScreen.createRoute(null),)
            navController.navigate(NavRoutes.Home.route) { popUpToTop(navController) }
        }
    )
}

fun showBiometricPrompt(
    context: Context,
    activity: AppCompatActivity,
    onAuthSuccess: () -> Unit,
    onAuthError: (String) -> Unit
) {
    val executor: Executor = ContextCompat.getMainExecutor(activity)

    val biometricPrompt = BiometricPrompt(
        activity,
        executor,
        object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                onAuthSuccess()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                onAuthError(errString.toString())
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                onAuthError("Authentication failed. Please try again.")
            }
        })

    val promptInfo = BiometricPrompt.PromptInfo.Builder()
        .setTitle(context.getString(R.string.fingerprint_name))
        .setSubtitle(context.getString(R.string.app_name))
        .setNegativeButtonText(context.getString(R.string.cancel))
        .setConfirmationRequired(true)
        .build()

    biometricPrompt.authenticate(promptInfo)
}