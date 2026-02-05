package com.babelsoftware.loudly.ui.screens.settings

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.babelsoftware.loudly.ui.screens.guides.*
import com.babelsoftware.loudly.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TipsScreen(
    navController: NavController
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.tips_and_features)) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(modifier = Modifier.padding(paddingValues)) {
            item { StoryShareFeatureStep() }
            item { SettingsHeaderFeatureStep() }
            item { PlayerPlaylistFeatureStep() }
            item { ClassicWidgetFeatureStep() }
            item { NewSettingsUiFeatureStep() }
        }
    }
}