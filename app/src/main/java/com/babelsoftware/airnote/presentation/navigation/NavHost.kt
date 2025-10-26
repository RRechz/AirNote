package com.babelsoftware.airnote.presentation.navigation

import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.babelsoftware.airnote.domain.model.Settings
import com.babelsoftware.airnote.presentation.screens.edit.EditNoteView
import com.babelsoftware.airnote.presentation.screens.home.HomeView
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.presentation.screens.terms.TermsScreen

@Composable
fun AppNavHost(
    settingsModel: SettingsViewModel,
    navController: NavHostController = rememberNavController(),
    noteId: Int,
    defaultRoute: String,
    isShareIntent: Boolean
) {
    val activity = LocalActivity.current as? ComponentActivity
    val settings by settingsModel.settings

    LaunchedEffect(key1 = isShareIntent, key2 = navController.currentDestination) {
        if (isShareIntent && navController.currentDestination?.route == defaultRoute) {
            val editRoute = NavRoutes.Edit.createRoute(id = 0, encrypted = false, folderId = null)
            navController.navigate(editRoute) {
                popUpTo(defaultRoute) { inclusive = true }
            }
        }
    }

    NavHost(
        navController = navController,
        startDestination = defaultRoute
    ) {
        addHomeRoute(navController, settingsModel, settings)
        addTermsRoute(settingsModel)
        addEditNoteRoute(settingsModel, noteId, activity)
        addSettingsScreens(settingsModel, navController)
    }
}

private fun NavGraphBuilder.addHomeRoute(
    navController: NavHostController,
    settingsModel: SettingsViewModel,
    settings: Settings
) {
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
            settingsModel = settingsModel,
            settings = settings,
            onNavigateToAbout = { navController.navigate(NavRoutes.About.route) },
        )
    }
}

private fun NavGraphBuilder.addTermsRoute(settingsModel: SettingsViewModel) {
    animatedComposable(NavRoutes.Terms.route) {
        TermsScreen(
            settingsModel
        )
    }
}

private fun NavGraphBuilder.addEditNoteRoute(
    settingsModel: SettingsViewModel,
    noteId: Int,
    activity: ComponentActivity?
) {
    animatedComposable(
        route = NavRoutes.Edit.route,
        arguments = listOf(
            navArgument("id") { type = NavType.IntType },
            navArgument("encrypted") { type = NavType.BoolType },
            navArgument("folderId") {
                type = NavType.StringType
                nullable = true
                defaultValue = null
            }
        )
    ) { backStackEntry ->
        val id = backStackEntry.arguments?.getInt("id") ?: 0
        val encrypted = backStackEntry.arguments?.getBoolean("encrypted") ?: false
        val folderIdString = backStackEntry.arguments?.getString("folderId")
        val folderId = if (folderIdString == "null" || folderIdString == null) null else folderIdString.toLongOrNull()

        EditNoteView(
            settingsViewModel = settingsModel,
            id = if (noteId == -1) id else noteId,
            folderId = folderId,
            encrypted = encrypted,
            isWidget = noteId != -1
        ) {
            if (noteId == -1) {
                (backStackEntry.destination.parent?.findNode(NavRoutes.Home.route) != null).let {
                    activity?.onBackPressedDispatcher?.onBackPressed()
                }
            } else {
                activity?.finish()
            }
        }
    }
}

private fun NavGraphBuilder.addSettingsScreens(
    settingsModel: SettingsViewModel,
    navController: NavHostController
) {
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
