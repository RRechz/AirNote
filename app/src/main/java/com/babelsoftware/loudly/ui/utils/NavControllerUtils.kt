package com.babelsoftware.loudly.ui.utils

import androidx.compose.ui.util.fastAny
import androidx.navigation.NavController
import com.babelsoftware.loudly.ui.screens.Screens

fun NavController.backToMain() {
    while (!Screens.Companion.MainScreens.fastAny { it.route == currentBackStackEntry?.destination?.route }) {
        navigateUp()
    }
}