package com.babelsoftware.airnote.presentation.screens.home

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
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
import androidx.compose.material.icons.filled.Description
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
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.LockOpen
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
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.domain.model.Folder
import com.babelsoftware.airnote.domain.model.Note
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
import com.babelsoftware.airnote.presentation.screens.home.viewmodel.HomeViewModel
import com.babelsoftware.airnote.presentation.screens.home.widgets.FolderActionBottomSheet
import com.babelsoftware.airnote.presentation.screens.home.widgets.MoveToFolderDialog
import com.babelsoftware.airnote.presentation.screens.home.widgets.NoteFilter
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.presentation.screens.settings.settings.PasswordPrompt
import com.babelsoftware.airnote.presentation.screens.settings.settings.shapeManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3WindowSizeClassApi::class, ExperimentalAnimationApi::class)
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
    var showUnlockDialog by remember { mutableStateOf<Int?>(null) }
    var showBulkUnlockDialog by remember { mutableStateOf(false) }
    val activity = context as? Activity
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val allFolders by viewModel.allFolders.collectAsState()
    val notes by viewModel.displayedNotes.collectAsState()
    val selectedFolderId by viewModel.selectedFolderId.collectAsState()
    val query by viewModel.searchQuery.collectAsState()
    val isVaultMode by viewModel.isVaultMode.collectAsState()
    val liveSettings by settingsModel.settings

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
            var showFolderSheet by remember { mutableStateOf(false) }
            val selectedFolder = remember(selectedFolderId, allFolders) {
                allFolders.find { it.id == selectedFolderId }
            }
            val imagePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent(),
                onResult = { uri: Uri? ->
                    if (uri != null) {
                        val mimeType = context.contentResolver.getType(uri)
                        viewModel.onAttachmentSelected(uri, mimeType ?: "image/*")
                    }
                }
            )

            val filePickerLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent(),
                onResult = { uri: Uri? ->
                    if (uri != null) {
                        val mimeType = context.contentResolver.getType(uri)
                        if (mimeType == "text/plain") {
                            viewModel.onAttachmentSelected(uri, mimeType)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Currently only '.txt' files are supported.")
                            }
                        }
                    }
                }
            )

            val imageAnalysisLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent(),
                onResult = { uri: Uri? ->
                    if (uri != null) {
                        val mimeType = context.contentResolver.getType(uri)
                        val analysisPrompt = context.getString(R.string.prompt_airnote_ai_analyzeimage)

                        viewModel.analyzeFileAndCreateDraft(analysisPrompt, uri, mimeType ?: "image/*")
                    }
                }
            )

            val fileAnalysisLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.GetContent(),
                onResult = { uri: Uri? ->
                    if (uri != null) {
                        val mimeType = context.contentResolver.getType(uri)
                        if (mimeType == "text/plain") {
                            val analysisPrompt = context.getString(R.string.prompt_airnote_ai_analyzeimage)
                            viewModel.analyzeFileAndCreateDraft(analysisPrompt, uri, mimeType)
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Currently only '.txt' files are supported for analysis.")
                            }
                        }
                    }
                }
            )

            var showAttachmentTypeSheet by remember { mutableStateOf(false) }
            if (showAttachmentTypeSheet) {
                AttachmentTypeBottomSheet(
                    onDismiss = { showAttachmentTypeSheet = false },
                    onImageClicked = {
                        imagePickerLauncher.launch("image/*")
                    },
                    onFileClicked = {
                        filePickerLauncher.launch("text/plain")
                    }
                )
            }

            LaunchedEffect(Unit) {
                viewModel.uiActionChannel.collect { action ->
                    when (action) {
                        is HomeViewModel.UiAction.RequestImageForAnalysis -> {
                            imageAnalysisLauncher.launch("image/*")
                        }
                        is HomeViewModel.UiAction.RequestFileForAnalysis -> {
                            fileAnalysisLauncher.launch("text/plain")
                        }
                        is HomeViewModel.UiAction.RequestAttachmentType -> {
                            showAttachmentTypeSheet = true
                        }
                    }
                }
            }

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
            val listState = rememberLazyStaggeredGridState()

            val isFabExtended by remember {
                derivedStateOf {
                    listState.firstVisibleItemIndex == 0
                }
            }
            viewModel.setFabExtended(isFabExtended)

            val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

            if (viewModel.isAiChatSheetVisible.value) {
                ModalBottomSheet(
                    onDismissRequest = { viewModel.toggleAiChatSheet(false) },
                    sheetState = sheetState,
                    modifier = Modifier.fillMaxWidth(),
                    tonalElevation = 0.dp
                ) {
                    AiChatContainer(viewModel = viewModel, settings = liveSettings)
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
                MoveToFolderDialog(
                    folders = allFolders,
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
                            onNewNoteClicked = { onNoteClicked(0, viewModel.isVaultMode.value, selectedFolderId) },
                            onAskAiClicked = { viewModel.toggleAiChatSheet(true) },
                            onDreamJournalClicked = { viewModel.onDreamJournalClicked(onNoteClicked) }
                        )
                    }
                },
                topBar = {
                    AnimatedVisibility(
                        visible = viewModel.selectedNotes.isNotEmpty(),
                        enter = defaultScreenEnterAnimation(),
                        exit = defaultScreenExitAnimation()
                    ) {
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
                            onCloseClick = { viewModel.selectedNotes.clear() },
                            onLockClick = {
                                val anyLocked = viewModel.selectedNotes.any { it.isLocked }
                                if (anyLocked) {
                                    if (settings.noteLockPassword.isNullOrBlank()) {
                                        scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.first_set_a_note_password)) }
                                    } else {
                                        showBulkUnlockDialog = true
                                    }
                                } else {
                                    if (settings.noteLockPassword.isNullOrBlank()) {
                                        scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.first_set_a_password_in_the_settings)) }
                                    } else {
                                        viewModel.toggleLockForSelectedNotes()
                                    }
                                }
                            }
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
                                query = query,
                                onQueryChange = { viewModel.changeSearchQuery(it) },
                                onSettingsClick = onSettingsClicked,
                                onClearClick = { viewModel.changeSearchQuery("") },
                                onVaultClicked = {
                                    if (!isVaultMode) {
                                        viewModel.toggleIsPasswordPromptVisible(true)
                                    } else {
                                        viewModel.toggleIsVaultMode(false)
                                        viewModel.encryptionHelper.removePassword()
                                    }
                                },
                                isVaultMode = isVaultMode,
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
                            val clickedNote = viewModel.displayedNotes.value.find { it.id == noteId }
                            if (clickedNote != null) {
                                if (clickedNote.isLocked) {
                                    if (settings.noteLockPassword.isNullOrBlank()) {
                                        scope.launch { snackbarHostState.showSnackbar(context.getString(R.string.note_is_locked_but_no_password)) }
                                    } else {
                                        showUnlockDialog = noteId
                                    }
                                } else {
                                    onNoteClicked(clickedNote.id, clickedNote.encrypted, clickedNote.folderId)
                                }
                            }
                        },
                        selectedNotes = viewModel.selectedNotes,
                        viewMode = settingsModel.settings.value.viewMode,
                        searchText = query.ifBlank { null },
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
        var showUnlockDialog by remember { mutableStateOf<Int?>(null) }
        if (showUnlockDialog != null) {
            var passwordInput by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showUnlockDialog = null },
                title = { Text(stringResource(R.string.unlock_note)) },
                text = {
                    Column {
                        Text(stringResource(R.string.enter_your_password_to_view_this_note))
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = passwordInput,
                            onValueChange = { passwordInput = it },
                            label = { Text(stringResource(R.string.note_password)) },
                            visualTransformation = PasswordVisualTransformation(),
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        if (passwordInput == settings.noteLockPassword) {
                            val noteToOpen = notes.find { it.id == showUnlockDialog }
                            if (noteToOpen != null) {
                                onNoteClicked(noteToOpen.id, noteToOpen.encrypted, noteToOpen.folderId)
                            }
                            showUnlockDialog = null
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Wrong password!!") }
                        }
                    }) { Text("Aç") }
                },
                dismissButton = {
                    TextButton(onClick = { showUnlockDialog = null }) { Text(stringResource(R.string.cancel)) }
                }
            )
        }
        if (showBulkUnlockDialog) {
            var passwordInput by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showBulkUnlockDialog = false },
                title = { Text(stringResource(R.string.unlock_selected_notes)) },
                text = {
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text(stringResource(R.string.note_password)) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true
                    )
                },
                confirmButton = {
                    Button(onClick = {
                        if (passwordInput == settings.noteLockPassword) {
                            viewModel.toggleLockForSelectedNotes()
                            showBulkUnlockDialog = false
                        } else {
                            scope.launch { snackbarHostState.showSnackbar("Wrong password!") }
                        }
                    }) { Text(stringResource(R.string.confirm)) }
                },
                dismissButton = {
                    TextButton(onClick = { showBulkUnlockDialog = false }) { Text(stringResource(R.string.cancel)) }
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
    onAskAiClicked: () -> Unit,
    onDreamJournalClicked: () -> Unit
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
    var hopTranslationY by remember { mutableStateOf(0f)
    }

    LaunchedEffect(isExpanded) {
        if (isExpanded) {
            delay(40)
            launch {
                animate(0f, -20f, animationSpec = spring(stiffness = Spring.StiffnessMedium)) { v, _ -> hopTranslationY = v }
                animate(-20f, 0f, animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)) { v, _ -> hopTranslationY = v }
            }
        } else {
            launch {
                animate(hopTranslationY, 0f, animationSpec = tween(100)) { v, _ -> hopTranslationY = v }
            }
        }
    }


    Column(
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = fadeIn(animationSpec = tween(150)),
            exit = fadeOut(animationSpec = tween(150))
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.graphicsLayer {
                        alpha = secondaryButtonAlpha
                        scaleX = secondaryButtonScale
                        scaleY = secondaryButtonScale
                    }
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 2.dp
                    ) {
                        Text(
                            text = stringResource(R.string.ai_button_texts),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            onAskAiClicked()
                            isExpanded = false
                        },
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = stringResource(R.string.ai_button_texts),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer,
                            modifier = Modifier.graphicsLayer {
                                translationY = hopTranslationY
                            }
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.graphicsLayer {
                        alpha = secondaryButtonAlpha
                        scaleX = secondaryButtonScale
                        scaleY = secondaryButtonScale
                    }
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 2.dp
                    ) {
                        Text(
                            text = stringResource(R.string.new_note),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            onNewNoteClicked()
                            isExpanded = false
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(
                            Icons.Rounded.Edit,
                            contentDescription = stringResource(R.string.new_note),
                            modifier = Modifier.graphicsLayer {
                                translationY = hopTranslationY
                            }
                        )
                    }
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.graphicsLayer {
                        alpha = secondaryButtonAlpha
                        scaleX = secondaryButtonScale
                        scaleY = secondaryButtonScale
                    }
                ) {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        tonalElevation = 2.dp
                    ) {
                        Text(
                            text = stringResource(R.string.dream_journal_new_note),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                    SmallFloatingActionButton(
                        onClick = {
                            onDreamJournalClicked()
                            isExpanded = false
                        },
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Icon(
                            Icons.Default.Book,
                            contentDescription = "Dream Journal",
                            modifier = Modifier.graphicsLayer {
                                translationY = hopTranslationY
                            }
                        )
                    }
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
    onCloseClick: () -> Unit,
    onLockClick: () -> Unit = {},
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
                IconButton(onClick = onLockClick) {
                    val allLocked = selectedNotes.all { it.isLocked }
                    Icon(
                        imageVector = if (allLocked) Icons.Rounded.LockOpen else Icons.Rounded.Lock,
                        contentDescription = "Lock/Unlock"
                    )
                }
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
    query: String,
    onQueryChange: (String) -> Unit,
    onSettingsClick: () -> Unit,
    onVaultClicked: () -> Unit,
    onClearClick: () -> Unit,
    isVaultMode: Boolean,
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
                    VaultButton(isVaultMode) { onVaultClicked() }
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
            }
        },
        onQueryChange = onQueryChange,
        onSearch = onQueryChange,
        onActiveChange = {},
        active = false,
    ) {}
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttachmentTypeBottomSheet(
    onDismiss: () -> Unit,
    onImageClicked: () -> Unit,
    onFileClicked: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp, top = 16.dp)
        ) {
            ListItem(
                headlineContent = { Text(stringResource(R.string.add_image)) },
                leadingContent = { Icon(Icons.Default.Image, contentDescription = "Image") },
                modifier = Modifier.clickable {
                    onImageClicked()
                    onDismiss()
                }
            )
            ListItem(
                headlineContent = { Text(stringResource(R.string.add_text_file)) },
                leadingContent = { Icon(Icons.Default.Description, contentDescription = "Fİle") },
                modifier = Modifier.clickable {
                    onFileClicked()
                    onDismiss()
                }
            )
        }
    }
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
                    Button(
                        onClick = onAddFolderClicked,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp)
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
                Text(
                    text = stringResource(R.string.preview),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                FolderListItem(
                    name = name.ifBlank { stringResource(R.string.folder_name) },
                    iconName = selectedIconName,
                    isSelected = false,
                    onClick = {}
                )
                Spacer(modifier = Modifier.height(20.dp))
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
    "Inbox" to Icons.Default.FolderOpen,
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
