package com.babelsoftware.loudly.extensions

import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

fun NavController.backToMain(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}

fun NavController.backToMain() {
    navigate(graph.startDestinationRoute!!) {
        popUpTo(graph.findStartDestination().id)
        launchSingleTop = true
    }
}