package com.babelsoftware.airnote.presentation.screens.home.widgets

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.domain.model.Folder
import com.babelsoftware.airnote.domain.model.Note
import com.babelsoftware.airnote.presentation.components.FolderIndicator
import com.babelsoftware.airnote.presentation.components.markdown.MarkdownText
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.presentation.theme.FontUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    settingsViewModel: SettingsViewModel,
    containerColor: Color,
    note: Note,
    allFolders: List<Folder>,
    isBorderEnabled: Boolean,
    shape: RoundedCornerShape,
    onShortClick: () -> Unit,
    onLongClick: () -> Unit,
    onNoteUpdate: (Note) -> Unit
) {
    val borderModifier = if (isBorderEnabled) {
        Modifier.border(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.primary,
            shape = shape
        )
    } else if (containerColor == Color.Black) {
        Modifier.border(
            width = 1.5.dp,
            color = MaterialTheme.colorScheme.surfaceContainerHighest,
            shape = shape
        )
    } else {
        Modifier
    }

    val folder = note.folderId?.let { id ->
        allFolders.find { it.id == id }
    }

    ElevatedCard(
        modifier = Modifier
            .padding(bottom = 12.dp)
            .clip(shape)
            .combinedClickable(
                onClick = onShortClick,
                onLongClick = onLongClick
            )
            .then(borderModifier),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (containerColor != Color.Black) 6.dp else 0.dp
        ),
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp, 12.dp, 16.dp, 12.dp)
            ) {
                if (note.name.isNotBlank()) {
                    MarkdownText(
                        isPreview = true,
                        isEnabled = settingsViewModel.settings.value.isMarkdownEnabled,
                        markdown = note.name.replaceFirstChar { it.uppercase() },
                        modifier = Modifier
                            .heightIn(max = dimensionResource(R.dimen.max_name_height))
                            .then(
                                if (note.description.isNotBlank() && !settingsViewModel.settings.value.showOnlyTitle) {
                                    Modifier.padding(bottom = 9.dp)
                                } else {
                                    Modifier
                                }
                            ),
                        weight = FontWeight.Bold,
                        spacing = 0.dp,
                        onContentChange = { onNoteUpdate(note.copy(name = it)) },
                        fontSize = FontUtils.getTitleFontSize(settingsViewModel),
                        radius = settingsViewModel.settings.value.cornerRadius,
                        settingsViewModel = settingsViewModel
                    )
                }

                if (note.description.isNotBlank() && !settingsViewModel.settings.value.showOnlyTitle) {
                    MarkdownText(
                        isPreview = true,
                        markdown = note.description,
                        isEnabled = settingsViewModel.settings.value.isMarkdownEnabled,
                        spacing = 0.dp,
                        modifier = Modifier
                            .heightIn(max = dimensionResource(R.dimen.max_description_height)),
                        onContentChange = { onNoteUpdate(note.copy(description = it)) },
                        fontSize = FontUtils.getBodyFontSize(settingsViewModel),
                        radius = settingsViewModel.settings.value.cornerRadius,
                        settingsViewModel = settingsViewModel
                    )
                }
            }

            if (folder != null && settingsViewModel.settings.value.showFolderIndicator) {
                FolderIndicator(
                    folderName = folder.name,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
            }
        }
    }
}