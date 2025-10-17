package com.babelsoftware.airnote.presentation.screens.home.desktop

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.rounded.LibraryBooks
import androidx.compose.material.icons.automirrored.rounded.MenuOpen
import androidx.compose.material.icons.automirrored.rounded.NoteAdd
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AllInbox
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Compress
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.FormatBold
import androidx.compose.material.icons.rounded.FormatItalic
import androidx.compose.material.icons.rounded.FormatListBulleted
import androidx.compose.material.icons.rounded.FormatQuote
import androidx.compose.material.icons.rounded.FormatUnderlined
import androidx.compose.material.icons.rounded.HorizontalRule
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material.icons.rounded.NoteAdd
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.TheaterComedy
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.data.repository.AiAction
import com.babelsoftware.airnote.data.repository.AiTone
import com.babelsoftware.airnote.domain.model.Folder
import com.babelsoftware.airnote.domain.model.Note
import com.babelsoftware.airnote.presentation.screens.home.viewmodel.HomeViewModel
import com.babelsoftware.airnote.presentation.screens.home.widgets.AddFolderDialog
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DesktopHomeScreen(
    viewModel: HomeViewModel,
    settingsModel: SettingsViewModel,
    settings: com.babelsoftware.airnote.domain.model.Settings,
    onNoteClicked: (noteId: Int, isVault: Boolean, folderId: Long?) -> Unit,
    onSettingsClicked: () -> Unit,
) {
    var showWelcomeAnimation by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(3200L) // 3,2s
        showWelcomeAnimation = false
    }

    val notes by viewModel.displayedNotes.collectAsState()
    val selectedNote by viewModel.selectedNote.collectAsState()
    val folders by viewModel.allFolders.collectAsState()
    val selectedFolderId by viewModel.selectedFolderId.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val noteForAction by viewModel.noteForAction
    val isAiChatSheetVisible by viewModel.isAiChatSheetVisible
    var isNavRailExpanded by remember { mutableStateOf(true) }
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
        DesktopAiAssistantDialog(
            viewModel = viewModel,
            onDismissRequest = {
                viewModel.toggleAiChatSheet(false)
                viewModel.resetChatState()
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
    ) {
        AnimatedVisibility(
            visible = !showWelcomeAnimation,
            enter = fadeIn(animationSpec = tween(durationMillis = 500, delayMillis = 200))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
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
        AnimatedVisibility(
            visible = showWelcomeAnimation,
            exit = fadeOut(animationSpec = tween(durationMillis = 500))
        ) {
            AnimatedWelcomeOverlay()
        }
    }
}

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
        colors = listOf(
            MaterialTheme.colorScheme.surfaceContainer,
            MaterialTheme.colorScheme.surfaceContainerLow
        )
    )

    val animatedWidth by animateDpAsState(
        targetValue = if (isExpanded) 240.dp else 80.dp,
        label = "NavRailWidthAnimation",
        animationSpec = spring(stiffness = Spring.StiffnessMedium)
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 4.dp, bottom = 16.dp)
            ) {
                IconButton(onClick = onToggleExpand) {
                    Icon(
                        imageVector = if (isExpanded) Icons.AutoMirrored.Rounded.MenuOpen else Icons.Rounded.Menu,
                        contentDescription = "Toggle Navigation Rail",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                AnimatedVisibility(visible = isExpanded) {
                    Text(
                        text = "AirNote DeX",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(start = 2.dp)
                    )
                }
            }

            NavItem(isExpanded = isExpanded, text = stringResource(R.string.new_note), icon = Icons.Rounded.Edit, onClick = onAddNewNote)
            if (aiEnabled) {
                NavItem(isExpanded = isExpanded, text = stringResource(R.string.ai_button_texts), icon = Icons.Rounded.AutoAwesome, onClick = onAskAi)
            }

            Divider(modifier = Modifier.padding(vertical = 4.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AnimatedVisibility(visible = isExpanded) {
                    Text(
                        text = stringResource(R.string.folders),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                AnimatedVisibility(visible = isExpanded) {
                    IconButton(onClick = onAddFolder, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Rounded.Add, contentDescription = "Add New Folder", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(top = 8.dp)
            ) {
                item {
                    NavItem(isExpanded = isExpanded, text = stringResource(R.string.all_notes), icon = Icons.Rounded.AllInbox, isSelected = selectedFolderId == null, onClick = { onFolderSelected(null) })
                }
                items(folders) { folder ->
                    NavItem(isExpanded = isExpanded, text = folder.name, icon = Icons.Rounded.Folder, isSelected = selectedFolderId == folder.id, onClick = { onFolderSelected(folder.id) })
                }
            }

            NavItem(isExpanded = isExpanded, text = stringResource(R.string.screen_settings), icon = Icons.Rounded.Settings, onClick = onSettingsClicked)
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
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        color = backgroundColor,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
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
                placeholder = { Text(stringResource(R.string.search_notes)) },
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
                    items(notes, key = { it.id }) { note ->
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
                                    text = { Text(stringResource(R.string.move_to_folder)) },
                                    onClick = { onMoveAction() },
                                    leadingIcon = { Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = "Move to Folder") }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.delete), color = MaterialTheme.colorScheme.error) },
                                    onClick = { onDeleteAction() },
                                    leadingIcon = { Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NoteDetailPane(
    note: Note?,
    modifier: Modifier = Modifier,
    aiEnabled: Boolean,
    onAiAction: (AiAction, AiTone?) -> Unit,
    onUpdateNote: (noteToUpdate: Note, newName: String, newDescription: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf(TextFieldValue("")) }
    var isDescriptionFocused by remember { mutableStateOf(false) }
    var showToneMenu by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = note) {
        if (note != null) {
            title = note.name
            description = TextFieldValue(note.description)
        } else {
            title = ""
            description = TextFieldValue("")
        }
    }
    LaunchedEffect(key1 = title, key2 = description) {
        if (note != null && (title != note.name || description.text != note.description)) {
            delay(750L)
            onUpdateNote(note, title, description.text)
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
                        placeholder = { Text(stringResource(R.string.title_dex)) },
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
                            .weight(1f)
                            .onFocusChanged { focusState ->
                                isDescriptionFocused = focusState.isFocused
                            },
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        placeholder = { Text(stringResource(R.string.write_your_note_here)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        )
                    )

                    AnimatedVisibility(visible = isDescriptionFocused) {
                        DesktopTextFormattingToolbar(
                            currentValue = description,
                            onValueChange = { newTextFieldValue ->
                                description = newTextFieldValue
                            }
                        )
                    }

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
                                AiActionButton(icon = Icons.Rounded.AutoAwesome, text = stringResource(R.string.ai_action_improve_writing)) {
                                    onAiAction(AiAction.IMPROVE_WRITING, null)
                                }
                                AiActionButton(icon = Icons.Rounded.Compress, text = stringResource(R.string.ai_action_summarize)) {
                                    onAiAction(AiAction.SUMMARIZE, null)
                                }
                                AiActionButton(icon = Icons.Rounded.KeyboardArrowDown, text = stringResource(R.string.ai_action_make_shorter)) {
                                    onAiAction(AiAction.MAKE_SHORTER, null)
                                }
                                AiActionButton(icon = Icons.Rounded.KeyboardArrowUp, text = stringResource(R.string.ai_action_make_longer)) {
                                    onAiAction(AiAction.MAKE_LONGER, null)
                                }
                                Box {
                                    AiActionButton(icon = Icons.Rounded.TheaterComedy, text = stringResource(R.string.ai_action_change_tone)) {
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
            imageVector = Icons.AutoMirrored.Rounded.NoteAdd,
            contentDescription = "Empty Note Box",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.no_created_notes),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onNewNoteClicked) {
            Icon(Icons.Rounded.Add, contentDescription = "New Note")
            Spacer(modifier = Modifier.width(8.dp))
            Text(stringResource(R.string.dex_create_new_note))
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
            contentDescription = "Note Not Selected",
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.dex_start),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.dex_note_preview),
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

@Composable
private fun AnimatedWelcomeOverlay() {
    var iconScale by remember { mutableStateOf(0f) }
    var textAlpha by remember { mutableStateOf(0f) }
    var textOffsetY by remember { mutableStateOf(30f) }

    val animatedIconScale by animateFloatAsState(
        targetValue = iconScale,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = 100f
        ), label = "icon_scale_anim"
    )

    val animatedTextAlpha by animateFloatAsState(
        targetValue = textAlpha,
        animationSpec = tween(durationMillis = 600, delayMillis = 200),
        label = "text_alpha_anim"
    )

    val animatedTextOffsetY by animateFloatAsState(
        targetValue = textOffsetY,
        animationSpec = tween(durationMillis = 600, delayMillis = 200),
        label = "text_offset_anim"
    )

    LaunchedEffect(Unit) {
        iconScale = 1f
        textAlpha = 1f
        textOffsetY = 0f
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_launcher_foreground),
                contentDescription = "AirNote Logo",
                modifier = Modifier
                    .size(128.dp)
                    .scale(animatedIconScale),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(animatedTextAlpha)
                    .offset(y = animatedTextOffsetY.dp)
            ) {
                Text(
                    text = stringResource(R.string.welcome_to_airnote_dex),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.airnote_dex_description),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DesktopTextFormattingToolbar(
    currentValue: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit
) {
    Surface(
        tonalElevation = 2.dp,
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically
        ) {
            item { IconButton(onClick = { onValueChange(currentValue.applyMarkdown("**", "**")) }) { Icon(Icons.Rounded.FormatBold, "Thick") } }
            item { IconButton(onClick = { onValueChange(currentValue.applyMarkdown("*", "*")) }) { Icon(Icons.Rounded.FormatItalic, "Italic") } }
            item { IconButton(onClick = { onValueChange(currentValue.applyMarkdown("<u>", "</u>")) }) { Icon(Icons.Rounded.FormatUnderlined, "Underlined") } }
            item { IconButton(onClick = { onValueChange(currentValue.applyMarkdown("# ", isLinePrefix = true)) }) { Icon(Icons.Rounded.Title, "Title") } }
            item { IconButton(onClick = { onValueChange(currentValue.applyMarkdown("\n- ", "")) }) { Icon(Icons.Rounded.FormatListBulleted, "List") } }
            item { IconButton(onClick = { onValueChange(currentValue.applyMarkdown("> ", isLinePrefix = true)) }) { Icon(Icons.Rounded.FormatQuote, "Quote") } }
            item { IconButton(onClick = { onValueChange(currentValue.applyMarkdown("`", "`")) }) { Icon(Icons.Rounded.Code, "Code") } }
            item { IconButton(onClick = { onValueChange(currentValue.applyMarkdown("\n---\n", "")) }) { Icon(Icons.Rounded.HorizontalRule, "Bracket") } }
            item { IconButton(onClick = { onValueChange(currentValue.applyMarkdown("[", "]()")) }) { Icon(Icons.Rounded.Link, "Link") } }
        }
    }
}

private fun TextFieldValue.applyMarkdown(
    prefix: String,
    suffix: String = "",
    isLinePrefix: Boolean = false
): TextFieldValue {
    val selection = this.selection
    val text = this.text

    if (isLinePrefix) {
        var lineStart = selection.start
        while (lineStart > 0 && text.getOrNull(lineStart - 1) != '\n') {
            lineStart--
        }
        val newText = text.substring(0, lineStart) + prefix + text.substring(lineStart)
        return this.copy(
            text = newText,
            selection = TextRange(selection.end + prefix.length)
        )
    } else if (selection.collapsed) {
        val newText = text.substring(0, selection.start) + prefix + suffix + text.substring(selection.end)
        return this.copy(
            text = newText,
            selection = TextRange(selection.start + prefix.length)
        )
    } else {
        val selectedText = text.substring(selection.min, selection.max)
        val newText = text.replaceRange(
            selection.min,
            selection.max,
            prefix + selectedText + suffix
        )
        return this.copy(
            text = newText,
            selection = TextRange(selection.end + prefix.length + suffix.length)
        )
    }
}
