package com.babelsoftware.airnote.presentation.screens.home

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CardGiftcard
import androidx.compose.material.icons.filled.Circle
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Extension
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SmallFloatingActionButton
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
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.domain.model.AiSuggestion
import com.babelsoftware.airnote.domain.model.ChatMessage
import com.babelsoftware.airnote.domain.model.Folder
import com.babelsoftware.airnote.domain.model.Note
import com.babelsoftware.airnote.domain.model.Participant
import com.babelsoftware.airnote.domain.model.Settings
import com.babelsoftware.airnote.presentation.components.CloseButton
import com.babelsoftware.airnote.presentation.components.DeleteButton
import com.babelsoftware.airnote.presentation.components.NotesScaffold
import com.babelsoftware.airnote.presentation.components.PinButton
import com.babelsoftware.airnote.presentation.components.SelectAllButton
import com.babelsoftware.airnote.presentation.components.TitleText
import com.babelsoftware.airnote.presentation.components.UpdateScreen
import com.babelsoftware.airnote.presentation.components.VaultButton
import com.babelsoftware.airnote.presentation.components.defaultScreenEnterAnimation
import com.babelsoftware.airnote.presentation.components.defaultScreenExitAnimation
import com.babelsoftware.airnote.presentation.screens.home.desktop.DesktopHomeScreen
import com.babelsoftware.airnote.presentation.screens.home.viewmodel.DraftedNote
import com.babelsoftware.airnote.presentation.screens.home.viewmodel.HomeViewModel
import com.babelsoftware.airnote.presentation.screens.home.widgets.FolderActionBottomSheet
import com.babelsoftware.airnote.presentation.screens.home.widgets.MoveToFolderDialog
import com.babelsoftware.airnote.presentation.screens.home.widgets.NoteFilter
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.presentation.screens.settings.settings.PasswordPrompt
import com.babelsoftware.airnote.presentation.screens.settings.settings.shapeManager
import com.babelsoftware.airnote.presentation.theme.AiButtonColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class, androidx.compose.animation.ExperimentalAnimationApi::class)
@Composable
fun HomeView (
    viewModel: HomeViewModel = hiltViewModel(),
    settingsModel: SettingsViewModel,
    settings: Settings,
    onSettingsClicked: () -> Unit,
    onNoteClicked: (noteId: Int, isVault: Boolean, folderId: Long?) -> Unit,
    onNavigateToAbout: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    if (activity != null) {
        val windowSizeClass = calculateWindowSizeClass(activity)
        val widthSizeClass = windowSizeClass.widthSizeClass
        val isCompact = widthSizeClass == WindowWidthSizeClass.Compact
        val shouldShowDesktopUi = !isCompact && settings.desktopModeEnabled

        if (shouldShowDesktopUi) {
            DesktopHomeScreen(
                viewModel = viewModel,
                settingsModel = settingsModel,
                settings = settings,
                onNoteClicked = onNoteClicked,
                onSettingsClicked = onSettingsClicked
            )
        } else {
            val allFolders by viewModel.allFolders.collectAsState()
            val notes by viewModel.displayedNotes.collectAsState()
            val settings = settingsModel.settings.value
            val selectedFolderId = viewModel.selectedFolderId.value
            var showFolderSheet by remember { mutableStateOf(false) }
            val selectedFolder = remember(selectedFolderId, allFolders) {
                allFolders.find { it.id == selectedFolderId }
            }
            val context = LocalContext.current
            // ---> Image Picker (Photo Picker)
            val imagePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent(),
                onResult = { uri: Uri? ->
                    if (uri != null) {
                        viewModel.analyzeImageAndCreateDraft(uri)
                    }
                }
            )

            LaunchedEffect(Unit) {
                viewModel.uiActionChannel.collect { action ->
                    when (action) {
                        is HomeViewModel.UiAction.RequestImageForAnalysis -> {
                            imagePickerLauncher.launch("image/*")
                        }
                    }
                }
            }
            // <---

            LaunchedEffect(key1 = Unit) {
                settingsModel.checkForNewUpdate(context)
            }

            if (showFolderSheet) {
                FolderSelectionBottomSheet(
                    allFolders = allFolders,
                    selectedFolderId = selectedFolderId,
                    onDismiss = { showFolderSheet = false },
                    onFolderSelected = { folderId ->
                        viewModel.selectFolder(folderId)
                        showFolderSheet = false
                    },
                    onAddFolderClicked = {
                        viewModel.setAddFolderDialogVisibility(true)
                    },
                    onFolderLongClick = { folder ->
                        viewModel.onFolderLongPressed(folder)
                    }
                )
            }

            if (settingsModel.showUpdateDialog.value) {
                UpdateScreen(
                    latestVersion = settingsModel.latestVersion.value,
                    onDismiss = { settingsModel.dismissUpdateDialog() },
                    onNavigateToAbout = onNavigateToAbout
                )
            }
            val topBarColor = MaterialTheme.colorScheme.surfaceContainerLow
            val listState =
                rememberLazyStaggeredGridState() // LazyListState to listen to the scrolling state of the note list

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
                    containerColor = Color.Transparent // Variable color for the AI window
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
                        val chatState = viewModel.chatState.value

                        if (chatState.latestDraft != null) {
                            DraftDisplay(
                                draft = chatState.latestDraft,
                                onSave = { viewModel.saveDraftedNote() },
                                onRegenerate = { viewModel.regenerateDraft() }
                            )
                        } else if (chatState.messages.isNotEmpty()) {
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
                                    items(items = chatState.messages, key = { it.hashCode() }) { message ->
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
                        } else {
                            NewAiScreen(
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
                AddFolderDialogWithIcons(
                    onDismiss = { viewModel.setAddFolderDialogVisibility(false) },
                    onConfirm = { name, iconName ->
                        viewModel.addFolder(name, iconName)
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
                AddFolderDialogWithIcons(
                    onDismiss = { viewModel.onDismissEditFolderDialog() },
                    onConfirm = { name, iconName ->
                        viewModel.updateFolder(name, iconName)
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
                        text = {
                            Text(
                                stringResource(
                                    R.string.delete_folder_confirmation,
                                    folder.name
                                )
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = { viewModel.confirmFolderDeletion() }) {
                                Text(
                                    stringResource(R.string.delete),
                                    color = MaterialTheme.colorScheme.error
                                )
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
            val sortedNotes = remember(notes, settings.sortDescending) {
                notes.sortedWith(sorter(settings.sortDescending))
            }

            NotesScaffold(
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                floatingActionButton = {
                    AnimatedVisibility(
                        visible = viewModel.isFabExtended.value,
                        enter = slideInVertically(initialOffsetY = { it * 2 }),
                        exit = slideOutVertically(targetOffsetY = { it * 2 })
                    ) {
                        MultiActionFloatingActionButton(
                            onNewNoteClicked = { onNoteClicked(0, viewModel.isVaultMode.value, null) },
                            onAskAiClicked = { viewModel.toggleAiChatSheet(true) }
                        )
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
                            onMoveToFolderClick = {
                                viewModel.setMoveToFolderDialogVisibility(
                                    true
                                )
                            },
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
                            val query by viewModel.searchQuery.collectAsState()
                            NotesSearchBar(
                                settingsModel = settingsModel,
                                query = query,
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
                                },
                                selectedFolderName = selectedFolder?.name ?: stringResource(R.string.all_notes),
                                selectedFolderIconName = selectedFolder?.iconName,
                                onFoldersClicked = { showFolderSheet = true }
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
                        notes = sortedNotes,
                        allFolders = allFolders,
                        onNoteClicked = { noteId ->
                            val clickedNote =
                                viewModel.displayedNotes.value.find { it.id == noteId }
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
                            scope.launch(Dispatchers.IO) {
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
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Composable
@ReadOnlyComposable
fun getContainerColor(settingsModel: SettingsViewModel): Color {
    return if (settingsModel.settings.value.extremeAmoledMode) Color.Black else MaterialTheme.colorScheme.surfaceContainerHigh
}

@Composable
private fun MultiActionFloatingActionButton(
    onNewNoteClicked: () -> Unit,
    onAskAiClicked: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(false) }
    val transition = updateTransition(targetState = isExpanded, label = "fab_transition")
    val mainButtonRotation by transition.animateFloat(
        label = "fab_rotation",
        transitionSpec = { spring(stiffness = Spring.StiffnessMedium) }
    ) { expanded ->
        if (expanded) 45f else 0f
    }
    val secondaryButtonAlpha by transition.animateFloat(
        label = "fab_alpha",
        transitionSpec = { spring(stiffness = Spring.StiffnessMedium) }
    ) { expanded ->
        if (expanded) 1f else 0f
    }
    val secondaryButtonScale by transition.animateFloat(
        label = "fab_scale",
        transitionSpec = { spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow) }
    ) { expanded ->
        if (expanded) 1f else 0.5f
    }

    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallFloatingActionButton(
                    onClick = {
                        onAskAiClicked()
                        isExpanded = false
                    },
                    containerColor = AiButtonColors.GeminiContainer,
                    modifier = Modifier.graphicsLayer {
                        alpha = secondaryButtonAlpha
                        scaleX = secondaryButtonScale
                        scaleY = secondaryButtonScale
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = stringResource(R.string.ai_button_texts),
                        tint = AiButtonColors.GeminiOnContainer
                    )
                }
                SmallFloatingActionButton(
                    onClick = {
                        onNewNoteClicked()
                        isExpanded = false
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.graphicsLayer {
                        alpha = secondaryButtonAlpha
                        scaleX = secondaryButtonScale
                        scaleY = secondaryButtonScale
                    }
                ) {
                    Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.new_note))
                }
            }
        }

        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            containerColor = MaterialTheme.colorScheme.primary,
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Add",
                modifier = Modifier.rotate(mainButtonRotation)
            )
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
                    Icon(Icons.Default.FolderOpen, contentDescription = "Move to Folder")
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
    onClearClick: () -> Unit,
    selectedFolderName: String,
    selectedFolderIconName: String?,
    onFoldersClicked: () -> Unit
) {
    SearchBar(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = if (settingsModel.settings.value.makeSearchBarLonger) 16.dp else 36.dp,
                vertical = 8.dp
            ),
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
                AnimatedVisibility(
                    visible = query.isBlank(),
                    enter = fadeIn() + slideInHorizontally { it },
                    exit = fadeOut() + slideOutHorizontally { it }
                ) {
                    Button(
                        onClick = onFoldersClicked,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier
                            .widthIn(max = 120.dp)
                            .padding(horizontal = 4.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp)
                    ) {
                        Icon(
                            imageVector = selectedFolderIconName?.let { iconName ->
                                iconNameToVector(iconName)
                            } ?: Icons.Default.FolderOpen,
                            contentDescription = "Folders",
                            modifier = Modifier.size(18.dp)
                        )

                        if (selectedFolderName != stringResource(R.string.all_notes)) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = selectedFolderName,
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                modifier = Modifier.basicMarquee(
                                    iterations = Int.MAX_VALUE,
                                    initialDelayMillis = 1000
                                )
                            )
                        }
                    }
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FolderSelectionBottomSheet(
    allFolders: List<Folder>,
    selectedFolderId: Long?,
    onDismiss: () -> Unit,
    onFolderSelected: (Long?) -> Unit,
    onAddFolderClicked: () -> Unit,
    onFolderLongClick: (Folder) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var searchQuery by remember { mutableStateOf("") }

    val filteredFolders = remember(searchQuery, allFolders) {
        if (searchQuery.isBlank()) {
            allFolders
        } else {
            allFolders.filter { folder ->
                folder.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        scrimColor = Color.Black.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            // Folder Search Bar
            TextField(
                value = searchQuery,
                onValueChange = { newQuery -> searchQuery = newQuery },
                placeholder = { Text(stringResource(R.string.search_folders)) },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = CircleShape,
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )

            // Folder list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                contentPadding = PaddingValues(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item {
                    FolderListItem(
                        name = stringResource(R.string.all_notes),
                        iconName = "Inbox",
                        isSelected = selectedFolderId == null,
                        onClick = { onFolderSelected(null) }
                    )
                }
                items(items = filteredFolders, key = { it.id }) { folder ->
                    FolderListItem(
                        name = folder.name,
                        iconName = folder.iconName,
                        isSelected = selectedFolderId == folder.id,
                        onClick = { onFolderSelected(folder.id) },
                        onLongClick = { onFolderLongClick(folder) }
                    )
                }

                item {
                    // "Add New Folder" button
                    Button(
                        onClick = onAddFolderClicked,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Add New Folder")
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(stringResource(R.string.add_new_folder))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderListItem(
    name: String,
    iconName: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val selectedBorder = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    val cardColors = CardDefaults.cardColors(
        containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(selectedBorder ?: BorderStroke(0.dp, Color.Transparent), RoundedCornerShape(16.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(16.dp),
        colors = cardColors
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Show icon
            Icon(
                imageVector = iconNameToVector(iconName),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}

@Composable
fun AddFolderDialogWithIcons(
    onDismiss: () -> Unit,
    onConfirm: (name: String, iconName: String) -> Unit,
    folderToEdit: Folder? = null
) {
    var name by remember { mutableStateOf(folderToEdit?.name ?: "") }
    var selectedIconName by remember { mutableStateOf(folderToEdit?.iconName ?: "Folder") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (folderToEdit == null) stringResource(R.string.new_folder) else stringResource(R.string.edit_folder)) },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { newName -> name = newName },
                    label = { Text(stringResource(R.string.folder_name)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(stringResource(R.string.choose_icon), style = MaterialTheme.typography.labelLarge)
                Spacer(modifier = Modifier.height(8.dp))
                IconPicker(
                    selectedIconName = selectedIconName,
                    onIconSelected = { selectedIconName = it }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(name, selectedIconName)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Composable
fun IconPicker(
    selectedIconName: String,
    onIconSelected: (String) -> Unit
) {
    val iconList = remember { materialIconsList.keys.toList() }
    LazyVerticalGrid(
        columns = GridCells.Adaptive(minSize = 48.dp),
        modifier = Modifier.heightIn(max = 200.dp)
    ) {
        items(items = iconList, key = { it }) { iconName ->
            val isSelected = selectedIconName == iconName
            IconButton(
                onClick = { onIconSelected(iconName) },
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                )
            ) {
                Icon(
                    imageVector = materialIconsList.getValue(iconName),
                    contentDescription = iconName,
                    tint = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

fun iconNameToVector(name: String): ImageVector {
    return materialIconsList[name] ?: Icons.Default.Folder
}

val materialIconsList = mapOf(
    "Folder" to Icons.Default.Folder,
    "Bookmark" to Icons.Default.Bookmark,
    "Favorite" to Icons.Default.Favorite,
    "Home" to Icons.Default.Home,
    "Star" to Icons.Default.Star,
    "Event" to Icons.Default.Event,
    "Work" to Icons.Default.Work,
    "School" to Icons.Default.School,
    "Person" to Icons.Default.Person,
    "Group" to Icons.Default.Group,
    "ShoppingCart" to Icons.Default.ShoppingCart,
    "AttachMoney" to Icons.Default.AttachMoney,
    "FitnessCenter" to Icons.Default.FitnessCenter,
    "Travel" to Icons.Default.Flight,
    "MusicNote" to Icons.Default.MusicNote,
    "Movie" to Icons.Default.Movie,
    "Book" to Icons.Default.Book,
    "Code" to Icons.Default.Code,
    "Cloud" to Icons.Default.Cloud,
    "Lightbulb" to Icons.Default.Lightbulb,
    "Pets" to Icons.Default.Pets,
    "Build" to Icons.Default.Build,
    "Palette" to Icons.Default.Palette,
    "CardGiftcard" to Icons.Default.CardGiftcard,
    "DirectionsCar" to Icons.Default.DirectionsCar,

    // Extra İcons
    "AccountBalance" to Icons.Default.AccountBalance,
    "Alarm" to Icons.Default.Alarm,
    "Apartment" to Icons.Default.Apartment,
    "Assessment" to Icons.Default.Assessment,
    "Backup" to Icons.Default.Backup,
    "Cake" to Icons.Default.Cake,
    "CameraAlt" to Icons.Default.CameraAlt,
    "Campaign" to Icons.Default.Campaign,
    "Chat" to Icons.AutoMirrored.Filled.Chat,
    "Circle" to Icons.Default.Circle,
    "ContentCut" to Icons.Default.ContentCut,
    "Eco" to Icons.Default.Eco,
    "Extension" to Icons.Default.Extension,
    "Flag" to Icons.Default.Flag,
    "Headset" to Icons.Default.Headset,
    "Key" to Icons.Default.Key,
    "Link" to Icons.Default.Link,
    "Lock" to Icons.Default.Lock,
    "Map" to Icons.Default.Map,
    "PushPin" to Icons.Default.PushPin,
    "Receipt" to Icons.Default.Receipt,
    "Restaurant" to Icons.Default.Restaurant,
    "Shield" to Icons.Default.Shield,
    "Spa" to Icons.Default.Spa,
    "SportsEsports" to Icons.Default.SportsEsports,
    "Store" to Icons.Default.Store,
    "ThumbUp" to Icons.Default.ThumbUp,
    "Visibility" to Icons.Default.Visibility,
    "Warning" to Icons.Default.Warning
)

@Composable
fun ChatMessageItem(message: ChatMessage) {
    Row(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = if (message.participant == Participant.USER) Arrangement.End else Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = when (message.participant) {
                Participant.USER -> MaterialTheme.colorScheme.primaryContainer
                Participant.MODEL -> MaterialTheme.colorScheme.surfaceVariant
                Participant.ERROR -> MaterialTheme.colorScheme.errorContainer
            },
            tonalElevation = 1.dp
        ) {
            Box(modifier = Modifier.padding(12.dp)) {
                if (message.isLoading) {
                    TypingIndicator()
                } else {
                    Text(
                        text = message.text.cleanMarkdown(),
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
    text: String,
    onValueChange: (String) -> Unit,
    isAwaitingTopic: Boolean,
    onSendMessage: () -> Unit,
    onImagePickerClicked: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onFocusChanged: (isFocused: Boolean) -> Unit = {}
) {
    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Add image button
        IconButton(onClick = onImagePickerClicked, enabled = enabled) {
            Icon(
                Icons.Default.Image,
                contentDescription = "Add İmage",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        TextField(
            value = text,
            onValueChange = onValueChange,
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { focusState ->
                    onFocusChanged(focusState.isFocused)
                },
            enabled = enabled,
            placeholder = {
                Text(if (isAwaitingTopic) "Write the draft topic..." else "Ask AirNote AI...")
            },
            shape = CircleShape,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        AnimatedContent(
            targetState = enabled,
            transitionSpec = {
                (fadeIn(animationSpec = tween(220, delayMillis = 90)) +
                        scaleIn(initialScale = 0.92f, animationSpec = tween(220, delayMillis = 90)))
                    .togetherWith(fadeOut(animationSpec = tween(90)))
            },
            label = "send_button_animation"
        ) { isEnabled ->
            if (!isEnabled) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
            } else {
                IconButton(
                    onClick = {
                        if (text.isNotBlank()) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSendMessage()
                        }
                    },
                    enabled = text.isNotBlank()
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send message")
                }
            }
        }
    }
}

@Composable
fun DraftDisplay(draft: DraftedNote, onSave: () -> Unit, onRegenerate: () -> Unit) {
    Column(modifier = Modifier
        .padding(16.dp)
        .fillMaxSize()) {
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

// =================================================================
// === New AI Window Design ========================================
// =================================================================

/**
 * Modern, theme compatible AI start screen with text input.
 * @param isAwaitingTopic Information from ViewModel whether a draft topic is waiting.
 * @param onSendMessage Lambda function to be triggered when the user sends a message.
 */
@Composable
fun NewAiScreen(
    isAwaitingTopic: Boolean,
    isLoading: Boolean,
    onSendMessage: (String) -> Unit,
    suggestions: List<AiSuggestion>,
    viewModel: HomeViewModel
) {
    var isFocused by remember { mutableStateOf(false) }
    var text by remember { mutableStateOf("") }
    val suggestionAnimatables = List(6) { remember { mutableStateOf(false) } }

    LaunchedEffect(Unit) {
        suggestionAnimatables.forEachIndexed { index, state ->
            delay(80L * index)
            state.value = true
        }
    }

    val backgroundAlpha by animateFloatAsState(
        targetValue = if (isFocused) 0.4f else 1.0f,
        animationSpec = tween(durationMillis = 400),
        label = "background_alpha"
    )
    val backgroundScale by animateFloatAsState(
        targetValue = if (isFocused) 0.95f else 1.0f,
        animationSpec = tween(durationMillis = 400),
        label = "background_scale"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .graphicsLayer {
                    alpha = backgroundAlpha
                    scaleX = backgroundScale
                    scaleY = backgroundScale
                },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            WavyGraphic(isLoading = isLoading)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "How can I help you today?",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
            Spacer(modifier = Modifier.height(32.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.padding(horizontal = 24.dp),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 8.dp)
            ) {
                itemsIndexed(items = suggestions, key = { _, suggestion -> suggestion.title }) { index, suggestion ->
                    AnimatedVisibility(
                        visible = suggestionAnimatables.getOrElse(index) { mutableStateOf(false) }.value,
                        enter = slideInVertically { it / 2 } + fadeIn(),
                    ) {
                        ModernSuggestionChip(
                            text = suggestion.title,
                            icon = suggestion.icon,
                            onClick = { onSendMessage(suggestion.title) }
                        )
                    }
                }
            }
        }
        ChatInputBar(
            text = text,
            onValueChange = { text = it },
            isAwaitingTopic = isAwaitingTopic,
            onSendMessage = {
                onSendMessage(text)
                text = ""
            },
            onImagePickerClicked = { viewModel.requestImageForAnalysis() },
            enabled = !isLoading,
            onFocusChanged = { focused ->
                isFocused = focused
            }
        )
    }
}

/**
 * Draws a static graph representing the wavy animation in the design.
 * A more advanced Canvas implementation is required for a performant animation.
 */
@Composable
fun WavyGraphic(isLoading: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "rgb_wave_transition")
    val animatedHue by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 10000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ), label = "hue_animation"
    )

    val pulsingAlpha = if (isLoading) {
        val pulseTransition = rememberInfiniteTransition(label = "pulse_alpha_transition")
        pulseTransition.animateFloat(
            initialValue = 0.6f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(tween(1200), RepeatMode.Reverse),
            label = "pulse_alpha"
        ).value
    } else {
        1.0f
    }

    val animatedColor = Color.hsl(hue = animatedHue, saturation = 0.7f, lightness = 0.6f)

    Box(
        modifier = Modifier
            .height(150.dp)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 4.dp.toPx()
            val path = Path().apply {
                moveTo(0f, size.height * 0.7f)
                quadraticBezierTo(size.width * 0.25f, size.height * 0.2f, size.width * 0.5f, size.height * 0.6f)
                quadraticBezierTo(size.width * 0.75f, size.height * 1.0f, size.width, size.height * 0.5f)
            }
            drawPath(
                path = path,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        animatedColor.copy(alpha = 0.3f * pulsingAlpha),
                        animatedColor.copy(alpha = 1.0f * pulsingAlpha),
                        animatedColor.copy(alpha = 0.3f * pulsingAlpha)
                    )
                ),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            val path2 = Path().apply {
                moveTo(0f, size.height * 0.5f)
                quadraticBezierTo(size.width * 0.25f, size.height * 1.0f, size.width * 0.5f, size.height * 0.6f)
                quadraticBezierTo(size.width * 0.75f, size.height * 0.2f, size.width, size.height * 0.7f)
            }
            drawPath(
                path = path2,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        animatedColor.copy(alpha = 1.0f * pulsingAlpha),
                        animatedColor.copy(alpha = 0.4f * pulsingAlpha),
                        animatedColor.copy(alpha = 1.0f * pulsingAlpha)
                    )
                ),
                style = Stroke(width = strokeWidth + 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}


/**
 * Modern suggestion button (chip) with icon and text, suitable for design.
 */
@Composable
fun ModernSuggestionChip(text: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = text,
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

/**
 * Shows a “typing...” animation with three dots jumping in sequence.
 */
@Composable
private fun TypingIndicator() {
    val transition = rememberInfiniteTransition(label = "typing_indicator_transition")
    val dotSize = 8.dp
    val dotSpacing = 12.dp

    @Composable
    fun Dot(offsetY: Float) {
        Box(
            modifier = Modifier
                .size(dotSize)
                .offset(y = offsetY.dp)
                .background(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        )
    }

    val yOffset1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "dot1_offset"
    )
    val yOffset2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearOutSlowInEasing, delayMillis = 150),
            repeatMode = RepeatMode.Reverse
        ), label = "dot2_offset"
    )
    val yOffset3 by transition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearOutSlowInEasing, delayMillis = 300),
            repeatMode = RepeatMode.Reverse
        ), label = "dot3_offset"
    )

    Row(
        modifier = Modifier.padding(vertical = 16.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(dotSpacing)
    ) {
        Dot(offsetY = yOffset1)
        Dot(offsetY = yOffset2)
        Dot(offsetY = yOffset3)
    }
}

private fun String.cleanMarkdown(): String {
    return this.replace(Regex("[*#]"), "").trim()
}