package com.babelsoftware.airnote.presentation.screens.edit

import android.icu.text.SimpleDateFormat
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.RemoveRedEye
import androidx.compose.material.icons.rounded.Tune
import com.babelsoftware.airnote.data.repository.AiTone
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.data.repository.AiAction
import com.babelsoftware.airnote.data.repository.AiAssistantAction
import com.babelsoftware.airnote.presentation.components.MoreButton
import com.babelsoftware.airnote.presentation.components.NavigationIcon
import com.babelsoftware.airnote.presentation.components.NotesScaffold
import com.babelsoftware.airnote.presentation.components.RedoButton
import com.babelsoftware.airnote.presentation.components.SaveButton
import com.babelsoftware.airnote.presentation.components.UndoButton
import com.babelsoftware.airnote.presentation.components.markdown.MarkdownText
import com.babelsoftware.airnote.presentation.screens.edit.components.CustomIconButton
import com.babelsoftware.airnote.presentation.screens.edit.components.CustomTextField
import com.babelsoftware.airnote.presentation.screens.edit.components.TextFormattingToolbar
import com.babelsoftware.airnote.presentation.screens.edit.model.EditViewModel
import com.babelsoftware.airnote.presentation.screens.settings.model.IconResource
import com.babelsoftware.airnote.presentation.screens.settings.model.SettingsViewModel
import com.babelsoftware.airnote.presentation.screens.settings.settings.shapeManager
import com.babelsoftware.airnote.presentation.screens.settings.widgets.ActionType
import com.babelsoftware.airnote.presentation.screens.settings.widgets.SettingsBox
import com.babelsoftware.airnote.presentation.screens.settings.widgets.copyToClipboard
import com.babelsoftware.airnote.presentation.theme.FontUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.util.Locale

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EditNoteView(
    id: Int,
    folderId: Long?,
    settingsViewModel: SettingsViewModel,
    encrypted: Boolean = false,
    isWidget: Boolean = false,
    onClickBack: () -> Unit
) {
    val viewModel: EditViewModel = hiltViewModel<EditViewModel>()
    viewModel.updateIsEncrypted(encrypted)
    viewModel.setupNoteData(id, folderId)
    ObserveLifecycleEvents(viewModel)

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(key1 = true) {
        viewModel.uiEvent.collect { message ->
            snackbarHostState.showSnackbar(message)
        }
    }

    if (viewModel.isAiActionSheetVisible.value) {
        AiActionSheet(viewModel = viewModel)
    }

    if (viewModel.isToneActionSheetVisible.value) {
        ToneActionSheet(viewModel = viewModel)
    }

    if (viewModel.titleSuggestions.value.isNotEmpty()) {
        TitleSuggestionDialog(
            suggestions = viewModel.titleSuggestions.value,
            onDismiss = { viewModel.clearTitleSuggestions() },
            onSelect = { selectedTitle ->
                viewModel.updateNoteName(TextFieldValue(selectedTitle))
                viewModel.clearTitleSuggestions()
            }
        )
    }

    val pagerState = rememberPagerState(initialPage = if (id == 0 || isWidget || settingsViewModel.settings.value.editMode) 0 else 1, pageCount = { 2 })


    val coroutineScope = rememberCoroutineScope()

    NotesScaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            if (!settingsViewModel.settings.value.minimalisticMode) TopBar(
                pagerState,
                coroutineScope,
                onClickBack,
                viewModel
            )
        },
        content = { PagerContent(pagerState, viewModel, settingsViewModel, onClickBack) }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TopBarActions(pagerState: PagerState, onClickBack: () -> Unit, viewModel: EditViewModel) {
    val context = LocalContext.current
    val isAssistantStreaming = viewModel.isAiAssistantStreaming.value
    val isMenuExpanded = viewModel.isAiAssistantSheetVisible.value

    val actionButtonPlaceholder = @Composable {
        Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
            if (isAssistantStreaming) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp
                )
            } else {
                IconButton(onClick = { viewModel.toggleAiAssistantSheet(true) }) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = "AI Asistanı"
                    )
                }
            }
            AiCommandMenu(
                viewModel = viewModel,
                expanded = isMenuExpanded,
                onDismiss = { viewModel.toggleAiAssistantSheet(false) }
            )
        }
    }

    when (pagerState.currentPage) {

        0 -> { // Edit Mode
            Row(verticalAlignment = Alignment.CenterVertically) {
                actionButtonPlaceholder()
                if (viewModel.isDescriptionInFocus.value) {
                    RedoButton { viewModel.redo() }
                }
                SaveButton { onClickBack() }
            }
        }
        1 -> { // Preview Mode
            Row(verticalAlignment = Alignment.CenterVertically) {
                actionButtonPlaceholder()
                MoreButton {
                    viewModel.toggleEditMenuVisibility(true)
                }
                DropdownMenu(
                    expanded = viewModel.isEditMenuVisible.value,
                    onDismissRequest = { viewModel.toggleEditMenuVisibility(false) }
                ) {
                    if (viewModel.noteId.value != 0) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.delete)) },
                            leadingIcon = { Icon(Icons.Rounded.Delete, contentDescription = "Delete")},
                            onClick = {
                                viewModel.toggleEditMenuVisibility(false)
                                viewModel.deleteNote(viewModel.noteId.value)
                                onClickBack()
                            }
                        )
                    }
                    DropdownMenuItem(
                        text = { Text(stringResource(id = R.string.pinned)) },
                        leadingIcon = { Icon(if (viewModel.isPinned.value) Icons.Rounded.PushPin else Icons.Outlined.PushPin, contentDescription = "Pin")},
                        onClick = { viewModel.toggleNotePin(!viewModel.isPinned.value) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.copy)) },
                        leadingIcon = { Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy")},
                        onClick = {
                            copyToClipboard(context, viewModel.noteDescription.value.text)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.information)) },
                        leadingIcon = { Icon(Icons.Rounded.Info, contentDescription = "Information")},
                        onClick = {
                            viewModel.toggleEditMenuVisibility(false)
                            viewModel.toggleNoteInfoVisibility(true)
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PagerContent(pagerState: PagerState, viewModel: EditViewModel, settingsViewModel: SettingsViewModel, onClickBack: () -> Unit) {
    HorizontalPager(
        state = pagerState,
        modifier = Modifier.imePadding(),
        userScrollEnabled = !settingsViewModel.settings.value.disableSwipeInEditMode
    ) { page ->
        when (page) {
            0 -> EditScreen(viewModel, settingsViewModel, pagerState, onClickBack)
            1 -> PreviewScreen(viewModel, settingsViewModel, pagerState, onClickBack)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TopBar(pagerState: PagerState,coroutineScope: CoroutineScope, onClickBack: () -> Unit, viewModel: EditViewModel) {
    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        title = { ModeButton(pagerState, coroutineScope) },
        navigationIcon = {
            Row {
                NavigationIcon(onClickBack)
                if (pagerState.currentPage == 0 && viewModel.isDescriptionInFocus.value) {
                    UndoButton { viewModel.undo() }
                }
            }
     },
        actions = { TopBarActions(pagerState,  onClickBack, viewModel) }
    )
}

@Composable
fun ObserveLifecycleEvents(viewModel: EditViewModel) {
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                viewModel.saveNote(viewModel.noteId.value)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomModal(viewModel: EditViewModel, settingsViewModel: SettingsViewModel) {
    ModalBottomSheet(
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        onDismissRequest = { viewModel.toggleNoteInfoVisibility(false) }
    ) {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        Column(
            modifier = Modifier.padding(20.dp, 0.dp, 20.dp, 20.dp)
        ) {
            SettingsBox(
                size = 8.dp,
                title = stringResource(R.string.created_time),
                icon = IconResource.Vector(Icons.Rounded.Numbers),
                actionType = ActionType.TEXT,
                radius = shapeManager(
                    isFirst = true,
                    radius = settingsViewModel.settings.value.cornerRadius
                ),
                customText = sdf.format(viewModel.noteCreatedTime.value).toString()
            )
            SettingsBox(
                size = 8.dp,
                title = stringResource(R.string.words),
                icon = IconResource.Vector(Icons.Rounded.Numbers),
                radius = shapeManager(radius = settingsViewModel.settings.value.cornerRadius),
                actionType = ActionType.TEXT,
                customText = if (viewModel.noteDescription.value.text != "") viewModel.noteDescription.value.text.split(
                    "\\s+".toRegex()
                ).size.toString() else "0"
            )
            SettingsBox(
                size = 8.dp,
                title = stringResource(R.string.characters),
                icon = IconResource.Vector(Icons.Rounded.Numbers),
                actionType = ActionType.TEXT,
                radius = shapeManager(
                    radius = settingsViewModel.settings.value.cornerRadius,
                    isLast = true
                ),
                customText = viewModel.noteDescription.value.text.length.toString()
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MinimalisticMode(
    alignment : Alignment.Vertical = Alignment.CenterVertically,
    viewModel: EditViewModel,
    modifier: Modifier = Modifier,
    isEnabled: Boolean, pagerState: PagerState,
    isExtremeAmoled: Boolean,
    showOnlyDescription: Boolean = false,
    onClickBack: () -> Unit, content: @Composable () -> Unit,
) {
    val coroutineScope = rememberCoroutineScope()
    Row(
        verticalAlignment = alignment,
        modifier = modifier
            .fillMaxWidth()
            .then(if (showOnlyDescription) Modifier.padding(top = 8.dp) else Modifier)
    ) {
        if (!showOnlyDescription) {
            if (isEnabled) NavigationIcon(onClickBack)
            if (isEnabled && viewModel.isDescriptionInFocus.value) UndoButton { viewModel.undo() }
            content()
            if (isEnabled) TopBarActions(pagerState,  onClickBack, viewModel)
            if (isEnabled) ModeButton(pagerState, coroutineScope, isMinimalistic = true, isExtremeAmoled = isExtremeAmoled) } else {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEnabled) NavigationIcon(onClickBack)
                    Spacer(modifier = Modifier.weight(1f))
                    if (isEnabled) ModeButton(pagerState, coroutineScope, isMinimalistic = true, isExtremeAmoled = isExtremeAmoled)
                    if (isEnabled) TopBarActions(pagerState,  onClickBack, viewModel)
                }
                content()
            }
        }
    }
}



@OptIn(ExperimentalFoundationApi::class, ExperimentalComposeUiApi::class)
@Composable
fun EditScreen(viewModel: EditViewModel, settingsViewModel: SettingsViewModel, pagerState: PagerState, onClickBack: () -> Unit) {

    if (viewModel.isAiLoading.value) {
        LoadingOverlay()
    }

    if (viewModel.aiResultText.value != null) {
        AiResultDialog(viewModel = viewModel)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp, 16.dp, 16.dp, if (viewModel.isDescriptionInFocus.value && settingsViewModel.settings.value.isMarkdownEnabled) 2.dp else 16.dp)
    ) {
        MarkdownBox(
            isExtremeAmoled = settingsViewModel.settings.value.extremeAmoledMode,
            shape = shapeManager(
                radius = settingsViewModel.settings.value.cornerRadius,
                isFirst = true
            ),
            content = {
                MinimalisticMode(
                    viewModel = viewModel,
                    modifier = Modifier.padding(top = 2.dp),
                    isEnabled = settingsViewModel.settings.value.minimalisticMode,
                    pagerState = pagerState,
                    isExtremeAmoled = settingsViewModel.settings.value.extremeAmoledMode,
                    onClickBack = { onClickBack() }
                ) {
                    println(settingsViewModel.settings.value.useMonoSpaceFont)
                    CustomTextField(
                        value = viewModel.noteName.value,
                        modifier = Modifier.weight(1f),
                        onValueChange = { viewModel.updateNoteName(it) },
                        placeholder = stringResource(R.string.name),
                        useMonoSpaceFont = settingsViewModel.settings.value.useMonoSpaceFont
                    )
                }
            }
        )
        MarkdownBox(
            isExtremeAmoled = settingsViewModel.settings.value.extremeAmoledMode,
            shape = shapeManager(
                radius = settingsViewModel.settings.value.cornerRadius,
                isLast = true
            ),
            modifier = Modifier
                .weight(1f)
                .onFocusChanged { viewModel.toggleIsDescriptionInFocus(it.isFocused) },
            content = {
                CustomTextField(
                    value = viewModel.noteDescription.value,
                    onValueChange = { viewModel.updateNoteDescription(it) },
                    modifier = Modifier.fillMaxSize(),
                    placeholder = stringResource(R.string.description),
                    useMonoSpaceFont = settingsViewModel.settings.value.useMonoSpaceFont
                )
            }
        )
        if (viewModel.isDescriptionInFocus.value && settingsViewModel.settings.value.isMarkdownEnabled) TextFormattingToolbar(
            viewModel
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun PreviewScreen(viewModel: EditViewModel, settingsViewModel: SettingsViewModel, pagerState: PagerState, onClickBack: () -> Unit) {
    if (viewModel.isNoteInfoVisible.value) BottomModal(viewModel, settingsViewModel)

    val focusManager = LocalFocusManager.current
    focusManager.clearFocus()
    val showOnlyDescription = viewModel.noteName.value.text.isNotBlank()

    Column(
        modifier = Modifier.padding(16.dp),
    ) {
        if (showOnlyDescription) {
            MarkdownBox(
                isExtremeAmoled = settingsViewModel.settings.value.extremeAmoledMode,
                shape = shapeManager(
                    radius = settingsViewModel.settings.value.cornerRadius,
                    isFirst = true
                ),
                content = {
                    MinimalisticMode(
                        viewModel = viewModel,
                        isEnabled = settingsViewModel.settings.value.minimalisticMode,
                        pagerState = pagerState,
                        isExtremeAmoled = settingsViewModel.settings.value.extremeAmoledMode,
                        onClickBack = { onClickBack() }
                    ) {
                        MarkdownText(
                            markdown = viewModel.noteName.value.text,
                            isEnabled = settingsViewModel.settings.value.isMarkdownEnabled,
                            weight = FontWeight.Bold,
                            fontSize = FontUtils.getTitleFontSize(settingsViewModel),
                            modifier = Modifier
                                .padding(16.dp)
                                .align(Alignment.CenterHorizontally),
                            onContentChange = { viewModel.updateNoteName(TextFieldValue(text = it)) },
                            radius = settingsViewModel.settings.value.cornerRadius,
                            settingsViewModel = settingsViewModel
                        )
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            )
        }
        MarkdownBox(
            isExtremeAmoled = settingsViewModel.settings.value.extremeAmoledMode,
            shape = shapeManager(
                radius = settingsViewModel.settings.value.cornerRadius,
                isLast = (showOnlyDescription),
                isBoth = (!showOnlyDescription)
            ),
            modifier = Modifier.fillMaxSize(),
            content = {
                MinimalisticMode(
                    alignment = Alignment.Top,
                    viewModel = viewModel,
                    isExtremeAmoled = settingsViewModel.settings.value.extremeAmoledMode,
                    isEnabled = settingsViewModel.settings.value.minimalisticMode && !showOnlyDescription,
                    pagerState = pagerState,
                    showOnlyDescription = !showOnlyDescription,
                    onClickBack = { onClickBack() },
                ) {
                    MarkdownText(
                        radius = settingsViewModel.settings.value.cornerRadius,
                        markdown = viewModel.noteDescription.value.text,
                        isEnabled = settingsViewModel.settings.value.isMarkdownEnabled,
                        fontSize = FontUtils.getBodyFontSize(settingsViewModel),
                        modifier = Modifier
                            .padding(
                                16.dp,
                                top = if (showOnlyDescription) 16.dp else 6.dp,
                                16.dp,
                                16.dp
                            )
                            .weight(1f),
                        onContentChange = { viewModel.updateNoteDescription(TextFieldValue(text = it)) },
                        settingsViewModel = settingsViewModel
                    )
                    }
            }
        )
    }
}

@Composable
fun MarkdownBox(
    isExtremeAmoled: Boolean,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(0.dp),
    content: @Composable () -> Unit,
) {
    ElevatedCard(
        shape = shape,
        modifier = modifier
            .clip(shape)
            .then(
                if (isExtremeAmoled) {
                    Modifier.border(
                        1.5.dp,
                        shape = shape,
                        color = MaterialTheme.colorScheme.surfaceContainerHighest
                    )
                } else Modifier
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (!isExtremeAmoled) 1.dp else 0.dp), // Shadow

        ) {
        content()
    }
    Spacer(modifier = Modifier.height(3.dp))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ModeButton(
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    isMinimalistic: Boolean = false,
    isExtremeAmoled: Boolean = false
) {
    Row {
        RenderButton(
            pagerState = pagerState,
            coroutineScope = coroutineScope,
            pageIndex = 0,
            icon = Icons.Rounded.Edit,
            isMinimalistic = isMinimalistic,
            isExtremeAmoled = isExtremeAmoled
        )
        RenderButton(
            pagerState = pagerState,
            coroutineScope = coroutineScope,
            pageIndex = 1,
            icon = Icons.Rounded.RemoveRedEye,
            isMinimalistic = isMinimalistic,
            isExtremeAmoled = isExtremeAmoled
        )
    }
}

@Composable
private fun TitleSuggestionDialog(
    suggestions: List<String>,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.ai_suggestion)) },
        text = {
            LazyColumn {
                items(suggestions) { suggestion ->
                    Text(
                        text = suggestion,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(suggestion) }
                            .padding(vertical = 8.dp)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
private fun CommandMenuItem(
    title: String,
    subtitle: String? = null,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Text(text = title, style = MaterialTheme.typography.bodyLarge)
        if (subtitle != null) {
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun AiCommandMenu(
    viewModel: EditViewModel,
    expanded: Boolean,
    onDismiss: () -> Unit
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.widthIn(min = 280.dp)
    ) {
        CommandMenuItem(
            title = "Artı ve Eksilerini Listele",
            onClick = { viewModel.executeAiAssistantAction(AiAssistantAction.PROS_AND_CONS) }
        )
        CommandMenuItem(
            title = "Yapılacaklar Listesi Oluştur",
            onClick = { viewModel.executeAiAssistantAction(AiAssistantAction.CREATE_TODO_LIST) }
        )
        CommandMenuItem(
            title = "Daha Basit Anlat",
            subtitle = "İçe aktarılan dosyalar için önerilir",
            onClick = { viewModel.executeAiAssistantAction(AiAssistantAction.SIMPLIFY) }
        )
        CommandMenuItem(
            title = "Başlık Öner",
            onClick = { viewModel.executeAiAssistantAction(AiAssistantAction.SUGGEST_A_TITLE) }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun RenderButton(
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    pageIndex: Int,
    icon: ImageVector,
    isMinimalistic: Boolean,
    isExtremeAmoled: Boolean
) {
    CustomIconButton(
        shape = if (isMinimalistic) RoundedCornerShape(100) else if (pageIndex == 0) RoundedCornerShape(
            topStart = 32.dp,
            bottomStart = 32.dp
        ) else RoundedCornerShape(bottomEnd = 32.dp, topEnd = 32.dp),
        onClick = {
            coroutineScope.launch {
                pagerState.animateScrollToPage(pageIndex)
            }
        },
        icon = icon,
        elevation = when {
            isExtremeAmoled || isMinimalistic -> 0.dp
            pagerState.currentPage != pageIndex -> 6.dp
            else -> 12.dp
        }
    )
}

@Composable
private fun AiResultDialog(viewModel: EditViewModel) {
    val aiResult = viewModel.aiResultText.value ?: return

    AlertDialog(
        onDismissRequest = { viewModel.clearAiResult() },
        title = { Text("AI Önerisi") },
        text = { Text(aiResult) },
        confirmButton = {
            TextButton(onClick = { viewModel.replaceWithAiResult() }) {
                Text("Değiştir")
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.clearAiResult() }) {
                Text("İptal")
            }
        }
    )
}

@Composable
private fun LoadingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f))
            .clickable(enabled = false, onClick = {}) // Prevents back clicks
            .zIndex(10f), // To appear above everything
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiActionSheet(viewModel: EditViewModel) {
    if (!viewModel.isAiActionSheetVisible.value) return

    val actions = AiAction.values()

    ModalBottomSheet(
        onDismissRequest = { viewModel.toggleAiActionSheet(false) }
    ) {
        LazyColumn {
            items(actions) { action ->
                ListItem(
                    headlineContent = { Text(text = action.getDisplayName()) },
                    leadingContent = {
                        if (action == AiAction.CHANGE_TONE) {
                            Icon(
                                imageVector = Icons.Rounded.Tune,
                                contentDescription = null
                            )
                        }
                    },
                    modifier = Modifier.clickable {
                        viewModel.executeAiAction(action = action, tone = null)
                        viewModel.toggleAiActionSheet(false)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToneActionSheet(viewModel: EditViewModel) {
    val tones = AiTone.values().toList()

    ModalBottomSheet(
        onDismissRequest = { viewModel.toggleToneActionSheet(false) },
    ) {
        LazyColumn {
            items(items = tones) { tone ->
                ListItem(
                    headlineContent = { Text(tone.getDisplayName()) },
                    modifier = Modifier.clickable {
                        viewModel.executeAiAction(action = AiAction.CHANGE_TONE, tone = tone) // We call executeAiAction, this time with the selected tone
                    }
                )
            }
        }
    }
}

@Composable
fun AiAction.getDisplayName(): String {
    return when (this) {
        AiAction.IMPROVE_WRITING -> stringResource(id = R.string.ai_action_improve_writing)
        AiAction.SUMMARIZE -> stringResource(id = R.string.ai_action_summarize)
        AiAction.MAKE_SHORTER -> stringResource(id = R.string.ai_action_make_shorter)
        AiAction.MAKE_LONGER -> stringResource(id = R.string.ai_action_make_longer)
        AiAction.CHANGE_TONE -> stringResource(id = R.string.ai_action_change_tone)
    }
}

@Composable
fun AiTone.getDisplayName(): String {
    return when (this) {
        AiTone.FORMAL -> stringResource(id = R.string.ai_tone_formal)
        AiTone.BALANCED -> stringResource(id = R.string.ai_tone_balanced)
        AiTone.FRIENDLY -> stringResource(id = R.string.ai_tone_friendly)
    }
}