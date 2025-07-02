/*
 * Copyright (c) 2025 Babel Software.
 */

package com.babelsoftware.airnote.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun NotesScaffold(
    modifier: Modifier = Modifier,
    topBar : @Composable () -> Unit = {},
    floatingActionButton: @Composable () -> Unit = {},
    snackbarHost: @Composable () -> Unit = {},
    // DEĞİŞİKLİK: content parametresini tekrar orijinal, basit haline getiriyoruz.
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = modifier,
        snackbarHost = snackbarHost,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        topBar = { topBar() },
        floatingActionButton = { floatingActionButton() },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding)) {
            content()
        }
    }
}