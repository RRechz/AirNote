package com.babelsoftware.airnote.presentation.screens.home.desktop

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.data.repository.AiAction
import com.babelsoftware.airnote.data.repository.AiTone
import com.babelsoftware.airnote.domain.model.Folder
import com.babelsoftware.airnote.domain.model.Note
import com.babelsoftware.airnote.domain.model.Participant
import com.babelsoftware.airnote.presentation.screens.home.*
import com.babelsoftware.airnote.presentation.screens.home.viewmodel.HomeViewModel
import com.babelsoftware.airnote.presentation.screens.home.widgets.AddFolderDialog
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopHomeScreen(
    viewModel: HomeViewModel,
    settingsModel: SettingsViewModel,
    settings: com.babelsoftware.airnote.domain.model.Settings,
    onNoteClicked: (noteId: Int, isVault: Boolean, folderId: Long?) -> Unit,
    onSettingsClicked: () -> Unit,
) {
    val notes by viewModel.displayedNotes.collectAsState()
    val selectedNote by viewModel.selectedNote.collectAsState()
    val folders by viewModel.allFolders.collectAsState()
    val selectedFolderId by viewModel.selectedFolderId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val noteForAction by viewModel.noteForAction
    val isAiChatSheetVisible by viewModel.isAiChatSheetVisible
    val chatState by viewModel.chatState
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var isNavRailExpanded by remember { mutableStateOf(false) }
    val isAddFolderDialogVisible by viewModel.isAddFolderDialogVisible

    if (isAddFolderDialogVisible) {
        AddFolderDialog(
            onDismiss = { viewModel.setAddFolderDialogVisibility(false) },
            onConfirm = { name, color ->
                viewModel.addFolder(name, color)
            }
        )
    }

    if (isAiChatSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = {
                viewModel.toggleAiChatSheet(false)
                viewModel.resetChatState()
            },
            sheetState = sheetState,
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.Transparent
        ) {
            val isDark = isSystemInDarkTheme()
            val gradientBrush = Brush.verticalGradient(
                colors = if (isDark) {
                    listOf(Color(0xFF282322), Color(0xFF121011))
                } else {
                    listOf(
                        MaterialTheme.colorScheme.surfaceContainer,
                        MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                }
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(gradientBrush)
            ) {
                when {
                    chatState.latestDraft != null -> DraftDisplay(
                        draft = chatState.latestDraft!!,
                        onSave = { viewModel.saveDraftedNote() },
                        onRegenerate = { viewModel.regenerateDraft() }
                    )
                    chatState.messages.isNotEmpty() -> {
                        var text by remember { mutableStateOf("") }
                        val isLoading =
                            chatState.messages.lastOrNull()?.isLoading == true
                        val haptic = LocalHapticFeedback.current
                        val listState = rememberLazyListState()
                        val scope = rememberCoroutineScope()

                        LaunchedEffect(chatState.messages.size) {
                            if (chatState.messages.isNotEmpty()) {
                                scope.launch {
                                    listState.animateScrollToItem(chatState.messages.size - 1)
                                }
                                if (chatState.messages.last().participant == Participant.MODEL) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }
                        }

                        Column(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(horizontal = 8.dp),
                                state = listState
                            ) {
                                items(chatState.messages) { message ->
                                    AnimatedVisibility(
                                        visible = true,
                                        enter = slideInHorizontally { fullWidth ->
                                            if (message.participant == Participant.USER) fullWidth else -fullWidth
                                        } + fadeIn()
                                    ) {
                                        ChatMessageItem(message = message)
                                    }
                                }
                            }
                            ChatInputBar(
                                text = text,
                                onValueChange = { text = it },
                                isAwaitingTopic = chatState.isAwaitingDraftTopic,
                                onSendMessage = {
                                    val messageToSend = text
                                    if (messageToSend.isNotBlank()) {
                                        if (chatState.isAwaitingDraftTopic) {
                                            viewModel.generateDraft(messageToSend)
                                        } else {
                                            viewModel.sendMessage(messageToSend)
                                        }
                                        text = ""
                                    }
                                },
                                onImagePickerClicked = { viewModel.requestImageForAnalysis() },
                                enabled = !isLoading
                            )
                        }
                    }
                    else -> NewAiScreen(
                        isAwaitingTopic = chatState.isAwaitingDraftTopic,
                        isLoading = chatState.messages.lastOrNull()?.isLoading == true,
                        onSendMessage = { message ->
                            if (chatState.isAwaitingDraftTopic) {
                                viewModel.generateDraft(message)
                            } else {
                                viewModel.sendMessage(message)
                            }
                        },
                        suggestions = viewModel.suggestions,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        DesktopNavRail(
            isExpanded = isNavRailExpanded,
            onToggleExpand = { isNavRailExpanded = !isNavRailExpanded },
            aiEnabled = settings.desktopModeAiEnabled,
            folders = folders,
            selectedFolderId = selectedFolderId,
            onFolderSelected = { folderId -> viewModel.selectFolder(folderId) },
            onAddNewNote = { viewModel.createNewNoteForDesktop() },
            onAddFolder = { viewModel.setAddFolderDialogVisibility(true) },
            onAskAi = { viewModel.toggleAiChatSheet(true) },
            onSettingsClicked = onSettingsClicked
        )

        NoteListPane(
            notes = notes,
            selectedNote = selectedNote,
            searchQuery = searchQuery,
            onQueryChange = { query -> viewModel.changeSearchQuery(query) },
            onNoteSelected = { note -> viewModel.selectNote(note) },
            onNewNoteClicked = { viewModel.createNewNoteForDesktop() },
            noteForAction = noteForAction,
            onNoteLongPressed = { note -> viewModel.onNoteLongPressed(note) },
            onDismissNoteAction = { viewModel.onDismissNoteAction() },
            onDeleteAction = { viewModel.deleteNoteAction() },
            onMoveAction = { viewModel.requestMoveNoteAction() },
            modifier = Modifier.weight(0.4f)
        )

        AnimatedContent(
            targetState = selectedNote,
            modifier = Modifier.weight(0.6f),
            transitionSpec = {
                (fadeIn(animationSpec = tween(300, easing = LinearOutSlowInEasing)) +
                        slideInVertically(animationSpec = tween(300, easing = LinearOutSlowInEasing), initialOffsetY = { it / 8 }))
                    .togetherWith(fadeOut(animationSpec = tween(300, easing = LinearOutSlowInEasing)))
            },
            label = "NoteDetailAnimation"
        ) { targetNote ->
            NoteDetailPane(
                note = targetNote,
                aiEnabled = settings.desktopModeAiEnabled,
                onAiAction = { action, tone -> viewModel.executeDesktopAiAction(action, tone) },
                onUpdateNote = { noteToUpdate, newName, newDescription ->
                    viewModel.updateNoteDetails(noteToUpdate, newName, newDescription)
                }
            )
        }
    }
}

// Left (Vertical Nav) Panel
@Composable
private fun DesktopNavRail(
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    aiEnabled: Boolean,
    folders: List<Folder>,
    selectedFolderId: Long?,
    onFolderSelected: (Long?) -> Unit,
    onAddNewNote: () -> Unit,
    onAddFolder: () -> Unit,
    onAskAi: () -> Unit,
    onSettingsClicked: () -> Unit
) {
    val navRailBackground = Brush.verticalGradient(
        colors = listOf(Color(0xFF0A3D2A), Color(0xFF1A5A41))
    )

    // Animated state for the left panel width
    val animatedWidth by animateDpAsState(
        targetValue = if (isExpanded) 240.dp else 80.dp,
        label = "NavRailWidthAnimation"
    )

    Surface(
        color = Color.Transparent,
        modifier = Modifier
            .fillMaxHeight()
            .width(animatedWidth)
            .background(navRailBackground)
            .padding(vertical = 10.dp, horizontal = 12.dp)
    ) {
        Column(horizontalAlignment = Alignment.Start) {
            // --- FIXED TOP SECTION
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp, bottom = 16.dp)
            ) {
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.AutoMirrored.Rounded.MenuOpen else Icons.Rounded.Menu,
                        contentDescription = "Toggle Navigation Rail",
                        tint = Color.White
                    )
                }
                AnimatedVisibility(visible = isExpanded) {
                    Text(
                        text = "AirNote Desktop BETA",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }

            NavItem(isExpanded = isExpanded, text = stringResource(R.string.new_note), icon = Icons.Rounded.Edit, onClick = onAddNewNote)
            if (aiEnabled) {
                NavItem(isExpanded = isExpanded, text = stringResource(R.string.ai_button_texts), icon = Icons.Rounded.AutoAwesome, onClick = onAskAi)
            }
            // <---

            Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color.White.copy(alpha = 0.2f))

            // ---> FOLDER SECTION
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AnimatedVisibility(visible = isExpanded) {
                    Text(
                        text = "Folders",
                        style = MaterialTheme.typography.titleSmall,
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
                AnimatedVisibility(visible = isExpanded) {
                    IconButton(onClick = onAddFolder, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Rounded.Add, contentDescription = "Yeni Klasör Ekle", tint = Color.White)
                    }
                }
            }

            // Scrolling area of folders list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(32.dp),
                contentPadding = PaddingValues(top = 8.dp)
            ) {
                item {
                    NavItem(isExpanded = isExpanded, text = "All Notes", icon = Icons.Rounded.AllInbox, isSelected = selectedFolderId == null, onClick = { onFolderSelected(null) })
                }
                items(folders) { folder ->
                    NavItem(isExpanded = isExpanded, text = folder.name, icon = Icons.Rounded.Folder, isSelected = selectedFolderId == folder.id, onClick = { onFolderSelected(folder.id) })
                }
            }
            // <---

            // ---> SETTİNGS SUBSECTION
            NavItem(isExpanded = isExpanded, text = stringResource(R.string.screen_settings), icon = Icons.Rounded.Settings, onClick = onSettingsClicked)
            // <---
        }
    }
}

@Composable
private fun NavItem(
    isExpanded: Boolean,
    text: String,
    icon: ImageVector,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.25f) else Color.Transparent
    val contentColor = Color.White

    Surface(
        onClick = onClick,
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().height(56.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp)
        ) {
            Icon(imageVector = icon, contentDescription = text, tint = contentColor)
            AnimatedVisibility(visible = isExpanded) {
                Row {
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(text = text, color = contentColor, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

/*
 * Center panel: The area where the notes are listed
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun NoteListPane(
    notes: List<Note>,
    selectedNote: Note?,
    searchQuery: String,
    onQueryChange: (String) -> Unit,
    onNoteSelected: (Note) -> Unit,
    onNewNoteClicked: () -> Unit,
    noteForAction: Note?,
    onNoteLongPressed: (Note) -> Unit,
    onDismissNoteAction: () -> Unit,
    onDeleteAction: () -> Unit,
    onMoveAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxHeight()
            .padding(top = 16.dp, bottom = 16.dp, start = 16.dp)
            .shadow(elevation = 8.dp, shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp)),
        shape = RoundedCornerShape(topStart = 24.dp, bottomStart = 24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onQueryChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                )
            )

            if (notes.isEmpty() && searchQuery.isEmpty()) {
                EmptyNotesView(onNewNoteClicked = onNewNoteClicked)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp)
                ) {
                    items(notes) { note ->
                        val isSelected = selectedNote?.id == note.id
                        Box {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp)
                                    .shadow(elevation = if (isSelected) 4.dp else 0.dp, shape = RoundedCornerShape(16.dp))
                                    .combinedClickable(
                                        onClick = { onNoteSelected(note) },
                                        onLongClick = { onNoteLongPressed(note) }
                                    ),
                                shape = RoundedCornerShape(16.dp),
                                border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                colors = if (isSelected) {
                                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                                } else {
                                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                                }
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Text(text = note.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = note.description.take(120),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 3
                                    )
                                }
                            }
                            DropdownMenu(
                                expanded = noteForAction?.id == note.id,
                                onDismissRequest = { onDismissNoteAction() }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Move to Folder") },
                                    onClick = { onMoveAction() },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = "Klasöre Taşı") }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete", color = MaterialTheme.colorScheme.error) },
                                    onClick = { onDeleteAction() },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Sil", tint = MaterialTheme.colorScheme.error) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/*
 * Right panel: Area where the content of the selected note is shown
 */
@Composable
private fun NoteDetailPane(
    note: Note?,
    modifier: Modifier = Modifier,
    aiEnabled: Boolean,
    onAiAction: (AiAction, AiTone?) -> Unit,
    onUpdateNote: (noteToUpdate: Note, newName: String, newDescription: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showToneMenu by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = note) {
        if (note != null) {
            title = note.name
            description = note.description
        } else {
            title = ""
            description = ""
        }
    }
    LaunchedEffect(key1 = title, key2 = description) {
        if (note != null && (title != note.name || description != note.description)) {
            delay(750L)
            onUpdateNote(note, title, description)
        }
    }

    Box(
        modifier = modifier.padding(top = 16.dp, bottom = 16.dp, end = 16.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 800.dp)
                .fillMaxHeight()
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp)),
            shape = RoundedCornerShape(topEnd = 24.dp, bottomEnd = 24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            if (note != null) {
                Column(modifier = Modifier.fillMaxSize()) {
                    TextField(
                        value = title,
                        onValueChange = { title = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.headlineMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        placeholder = { Text("Title...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )
                    Divider(modifier = Modifier.padding(horizontal = 16.dp))
                    TextField(
                        value = description,
                        onValueChange = { description = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        placeholder = { Text("Write your note here...") },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    if (aiEnabled) {
                        Surface(
                            tonalElevation = 3.dp,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AiActionButton(icon = Icons.Rounded.AutoAwesome, text = "Enhance") {
                                    onAiAction(AiAction.IMPROVE_WRITING, null)
                                }
                                AiActionButton(icon = Icons.Rounded.Compress, text = "Summarize") {
                                    onAiAction(AiAction.SUMMARIZE, null)
                                }
                                AiActionButton(icon = Icons.Rounded.KeyboardArrowDown, text = "Make Shorter") {
                                    onAiAction(AiAction.MAKE_SHORTER, null)
                                }
                                AiActionButton(icon = Icons.Rounded.KeyboardArrowUp, text = "Make Longer") {
                                    onAiAction(AiAction.MAKE_LONGER, null)
                                }
                                Box {
                                    AiActionButton(icon = Icons.Rounded.TheaterComedy, text = "Change Tone") {
                                        showToneMenu = true
                                    }
                                    DropdownMenu(
                                        expanded = showToneMenu,
                                        onDismissRequest = { showToneMenu = false }
                                    ) {
                                        AiTone.values().forEach { tone ->
                                            DropdownMenuItem(
                                                text = { Text(tone.name.replaceFirstChar { it.titlecase() }) },
                                                onClick = {
                                                    onAiAction(AiAction.CHANGE_TONE, tone)
                                                    showToneMenu = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                EmptySelectionView()
            }
        }
    }
}

@Composable
private fun EmptyNotesView(onNewNoteClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.NoteAdd,
            contentDescription = "Boş Not Kutusu",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "No note yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "To get started, create a new note.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onNewNoteClicked) {
            Icon(Icons.Rounded.Add, contentDescription = "Yeni Not")
            Spacer(modifier = Modifier.width(8.dp))
            Text("Create New Note")
        }
    }
}

@Composable
private fun EmptySelectionView() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Rounded.LibraryBooks,
            contentDescription = "Not Seçilmedi",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Get Started!",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Select a note from the list on the left to view or edit it.",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun AiActionButton(icon: ImageVector, text: String, onClick: () -> Unit) {
    TextButton(onClick = onClick) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = text)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = text, style = MaterialTheme.typography.labelSmall)
        }
    }
}