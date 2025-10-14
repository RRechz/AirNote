package com.babelsoftware.airnote.presentation.screens.home.widgets

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.staggeredgrid.LazyStaggeredGridState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.babelsoftware.airnote.domain.model.Note
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.domain.model.Folder
import com.babelsoftware.airnote.R

@Composable
fun NoteFilter(
    modifier: Modifier = Modifier,
    listState: LazyStaggeredGridState = rememberLazyStaggeredGridState(),
    settingsViewModel: SettingsViewModel,
    containerColor : Color,
    onNoteClicked: (Int) -> Unit,
    shape: RoundedCornerShape,
    notes: List<Note>,
    allFolders: List<Folder>,
    searchText: String? = null,
    selectedNotes: MutableList<Note> = mutableListOf(),
    viewMode: Boolean = false,
    isDeleteMode: Boolean = false,
    onNoteUpdate: (Note) -> Unit = {},
    onDeleteNote: (Int) -> Unit = {}
) {
    if (notes.isEmpty()) {
        Placeholder(
            modifier = modifier,
            placeholderIcon = {
                Icon(
                    getEmptyIcon(searchText),
                    contentDescription = "Placeholder icon",
                    tint = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(64.dp)
                )
            },
            placeholderText = getEmptyText(searchText)
        )
    } else {
        NotesGrid(
            modifier = modifier,
            listState = listState,
            settingsViewModel = settingsViewModel,
            containerColor = containerColor,
            onNoteClicked = onNoteClicked,
            notes = notes,
            allFolders = allFolders,
            shape = shape,
            onNoteUpdate = onNoteUpdate,
            selectedNotes = selectedNotes,
            viewMode = viewMode,
            isDeleteClicked = isDeleteMode,
            animationFinished = onDeleteNote
        )
    }
}

@Composable
private fun getEmptyText(searchText: String?): String {
    return when {
        searchText.isNullOrEmpty() -> stringResource(R.string.no_created_notes)
        else -> stringResource(R.string.no_found_notes)
    }
}

@Composable
private fun getEmptyIcon(searchText: String?): ImageVector {
    return when {
        searchText.isNullOrEmpty() -> Icons.AutoMirrored.Rounded.Notes
        else -> Icons.Rounded.Search
    }
}
