/*
 * Copyright (c) 2025 Babel Software.
 */

package com.babelsoftware.airnote.presentation.screens.home.widgets

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.domain.model.Folder

@Composable
fun AddFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorHex: String) -> Unit,
    folderToEdit: Folder? = null
) {
    var folderName by remember { mutableStateOf("") }
    val folderColors = listOf(
        "#FF5733", "#33FF57", "#3357FF", "#FF33A1",
        "#A133FF", "#33FFA1", "#FFC300", "#C70039"
    )
    var selectedColor by remember { mutableStateOf(folderColors.first()) }
    val isEditing = folderToEdit != null

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = stringResource(if (isEditing) R.string.edit_folder else R.string.add_new_folder)) },
        text = {
            Column {
                // Klasör adı için TextField
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text(stringResource(R.string.folder_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

                Text(text = stringResource(R.string.folder_color), style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(folderColors) { colorHex ->
                        val color = Color(android.graphics.Color.parseColor(colorHex))
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(color)
                                .clickable { selectedColor = colorHex }
                                .border(
                                    width = 2.dp,
                                    color = if (selectedColor == colorHex) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (folderName.isNotBlank()) {
                        onConfirm(folderName, selectedColor)
                    }
                },
                enabled = folderName.isNotBlank()
            ) {
                Text(stringResource(if (isEditing) R.string.save else R.string.add))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}