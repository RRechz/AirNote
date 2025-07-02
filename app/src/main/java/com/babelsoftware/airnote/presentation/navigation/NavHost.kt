package com.babelsoftware.airnote.presentation.navigation

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.babelsoftware.airnote.presentation.screens.edit.EditNoteView
import com.babelsoftware.airnote.presentation.screens.home.HomeView
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.presentation.screens.settings.settings.lock.LockScreen
import com.babelsoftware.airnote.presentation.screens.terms.TermsScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

@Composable
fun AppNavHost(settingsModel: SettingsViewModel, navController: NavHostController = rememberNavController(), noteId: Int, defaultRoute: String) {

    val activity = (LocalContext.current as? Activity)
    val startRoute = if (defaultRoute != NavRoutes.LockScreen.route && noteId != -1) NavRoutes.Edit.route else defaultRoute

    NavHost(navController, startDestination = startRoute) {
        animatedComposable(NavRoutes.Home.route) {
            HomeView(
                onSettingsClicked = { navController.navigate(NavRoutes.Settings.route) },
                onNoteClicked = { id, encrypted, folderId ->
                    navController.navigate(
                        NavRoutes.Edit.createRoute(
                            id,
                            encrypted,
                            folderId
                        )
                    )
                },
                settingsModel = settingsModel
            )
        }

        animatedComposable(NavRoutes.Terms.route) {
            TermsScreen(
                settingsModel
            )
        }

        animatedComposable(
            route = NavRoutes.Edit.route,
            arguments = listOf(
                navArgument("id") { type = NavType.IntType },
                navArgument("encrypted") { type = NavType.BoolType },
                navArgument("folderId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            val encrypted = backStackEntry.arguments?.getBoolean("encrypted") ?: false

            var folderId = backStackEntry.arguments?.getLong("folderId")
            if (folderId == -1L) {
                folderId = null
            }

            EditNoteView(
                settingsViewModel = settingsModel,
                id = if (noteId == -1) id else noteId,
                folderId = folderId,
                encrypted = encrypted,
                isWidget = noteId != -1
            ) {
                if (noteId == -1) {
                    navController.navigateUp()
                } else {
                    activity?.finish()
                }
            }
        }

        settingScreens.forEach { (route, screen) ->
            if (route == NavRoutes.Settings.route) {
                slideInComposable(route) {
                    screen(settingsModel, navController)
                }
            } else {
                animatedComposable(route) {
                    screen(settingsModel, navController)
                }
            }
        }
    }
}

suspend fun getDefaultRoute(
    settingsModel: SettingsViewModel,
    noteId: Int
): String {
    val routeFlow = MutableStateFlow<String?>(null)
    runBlocking {
            val route = when {
                settingsModel.settings.value.passcode != null -> NavRoutes.LockScreen.route
                !settingsModel.settings.value.termsOfService -> NavRoutes.Terms.route
                noteId == -1 -> NavRoutes.Home.route
                else -> NavRoutes.Edit.createRoute(noteId, false)
            }
            routeFlow.value = route
        }

    return routeFlow.filterNotNull().first()
}
