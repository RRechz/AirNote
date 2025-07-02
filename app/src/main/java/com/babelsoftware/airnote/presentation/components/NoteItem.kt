/*
 * Copyright (c) 2025 Babel Software.
 */

package com.babelsoftware.airnote.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.babelsoftware.airnote.domain.model.Folder
import com.babelsoftware.airnote.domain.model.Note

@Composable
fun FolderIndicator(
    folderName: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.padding(top = 8.dp, end = 8.dp),
        shape = RoundedCornerShape(percent = 50),
        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.9f),
        tonalElevation = 2.dp
    ) {
        Text(
            text = folderName,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onTertiaryContainer
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteItem(
    note: Note,
    allFolders: List<Folder>,
    onClick: () -> Unit
) {
    val folder = note.folderId?.let { id ->
        allFolders.find { it.id == id }
    }

    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                // DÜZELTME: 'note.title' yerine 'note.name' kullanıldı.
                Text(
                    text = note.name, //
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                // DÜZELTME: 'note.content' yerine 'note.description' kullanıldı.
                Text(
                    text = note.description, //
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 4,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            if (folder != null) {
                FolderIndicator(
                    folderName = folder.name,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}