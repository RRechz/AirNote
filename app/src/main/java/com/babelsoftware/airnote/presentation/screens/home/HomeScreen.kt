package com.babelsoftware.airnote.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.domain.model.AiSuggestion
import com.babelsoftware.airnote.domain.model.ChatMessage
import com.babelsoftware.airnote.domain.model.Folder
import com.babelsoftware.airnote.domain.model.Note
import com.babelsoftware.airnote.domain.model.Participant
import com.babelsoftware.airnote.presentation.components.CloseButton
import com.babelsoftware.airnote.presentation.components.DeleteButton
import com.babelsoftware.airnote.presentation.components.NotesScaffold
import com.babelsoftware.airnote.presentation.components.PinButton
import com.babelsoftware.airnote.presentation.components.SelectAllButton
import com.babelsoftware.airnote.presentation.components.SettingsButton
import com.babelsoftware.airnote.presentation.components.TitleText
import com.babelsoftware.airnote.presentation.components.UpdateScreen
import com.babelsoftware.airnote.presentation.components.VaultButton
import com.babelsoftware.airnote.presentation.components.defaultScreenEnterAnimation
import com.babelsoftware.airnote.presentation.components.defaultScreenExitAnimation
import com.babelsoftware.airnote.presentation.screens.home.viewmodel.DraftedNote
import com.babelsoftware.airnote.presentation.screens.home.viewmodel.HomeViewModel
import com.babelsoftware.airnote.presentation.screens.home.widgets.AddFolderDialog
import com.babelsoftware.airnote.presentation.screens.home.widgets.FolderActionBottomSheet
import com.babelsoftware.airnote.presentation.screens.home.widgets.MoveToFolderDialog
import com.babelsoftware.airnote.presentation.screens.home.widgets.NoteFilter
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.presentation.screens.settings.settings.PasswordPrompt
import com.babelsoftware.airnote.presentation.screens.settings.settings.shapeManager
import com.babelsoftware.airnote.presentation.theme.AiButtonColors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeView (
    viewModel: HomeViewModel = hiltViewModel(),
    settingsModel: SettingsViewModel,
    onSettingsClicked: () -> Unit,
    onNoteClicked: (noteId: Int, isVault: Boolean, folderId: Long?) -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val allFolders by viewModel.allFolders.collectAsState()
    val notes by viewModel.displayedNotes.collectAsState()
    val settings = settingsModel.settings.value
    val selectedFolderId = viewModel.selectedFolderId.value
    val context = LocalContext.current

    LaunchedEffect(key1 = Unit) {
        settingsModel.checkForNewUpdate(context)
    }

    if (settingsModel.showUpdateDialog.value) {
        UpdateScreen(
            latestVersion = settingsModel.latestVersion.value,
            onDismiss = { settingsModel.dismissUpdateDialog() },
            onNavigateToAbout = onNavigateToAbout
        )
    }

    val selectedFolder = remember(selectedFolderId, allFolders) {
        allFolders.find { it.id == selectedFolderId }
    }
    val topBarColor = MaterialTheme.colorScheme.surfaceContainerLow
    val listState = rememberLazyStaggeredGridState() // LazyListState to listen to the scrolling state of the note list

    // ---> Expansion and reduction of the FAB according to the scrolling situation
    val isFabExtended by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex == 0
        }
    }
    // <---
    viewModel.setFabExtended(isFabExtended)

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

    // AI Chat Window (ModalBottomSheet)
    if (viewModel.isAiChatSheetVisible.value) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.toggleAiChatSheet(false)
                viewModel.resetChatState()
            },
            sheetState = sheetState,
            modifier = Modifier.fillMaxWidth(),
        ) {
            val chatState = viewModel.chatState.value

            if (chatState.latestDraft != null) {
                DraftDisplay(
                    draft = chatState.latestDraft,
                    onSave = { viewModel.saveDraftedNote() },
                    onRegenerate = { viewModel.regenerateDraft() }
                )
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        state = rememberLazyListState(
                            initialFirstVisibleItemIndex = if (chatState.messages.isNotEmpty()) chatState.messages.size - 1 else 0
                        )
                    ) {
                        items(chatState.messages) { message ->
                            ChatMessageItem(message = message)
                        }
                    }

                    if (chatState.messages.isEmpty()) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.ai_welcome_message),
                                style = MaterialTheme.typography.titleLarge,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = stringResource(R.string.ai_suggestions_title),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 8.dp),
                                textAlign = TextAlign.Start
                            )
                            viewModel.suggestions.forEach { suggestion ->
                                SuggestionItem(suggestion = suggestion)
                            }
                        }
                    }

                    ChatInputBar(
                        isAwaitingTopic = chatState.isAwaitingDraftTopic,
                        onSendMessage = { message ->
                            if (chatState.isAwaitingDraftTopic) {
                                viewModel.generateDraft(message)
                            } else {
                                viewModel.sendMessage(message)
                            }
                        }
                    )
                }
            }
        }
    }

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
            AnimatedVisibility(
                visible = viewModel.isFabExtended.value,
                enter = slideInVertically(initialOffsetY = { it * 2 }),
                exit = slideOutVertically(targetOffsetY = { it * 2 })
            ) {
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(16.dp) // Puts space between buttons
                ) {
                    NewNoteButton {
                        onNoteClicked(0, viewModel.isVaultMode.value, null)
                    }
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        AskAiButton(onClick = { viewModel.toggleAiChatSheet(true) })
                    }
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
                listState = listState,
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
private fun NewNoteButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        // ---> Same with AI button colors
        containerColor = AiButtonColors.SecondaryContainer,
        contentColor = AiButtonColors.SecondaryOnContainer
        // <---
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp)) {
            Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.new_note))
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.new_note))
        }
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
            .padding(horizontal = if (settingsModel.settings.value.makeSearchBarLonger) 16.dp else 36.dp, vertical =  8.dp),
        query = query,
        placeholder = { Text(stringResource(R.string.search)) },
        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
        trailingIcon = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (query.isNotBlank()) {
                    CloseButton(contentDescription = "Clear", onCloseClicked = onClearClick)
                }
                if (settingsModel.settings.value.vaultSettingEnabled) {
                    VaultButton(viewModel.isVaultMode.value) { onVaultClicked() }
                }
                // ---> Update Check with Settings İcon
                BadgedBox(
                    badge = {
                        if (settingsModel.updateAvailable.value) {
                            Badge()
                        }
                    }
                ) {
                    IconButton(onClick = onSettingsClick) {
                        Icon(
                            imageVector = Icons.Rounded.Settings,
                            contentDescription = stringResource(R.string.screen_settings)
                        )
                    }
                }
                // <---
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
@Composable
private fun AskAiButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = AiButtonColors.GeminiContainer,
        tonalElevation = 4.dp,
        shadowElevation = 4.dp,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.AutoAwesome,
                contentDescription = "Ask AI",
                tint = AiButtonColors.GeminiOnContainer

            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = stringResource(R.string.ai_button_texts),
                style = MaterialTheme.typography.bodyLarge,
                color = AiButtonColors.GeminiOnContainer
            )
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    Row(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = if (message.participant == Participant.USER) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = when (message.participant) {
                Participant.USER -> MaterialTheme.colorScheme.primaryContainer
                Participant.MODEL -> MaterialTheme.colorScheme.surfaceVariant
                Participant.ERROR -> MaterialTheme.colorScheme.errorContainer // Hata için farklı renk
            },
            tonalElevation = 2.dp
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                if (message.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = message.text,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

// Composable with text input field and submit button
@Composable
fun ChatInputBar(
    isAwaitingTopic: Boolean,
    onSendMessage: (String) -> Unit,
) {
    var text by remember { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = text,
            onValueChange = { text = it },
            modifier = Modifier
                .weight(1f),
            placeholder = {
                Text(if (isAwaitingTopic) "Taslak konusunu yazın..." else "Ask AirNote AI...")
            },
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = {
                if (text.isNotBlank()) {
                    onSendMessage(text)
                    text = "" // Clear the field after sending the message
                }
            },
            enabled = text.isNotBlank()
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send message")
        }
    }
}

@Composable
fun DraftDisplay(draft: DraftedNote, onSave: () -> Unit, onRegenerate: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp).fillMaxSize()) {
        Text(text = draft.title, style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            item {
                Text(text = draft.content, style = MaterialTheme.typography.bodyLarge)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = onSave, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.save_note))
            }
            OutlinedButton(onClick = onRegenerate, modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.regenerate_note))
            }
        }
    }
}

@Composable
fun SuggestionItem(suggestion: AiSuggestion) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = suggestion.action)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = suggestion.icon,
            contentDescription = suggestion.title,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = suggestion.title, style = MaterialTheme.typography.bodyLarge)
    }
}