package com.babelsoftware.airnote.presentation.screens.home

import android.app.Activity
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.LinearEasing
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.material.icons.filled.AttachFile
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
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Headset
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
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
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material.icons.rounded.AddComment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.Psychology
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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.data.repository.AiMode
import com.babelsoftware.airnote.domain.model.AiChatSession
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
            val selectedFolderId by viewModel.selectedFolderId.collectAsState()
            var showFolderSheet by remember { mutableStateOf(false) }
            val selectedFolder = remember(selectedFolderId, allFolders) {
                allFolders.find { it.id == selectedFolderId }
            }
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
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp
                ) {
                    AiChatContainer(viewModel = viewModel)
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
                            onNewNoteClicked = { onNoteClicked(0, viewModel.isVaultMode.value, selectedFolderId) },
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
                        containerColor = AiButtonColors.GeminiContainer
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = stringResource(R.string.ai_button_texts),
                            tint = AiButtonColors.GeminiOnContainer
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
                        Icon(Icons.Rounded.Edit, contentDescription = stringResource(R.string.new_note))
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

// =================================================================
// === Enhanced AirNote AI Interface ===============================
// =================================================================

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AiChatContainer(viewModel: HomeViewModel) {
    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(Color(0xFF10141C), Color(0xFF0A0D12))
    )
    val showHistoryScreen by viewModel.showAiHistoryScreen
    val allSessions by viewModel.allChatSessions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
    ) {
        AiTopBar(
            viewModel = viewModel,
            showHistory = showHistoryScreen,
            onToggleHistory = { viewModel.toggleAiHistoryScreen(!showHistoryScreen) }
        )

        AnimatedContent(targetState = showHistoryScreen, label = "AiScreenAnimation") { showHistory ->
            if (showHistory) {
                AiHistoryScreen(
                    sessions = allSessions,
                    onNewChatClicked = { viewModel.startNewChat() },
                    onSessionClicked = { viewModel.loadChatSession(it) },
                    onDeleteSession = { viewModel.deleteChatSession(it.id) }
                )
            } else {
                AiMainContent(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun AiMainContent(viewModel: HomeViewModel) {
    val chatState by viewModel.chatState.collectAsState()
    var text by remember { mutableStateOf("") }
    val isLoading = chatState.messages.any { it.isLoading }
    val isChatActive = chatState.hasStartedConversation || chatState.messages.isNotEmpty()

    if (viewModel.showAskQuestionDialog.value) {
        AskAiQuestionDialog(
            onDismiss = { viewModel.onDismissQuestionDialog() },
            onConfirm = { question ->
                viewModel.sendMessage(question)
                viewModel.onDismissQuestionDialog()
            }
        )
    }

    if (viewModel.showCreateDraftDialog.value) {
        CreateDraftDialog(
            onDismiss = { viewModel.onDismissCreateDraftDialog() },
            onConfirm = { topic ->
                viewModel.generateDraft(topic)
                viewModel.onDismissCreateDraftDialog()
            }
        )
    }

    val onSendMessage = { message: String ->
        if (message.isNotBlank()) {
            if (chatState.isAwaitingDraftTopic) {
                viewModel.generateDraft(message)
            } else {
                viewModel.sendMessage(message)
            }
            text = ""
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
            when {
                chatState.latestDraft != null -> {
                    DraftDisplay(
                        draft = chatState.latestDraft,
                        onSave = { viewModel.saveDraftedNote() },
                        onRegenerate = { viewModel.regenerateDraft() }
                    )
                }
                isChatActive -> {
                    ChatScreenContent(
                        messages = chatState.messages,
                        topicForLoading = if (
                            chatState.messages.lastOrNull()?.isLoading == true &&
                            chatState.messages.getOrNull(chatState.messages.size - 2)?.participant == Participant.USER
                        ) {
                            chatState.messages.getOrNull(chatState.messages.size - 2)?.text
                        } else {
                            null
                        }
                    )
                }
                else -> {
                    NewAiHomeScreen(viewModel = viewModel)
                }
            }
        }

        if (isChatActive && chatState.latestDraft == null) {
            RedesignedChatInputBar(
                text = text,
                onValueChange = { text = it },
                onSendMessage = { onSendMessage(text) },
                onImagePickerClicked = { viewModel.requestImageForAnalysis() },
                enabled = !isLoading,
                placeholderText = if (chatState.isAwaitingDraftTopic) stringResource(R.string.draft_topic_placeholder) else stringResource(R.string.ask_airnote_ai)
            )
        } else if (!isChatActive) {
            PreChatInputBar(
                text = text,
                onValueChange = { text = it },
                onSendMessage = onSendMessage,
                onImagePickerClicked = { viewModel.requestImageForAnalysis() },
                enabled = !isLoading
            )
        }
    }
}


@Composable
fun NewAiHomeScreen(viewModel: HomeViewModel) {
    val chatState by viewModel.chatState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(64.dp))
            AiCentralGraphic(isThinking = chatState.messages.any { it.isLoading })
            Spacer(modifier = Modifier.height(24.dp))
        }

        val suggestions = viewModel.suggestions
        val rows = (suggestions.size + 1) / 2
        items(rows) { rowIndex ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                val itemIndex1 = rowIndex * 2
                Box(modifier = Modifier.weight(1f)) {
                    ActionCard(
                        text = suggestions[itemIndex1].title,
                        icon = suggestions[itemIndex1].icon,
                        onClick = suggestions[itemIndex1].action
                    )
                }

                val itemIndex2 = itemIndex1 + 1
                Box(modifier = Modifier.weight(1f)) {
                    if (itemIndex2 < suggestions.size) {
                        ActionCard(
                            text = suggestions[itemIndex2].title,
                            icon = suggestions[itemIndex2].icon,
                            onClick = suggestions[itemIndex2].action
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun ChatScreenContent(
    messages: List<com.babelsoftware.airnote.domain.model.ChatMessage>,
    topicForLoading: String?
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(messages.size - 1)
            }
        }
    }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp),
        state = listState,
        contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
    ) {
        items(items = messages, key = { it.hashCode() }) { message ->
            ChatMessageItem(message = message, topicForLoading = topicForLoading)
        }
    }
}

@Composable
fun AiHistoryScreen(
    sessions: List<AiChatSession>,
    onNewChatClicked: () -> Unit,
    onSessionClicked: (AiChatSession) -> Unit,
    onDeleteSession: (AiChatSession) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            OutlinedButton(
                onClick = onNewChatClicked,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
            ) {
                Icon(Icons.Rounded.AddComment, contentDescription = stringResource(R.string.new_chat))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.new_chat))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                stringResource(R.string.history),
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.8f)
            )
        }
        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 80.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    stringResource(R.string.no_chat_history),
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 32.dp)
                )
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(items = sessions, key = { it.id }) { session ->
                    HistoryItem(
                        session = session,
                        onClick = { onSessionClicked(session) },
                        onDelete = { onDeleteSession(session) }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun HistoryItem(session: AiChatSession, onClick: () -> Unit, onDelete: () -> Unit) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text(stringResource(R.string.delete_chat)) },
            text = { Text(stringResource(R.string.delete_chat_description)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showDeleteConfirm = true }
            ),
        color = Color.White.copy(alpha = 0.05f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                if (session.aiMode == AiMode.CREATIVE_MIND.name) Icons.Rounded.Psychology else Icons.Rounded.Notes,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = session.title,
                color = Color.White.copy(alpha = 0.9f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AiTopBar(
    viewModel: HomeViewModel,
    showHistory: Boolean,
    onToggleHistory: () -> Unit
) {
    val currentAiMode by viewModel.aiMode.collectAsState()
    var showModelMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box {
            Row(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.1f))
                    .clickable { showModelMenu = true }
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = when(currentAiMode) {
                        AiMode.NOTE_ASSISTANT -> Icons.Rounded.Notes
                        AiMode.CREATIVE_MIND -> Icons.Rounded.Psychology
                    },
                    contentDescription = "AI Model",
                    tint = Color(0xFF33A2FF),
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = when (currentAiMode) {
                        AiMode.NOTE_ASSISTANT -> stringResource(R.string.ai_mode_note_assistant)
                        AiMode.CREATIVE_MIND -> stringResource(R.string.ai_mode_creative_mind)
                    },
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp
                )
                Icon(Icons.Rounded.ExpandMore, contentDescription = "Change Model", tint = Color.White.copy(alpha = 0.7f))
            }

            DropdownMenu(
                expanded = showModelMenu,
                onDismissRequest = { showModelMenu = false },
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.ai_mode_note_assistant)) },
                    onClick = {
                        viewModel.setAiMode(AiMode.NOTE_ASSISTANT)
                        showModelMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Notes,
                            contentDescription = stringResource(R.string.ai_mode_note_assistant)
                        )
                    }
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.ai_mode_creative_mind)) },
                    onClick = {
                        viewModel.setAiMode(AiMode.CREATIVE_MIND)
                        showModelMenu = false
                    },
                    leadingIcon = {
                        Icon(
                            Icons.Rounded.Psychology,
                            contentDescription = stringResource(R.string.ai_mode_creative_mind)
                        )
                    }
                )
            }
        }

        IconButton(onClick = onToggleHistory) {
            Icon(
                if (showHistory) Icons.AutoMirrored.Filled.Chat else Icons.Rounded.History,
                contentDescription = "Toggle History",
                tint = Color.White.copy(alpha = 0.8f)
            )
        }
    }
}

@Composable
fun AiCentralGraphic(isThinking: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_animation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(if (isThinking) 2000 else 10000, easing = LinearEasing), RepeatMode.Restart),
        label = "orb_rotation"
    )
    val scale by animateFloatAsState(
        targetValue = if (isThinking) 1.1f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "orb_scale"
    )

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color(0xFF33A2FF).copy(alpha = 0.1f), Color.Transparent),
                ),
                radius = (size.minDimension / 2.0f) * scale
            )
        }
        Box(
            modifier = Modifier
                .size(120.dp)
                .graphicsLayer {
                    rotationZ = rotation
                }
                .border(
                    BorderStroke(
                        2.dp,
                        Brush.sweepGradient(
                            0.0f to Color.Transparent,
                            0.7f to Color(0xFF33A2FF),
                            1.0f to Color.Transparent
                        )
                    ),
                    CircleShape
                ),
        )
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = "AI Core",
            tint = Color(0xFF33A2FF),
            modifier = Modifier.size(50.dp)
        )
    }
}

@Composable
fun ActionCard(text: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White.copy(alpha = 0.8f)
            )
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
fun ChatMessageItem(
    message: com.babelsoftware.airnote.domain.model.ChatMessage,
    topicForLoading: String?
) {
    val isUser = message.participant == Participant.USER
    Row(
        modifier = Modifier
            .padding(vertical = 4.dp, horizontal = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        if (message.isLoading) {
            if (message.text.startsWith("// Generating Note...")) {
                TerminalLoadingIndicator(topic = topicForLoading ?: "")
            } else {
                TypingIndicator()
            }
        } else {
            val bubbleShape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
                bottomStart = if (isUser) 16.dp else 0.dp,
                bottomEnd = if (isUser) 0.dp else 16.dp
            )

            val backgroundBrush = when (message.participant) {
                Participant.USER -> Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF33A2FF).copy(alpha = 0.4f),
                        Color(0xFF33A2FF).copy(alpha = 0.1f)
                    )
                )
                Participant.MODEL -> Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.15f),
                        Color.White.copy(alpha = 0.05f)
                    )
                )
                Participant.ERROR -> Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.error.copy(alpha = 0.4f),
                        MaterialTheme.colorScheme.error.copy(alpha = 0.2f)
                    )
                )
            }

            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .background(brush = backgroundBrush)
                    .border(
                        width = 1.dp,
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        ),
                        shape = bubbleShape
                    )
                    .padding(12.dp)
            ) {
                Text(
                    text = message.text.replace(Regex("[*#]"), "").trim(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (message.participant == Participant.ERROR) Color.White else Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun RedesignedChatInputBar(
    text: String,
    onValueChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onImagePickerClicked: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    placeholderText: String
) {
    val haptic = LocalHapticFeedback.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onImagePickerClicked, enabled = enabled) {
                Icon(
                    Icons.Default.AttachFile,
                    contentDescription = "Attach File",
                    tint = Color.White.copy(alpha = 0.7f)
                )
            }
            TextField(
                value = text,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                enabled = enabled,
                placeholder = {
                    Text(placeholderText, color = Color.White.copy(alpha = 0.5f))
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = Color(0xFF33A2FF),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White.copy(alpha = 0.9f),
                    disabledPlaceholderColor = Color.White.copy(alpha = 0.3f)
                )
            )
            AnimatedVisibility(visible = text.isNotBlank() && enabled) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSendMessage()
                    },
                    modifier = Modifier.padding(start = 8.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0xFF33A2FF),
                        contentColor = Color.White
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send message"
                    )
                }
            }
        }
    }
}

@Composable
fun PreChatInputBar(
    text: String,
    onValueChange: (String) -> Unit,
    onSendMessage: (String) -> Unit,
    onImagePickerClicked: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White.copy(alpha = 0.05f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = text,
                    onValueChange = onValueChange,
                    modifier = Modifier.weight(1f),
                    enabled = enabled,
                    placeholder = {
                        Text(stringResource(R.string.ask_airnote_ai), color = Color.White.copy(alpha = 0.6f))
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = Color.White.copy(alpha = 0.6f))
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF33A2FF),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White.copy(alpha = 0.9f),
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onImagePickerClicked, enabled = enabled) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Attach File",
                        tint = Color.White.copy(alpha = 0.8f)
                    )
                }

                val searchString = stringResource(R.string.ai_search)
                val thinkString = stringResource(R.string.ai_think)

                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
                ) {
                    InputActionButton(
                        text = searchString,
                        icon = Icons.Rounded.AutoAwesome,
                        onClick = { onValueChange("$searchString: ") }
                    )
                    InputActionButton(
                        text = thinkString,
                        icon = Icons.Default.Language,
                        onClick = { onValueChange("$thinkString: ") }
                    )
                }

                IconButton(
                    onClick = { if (text.isNotBlank()) onSendMessage(text) },
                    enabled = text.isNotBlank() && enabled,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color(0xFF33A2FF),
                        contentColor = Color.White,
                        disabledContainerColor = Color.White.copy(alpha = 0.1f),
                        disabledContentColor = Color.White.copy(alpha = 0.4f)
                    )
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send"
                    )
                }
            }
        }
    }
}

@Composable
private fun InputActionButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = Color.White.copy(alpha = 0.1f),
            contentColor = Color.White.copy(alpha = 0.9f)
        ),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun AskAiQuestionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var question by remember { mutableStateOf("") }
    val maxChars = 280

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF10141C),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.ask_a_question_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = question,
                    onValueChange = { if (it.length <= maxChars) question = it },
                    placeholder = { Text(stringResource(R.string.ask_a_question), color = Color.White.copy(alpha = 0.6f)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF33A2FF),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White.copy(alpha = 0.9f),
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                Text(
                    text = "${question.length} / $maxChars",
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel), color = Color.White.copy(alpha = 0.8f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(question) },
                        enabled = question.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF33A2FF),
                            contentColor = Color(0xFF10141C),
                            disabledContainerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.send))
                    }
                }
            }
        }
    }
}

@Composable
fun CreateDraftDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var topic by remember { mutableStateOf("") }
    val maxChars = 100

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF10141C),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.new_ai_note_draft),
                        style = MaterialTheme.typography.headlineSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.sample_question_request),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = topic,
                    onValueChange = { if (it.length <= maxChars) topic = it },
                    placeholder = { Text(stringResource(R.string.example_question), color = Color.White.copy(alpha = 0.6f)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White.copy(alpha = 0.05f),
                        unfocusedContainerColor = Color.White.copy(alpha = 0.05f),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = Color(0xFF33A2FF),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White.copy(alpha = 0.9f),
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                Text(
                    text = "${topic.length} / $maxChars",
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel), color = Color.White.copy(alpha = 0.8f))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(topic) },
                        enabled = topic.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF33A2FF),
                            contentColor = Color(0xFF10141C),
                            disabledContainerColor = Color.White.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(stringResource(R.string.create))
                    }
                }
            }
        }
    }
}


@Composable
fun DraftDisplay(draft: DraftedNote?, onSave: () -> Unit, onRegenerate: () -> Unit) {
    if (draft == null) return

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = Color.White.copy(alpha = 0.05f),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    item {
                        Text(
                            text = draft.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    item {
                        Text(
                            text = draft.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 24.sp
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onRegenerate,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF33A2FF))
                ) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = Color(0xFF33A2FF))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.regenerate_note), color = Color(0xFF33A2FF))
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF33A2FF))
                ) {
                    Text(stringResource(R.string.save_note), color = Color(0xFF10141C))
                }
            }
        }
    }
}

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
                    color = Color.White.copy(alpha = 0.5f),
                    shape = CircleShape
                )
        )
    }

    val yOffset1 by transition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(400),
            repeatMode = RepeatMode.Reverse
        ), label = "dot1_offset"
    )
    val yOffset2 by transition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 150),
            repeatMode = RepeatMode.Reverse
        ), label = "dot2_offset"
    )
    val yOffset3 by transition.animateFloat(
        initialValue = 0f,
        targetValue = -10f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 300),
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

@Composable
fun TerminalLoadingIndicator(topic: String) {
    var displayedText by remember { mutableStateOf("") }
    val fullText = """
[INFO] Connecting to AirNote AI services...
[INFO] Authentication successful.
[PROCESS] Analyzing topic: "$topic"
[PROCESS] Generating content blocks...
[PROCESS] Assembling draft...
[SUCCESS] Note draft created.
    """.trimIndent()

    LaunchedEffect(Unit) {
        fullText.lines().forEach { line ->
            for (char in line) {
                displayedText += char
                delay(10)
            }
            displayedText += "\n"
            delay(280)
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "cursor_blink")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500),
            repeatMode = RepeatMode.Reverse
        ), label = "cursor_alpha"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.Black.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Text(
                text = displayedText,
                color = Color.Green.copy(alpha = 0.8f),
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            Text(
                text = "█",
                color = Color.Green.copy(alpha = cursorAlpha),
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )
        }
    }
}
