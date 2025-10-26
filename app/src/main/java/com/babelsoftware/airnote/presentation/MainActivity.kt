package com.babelsoftware.airnote.presentation

import android.content.Intent
import android.os.Bundle
import android.provider.MediaStore
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import androidx.navigation.compose.rememberNavController
import com.babelsoftware.airnote.presentation.navigation.AppNavHost
import com.babelsoftware.airnote.presentation.navigation.NavRoutes
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.presentation.theme.LeafNotesTheme
import dagger.hilt.android.AndroidEntryPoint

fun NavOptionsBuilder.popUpToTop(navController: NavController) {
    popUpTo(navController.currentBackStackEntry?.destination?.route ?: return) {
        inclusive =  true
    }
}

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private lateinit var navController: NavHostController
    private var settingsViewModel: SettingsViewModel? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()
        enableEdgeToEdge()

        setContent {
            settingsViewModel = hiltViewModel<SettingsViewModel>()
            settingsViewModel!!.loadDefaultRoute()

            val noteId = intent?.getIntExtra("noteId", -1) ?: -1
            val isShareIntent = intent?.action == Intent.ACTION_SEND && "text/plain" == intent.type
            val startRoute = settingsViewModel!!.defaultRoute!!

            if (settingsViewModel!!.settings.value.gallerySync) {
                contentResolver.registerContentObserver(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    true,
                    settingsViewModel!!.galleryObserver
                )
            }

            LeafNotesTheme(settingsViewModel!!) {

                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                ) {
                    navController = rememberNavController()
                    AppNavHost(
                        settingsModel = settingsViewModel!!,
                        navController = navController,
                        noteId = noteId,
                        defaultRoute = startRoute,
                        isShareIntent = isShareIntent
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        settingsViewModel?.let {
            if (it.defaultRoute != NavRoutes.Home.route) {
                if (it.settings.value.passcode != null || it.settings.value.fingerprint || it.settings.value.pattern != null) {
                    if (it.settings.value.lockImmediately) {
                        if (::navController.isInitialized) {
                            navController.navigate(it.defaultRoute!!) { popUpToTop(navController) }
                        }
                    }
                }
            }
        }
    }
}
