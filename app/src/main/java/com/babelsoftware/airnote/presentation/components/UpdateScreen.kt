/*
 * Copyright (c) 2025 Babel Software.
 *
 * This file and codes created by RRechz - Babel Software
 */

package com.babelsoftware.airnote.presentation.components

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.util.ChangelogResult
import com.babelsoftware.airnote.util.getChangelogFromGitHub
import kotlinx.coroutines.launch

@Composable
fun UpdateScreen(
    latestVersion: String,
    onDismiss: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val context = LocalContext.current
    var changelog by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        scope.launch {
            changelog = when (val result = getChangelogFromGitHub()) {
                is ChangelogResult.Success -> {
                    if (result.body.isNotBlank()) {
                        result.body
                    } else {
                        context.getString(R.string.changelog_fetch_error)
                    }
                }
                is ChangelogResult.Error -> {
                    context.getString(R.string.changelog_fetch_error_with_message, result.exception.message ?: "Bilinmeyen Hata")
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "${stringResource(R.string.newversion)} $latestVersion")
        },
        text = {
            LazyColumn(modifier = Modifier.padding(vertical = 8.dp)) {
                item {
                    Text(text = changelog)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onNavigateToAbout()
                    onDismiss()
                }
            ) {
                Text(stringResource(id = R.string.update))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(id = R.string.cancel))
            }
        }
    )
}