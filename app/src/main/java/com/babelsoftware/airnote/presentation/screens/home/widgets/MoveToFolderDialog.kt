/*
 * Copyright (c) 2025 Babel Software.
 */

package com.babelsoftware.airnote.presentation.screens.home.widgets

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.domain.model.Folder

@Composable
fun MoveToFolderDialog(
    folders: List<Folder>,
    onDismiss: () -> Unit,
    onFolderSelected: (folderId: Long) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(R.string.move_to_folder)) },
        text = {
            if (folders.isEmpty()) {
                Text(text = stringResource(R.string.no_folders_available))
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(folders) { folder ->
                        ListItem(
                            headlineContent = { Text(folder.name) },
                            leadingContent = {
                                Icon(
                                    imageVector = Icons.Default.Folder,
                                    contentDescription = folder.name,
                                    tint = Color(android.graphics.Color.parseColor(folder.color))
                                )
                            },
                            modifier = Modifier.clickable {
                                onFolderSelected(folder.id)
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            // Bu diyalogda sadece seçim yapıldığı için confirm butonu yerine
            // direkt liste elemanına tıklama aksiyonu kullanıyoruz.
            // İsterseniz bir "Vazgeç" butonu ekleyebilirsiniz.
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}