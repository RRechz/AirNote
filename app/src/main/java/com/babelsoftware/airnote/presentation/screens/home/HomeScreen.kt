package com.babelsoftware.airnote.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.domain.model.Folder
import com.babelsoftware.airnote.domain.model.Note
import com.babelsoftware.airnote.presentation.components.CloseButton
import com.babelsoftware.airnote.presentation.components.DeleteButton
import com.babelsoftware.airnote.presentation.components.NotesButton
import com.babelsoftware.airnote.presentation.components.NotesScaffold
import com.babelsoftware.airnote.presentation.components.PinButton
import com.babelsoftware.airnote.presentation.components.SelectAllButton
import com.babelsoftware.airnote.presentation.components.SettingsButton
import com.babelsoftware.airnote.presentation.components.TitleText
import com.babelsoftware.airnote.presentation.components.VaultButton
import com.babelsoftware.airnote.presentation.components.defaultScreenEnterAnimation
import com.babelsoftware.airnote.presentation.components.defaultScreenExitAnimation
import com.babelsoftware.airnote.presentation.screens.home.viewmodel.HomeViewModel
import com.babelsoftware.airnote.presentation.screens.home.widgets.AddFolderDialog
import com.babelsoftware.airnote.presentation.screens.home.widgets.FolderActionBottomSheet
import com.babelsoftware.airnote.presentation.screens.home.widgets.MoveToFolderDialog
import com.babelsoftware.airnote.presentation.screens.home.widgets.NoteFilter
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.presentation.screens.settings.settings.PasswordPrompt
import com.babelsoftware.airnote.presentation.screens.settings.settings.shapeManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@Composable
fun HomeView (
    viewModel: HomeViewModel = hiltViewModel(),
    settingsModel: SettingsViewModel,
    onSettingsClicked: () -> Unit,
    onNoteClicked: (noteId: Int, isVault: Boolean, folderId: Long?) -> Unit
) {
    val allFolders by viewModel.allFolders.collectAsState()
    val notes by viewModel.displayedNotes.collectAsState()
    val settings = settingsModel.settings.value
    val selectedFolderId = viewModel.selectedFolderId.value

    val selectedFolder = remember(selectedFolderId, allFolders) {
        allFolders.find { it.id == selectedFolderId }
    }
    val topBarColor = MaterialTheme.colorScheme.surfaceContainerLow

    val context = LocalContext.current
    if (viewModel.isPasswordPromptVisible.value) {
        PasswordPrompt(
            context = context,
            text = stringResource(id = R.string.password_continue),
            settingsViewModel = settingsModel,
            onExit = { password ->
                if (password != null) {
                    if (password.text.isNotBlank()) {
                        viewModel.encryptionHelper.setPassword(password.text)
                        viewModel.observeNotes()
                    }
                }
                viewModel.toggleIsPasswordPromptVisible(false)
            }
        )
    }

    if (viewModel.isAddFolderDialogVisible.value) {
        AddFolderDialog(
            onDismiss = { viewModel.setAddFolderDialogVisibility(false) },
            onConfirm = { name, color ->
                viewModel.addFolder(name, color)
            }
        )
    }

    viewModel.folderForAction.value?.let { folder ->
        if (!viewModel.showDeleteConfirmDialog.value) {
            FolderActionBottomSheet(
                folder = folder,
                onDismiss = { viewModel.onDismissFolderAction() },
                onEditClick = { viewModel.onEditFolderRequest() },
                onDeleteClick = {
                    viewModel.onDeleteFolderRequest()
                }
            )
        }
    }

    viewModel.folderToEdit.value?.let { folder ->
        AddFolderDialog(
            onDismiss = { viewModel.onDismissEditFolderDialog() },
            onConfirm = { name, color ->
                viewModel.updateFolder(name, color)
            },
            folderToEdit = folder
        )
    }

    if (viewModel.isMoveToFolderDialogVisible.value) {
        val folders by viewModel.allFolders.collectAsState()
        MoveToFolderDialog(
            folders = folders,
            onDismiss = { viewModel.setMoveToFolderDialogVisibility(false) },
            onFolderSelected = { folderId ->
                viewModel.moveSelectedNotesToFolder(folderId)
            }
        )
    }

    if (viewModel.showDeleteConfirmDialog.value) {
        viewModel.folderForAction.value?.let { folder ->
            AlertDialog(
                onDismissRequest = { viewModel.onDismissFolderAction() },
                title = { Text(stringResource(R.string.delete_folder)) },
                text = { Text(stringResource(R.string.delete_folder_confirmation, folder.name)) },
                confirmButton = {
                    TextButton(onClick = { viewModel.confirmFolderDeletion() }) {
                        Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.onDismissFolderAction() }) {
                        Text(stringResource(R.string.cancel))
                    }
                }
            )
        }
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { message ->
            scope.launch {
                snackbarHostState.showSnackbar(message)
            }
        }
    }

    if (settingsModel.databaseUpdate.value) viewModel.observeNotes()
    val containerColor = getContainerColor(settingsModel)
    NotesScaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        floatingActionButton = {
            NotesButton(text = stringResource(R.string.new_note)) {
                viewModel.onAddNewNoteClicked { noteId, isVault, folderId ->
                    onNoteClicked(noteId, isVault, folderId)
                }
            }
        },
        topBar = {
            AnimatedVisibility(
                visible = viewModel.selectedNotes.isNotEmpty(),
                enter = defaultScreenEnterAnimation(),
                exit = defaultScreenExitAnimation()
            ) {
                val notes by viewModel.displayedNotes.collectAsState()
                SelectedNotesTopAppBar(
                    containerColor = topBarColor,
                    selectedNotes = viewModel.selectedNotes,
                    allNotes = notes,
                    settingsModel = settingsModel,
                    onPinClick = { viewModel.pinOrUnpinNotes() },
                    onDeleteClick = { viewModel.toggleIsDeleteMode(true) },
                    onSelectAllClick = { selectAllNotes(viewModel, notes) },
                    onMoveToFolderClick = { viewModel.setMoveToFolderDialogVisibility(true) },
                    onCloseClick = { viewModel.selectedNotes.clear() }
                )
            }

            AnimatedVisibility(
                visible = viewModel.selectedNotes.isEmpty(),
                enter = defaultScreenEnterAnimation(),
                exit = defaultScreenExitAnimation()
            ) {
                Column(
                    modifier = Modifier.background(topBarColor)
                ) {
                    NotesSearchBar(
                        settingsModel = settingsModel,
                        query = viewModel.searchQuery.value,
                        onQueryChange = { viewModel.changeSearchQuery(it) },
                        onSettingsClick = onSettingsClicked,
                        onClearClick = { viewModel.changeSearchQuery("") },
                        viewModel = viewModel,
                        onVaultClicked = {
                            if (!viewModel.isVaultMode.value) {
                                viewModel.toggleIsPasswordPromptVisible(true)
                            } else {
                                viewModel.toggleIsVaultMode(false)
                                viewModel.encryptionHelper.removePassword()
                            }
                        }
                    )
                    val folders by viewModel.allFolders.collectAsState()
                    FolderBar(
                        folders = folders,
                        selectedFolderId = viewModel.selectedFolderId.value,
                        onFolderSelected = { viewModel.selectFolder(it) },
                        onAddFolderClicked = { viewModel.setAddFolderDialogVisibility(true) },
                        onFolderLongClick = { folder -> viewModel.onFolderLongPressed(folder) } // Dikkat
                    )
                }
            }
        },
        content = {
            NoteFilter(
                settingsViewModel = settingsModel,
                containerColor = containerColor,
                shape = shapeManager(
                    radius = settingsModel.settings.value.cornerRadius / 2,
                    isBoth = true
                ),
                notes = notes.sortedWith(sorter(settings.sortDescending)),
                allFolders = allFolders,
                onNoteClicked = { noteId ->
                    val clickedNote = viewModel.displayedNotes.value.find { it.id == noteId }
                    if (clickedNote != null) {
                        onNoteClicked(
                            clickedNote.id,
                            clickedNote.encrypted,
                            clickedNote.folderId
                        )
                    }
                },
                selectedNotes = viewModel.selectedNotes,
                viewMode = settingsModel.settings.value.viewMode,
                searchText = viewModel.searchQuery.value.ifBlank { null },
                isDeleteMode = viewModel.isDeleteMode.value,
                onNoteUpdate = { note ->
                    CoroutineScope(Dispatchers.IO).launch {
                        viewModel.updateNote(note)
                    }
                },
                onDeleteNote = {
                    viewModel.toggleIsDeleteMode(false)
                    viewModel.deleteNoteById(it)
                },
            )
        }
    )
}

@Composable
fun getContainerColor(settingsModel: SettingsViewModel): Color {
    return if (settingsModel.settings.value.extremeAmoledMode) Color.Black else MaterialTheme.colorScheme.surfaceContainerHigh
}

@Composable
private fun NewNoteButton(onNoteClicked: (Int) -> Unit) {
    NotesButton(text = stringResource(R.string.new_note)) {
        onNoteClicked(0)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectedNotesTopAppBar(
    selectedNotes: List<Note>,
    allNotes: List<Note>,
    settingsModel: SettingsViewModel,
    onPinClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSelectAllClick: () -> Unit,
    onMoveToFolderClick: () -> Unit,
    containerColor: Color,
    onCloseClick: () -> Unit
) {
    var deletelaert by remember {
        mutableStateOf(false)
    }
    AnimatedVisibility(visible = deletelaert) {
        AlertDialog(onDismissRequest = { deletelaert = false }, title = {
            Text(
                text = stringResource(id = R.string.alert_text)
            )
        }, confirmButton = {
            TextButton(onClick = { deletelaert=false
                onDeleteClick()
            }) {
                Text(text = stringResource(id = R.string.yes), color = MaterialTheme.colorScheme.error )
            }
        },
            dismissButton = {
                TextButton(onClick = { deletelaert = false }) {
                    Text(text =stringResource(id = R.string.cancel))
                }
            })

    }
    TopAppBar(
        modifier = Modifier.padding(bottom = 36.dp),
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = containerColor
        ),
        title = { TitleText(titleText = selectedNotes.size.toString()) },
        navigationIcon = { CloseButton(onCloseClicked = onCloseClick) },
        actions = {
            Row {
                IconButton(onClick = onMoveToFolderClick) {
                    Icon(Icons.Default.FolderOpen, contentDescription = "Klasöre Taşı")
                }
                PinButton(isPinned = selectedNotes.all { it.pinned }, onClick = onPinClick)
                DeleteButton(onClick = { deletelaert = true })
                SelectAllButton(
                    enabled = selectedNotes.size != allNotes.size,
                    onClick = onSelectAllClick
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NotesSearchBar(
    settingsModel: SettingsViewModel,
    viewModel: HomeViewModel,
    query: String,
    onQueryChange: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onVaultClicked: () -> Unit,
    onClearClick: () -> Unit
) {
    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (settingsModel.settings.value.makeSearchBarLonger) 16.dp else 36.dp, vertical =  18.dp),
        query = query,
        placeholder = { Text(stringResource(R.string.search)) },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
        trailingIcon = {
            Row {
                if (query.isNotBlank()) {
                    CloseButton(contentDescription = "Clear", onCloseClicked = onClearClick)
                }
                if (settingsModel.settings.value.vaultSettingEnabled) {
                    VaultButton(viewModel.isVaultMode.value) { onVaultClicked() }
                }
                SettingsButton(onSettingsClicked = onSettingsClick)
            }
        },
        onQueryChange = onQueryChange,
        onSearch = onQueryChange,
        onActiveChange = {},
        active = false,
    ) {}
}

private fun selectAllNotes(viewModel: HomeViewModel, allNotes: List<Note>) {
    allNotes.forEach {
        if (!viewModel.selectedNotes.contains(it)) {
            viewModel.selectedNotes.add(it)
        }
    }
}

fun sorter(descending: Boolean): Comparator<Note> {
    return if (descending) {
        compareByDescending { it.createdAt }
    } else {
        compareBy { it.createdAt }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderBar(
    folders: List<Folder>,
    selectedFolderId: Long?,
    onFolderSelected: (Long?) -> Unit,
    onAddFolderClicked: () -> Unit,
    onFolderLongClick: (Folder) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        item {
            Button(
                onClick = { onFolderSelected(null) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (selectedFolderId == null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Text(stringResource(R.string.all_notes))
            }
        }

        items(folders) { folder ->
            val isSelected = selectedFolderId == folder.id
            Surface(
                modifier = Modifier
                    .clip(CircleShape)
                    .combinedClickable(
                        onClick = { onFolderSelected(folder.id) },
                        onLongClick = { onFolderLongClick(folder) }
                    ),
                shape = CircleShape,
                color = if (isSelected) Color(android.graphics.Color.parseColor(folder.color)) else MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = 2.dp
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = folder.name,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            IconButton(onClick = onAddFolderClicked) {
                Icon(Icons.Default.Add, contentDescription = "Yeni Klasör Ekle")
            }
        }
    }
}