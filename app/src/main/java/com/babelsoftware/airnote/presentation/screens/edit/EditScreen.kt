package com.babelsoftware.airnote.presentation.screens.edit

import android.content.Intent
import android.icu.text.SimpleDateFormat
import androidx.activity.compose.LocalActivity
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.EmojiSymbols
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Interests
import androidx.compose.material.icons.rounded.Numbers
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.RemoveRedEye
import androidx.compose.material.icons.rounded.SettingsSystemDaydream
import androidx.compose.material.icons.rounded.Spellcheck
import androidx.compose.material.icons.rounded.Summarize
import androidx.compose.material.icons.rounded.Translate
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.data.repository.AiAction
import com.babelsoftware.airnote.data.repository.AiAssistantAction
import com.babelsoftware.airnote.data.repository.AiTone
import com.babelsoftware.airnote.domain.model.ChatMessage
import com.babelsoftware.airnote.domain.model.Participant
import com.babelsoftware.airnote.presentation.components.MoreButton
import com.babelsoftware.airnote.presentation.components.NavigationIcon
import com.babelsoftware.airnote.presentation.components.NotesScaffold
import com.babelsoftware.airnote.presentation.components.RedoButton
import com.babelsoftware.airnote.presentation.components.SaveButton
import com.babelsoftware.airnote.presentation.components.UndoButton
import com.babelsoftware.airnote.presentation.components.markdown.MarkdownText
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
import kotlinx.coroutines.delay
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
    val activity = LocalActivity.current
    val intent = activity?.intent
    val isLoading by viewModel.isLoading

    LaunchedEffect(key1 = id, key2 = intent) {
        if (id == 0 && intent?.action == Intent.ACTION_SEND && "text/plain" == intent.type) {
            viewModel.handleSharedIntent(intent)
        } else {
            viewModel.setupNoteData(id, folderId)
        }
    }

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

    if (viewModel.isTranslateSheetVisible.value) {
        TranslateLanguageSheet(viewModel = viewModel)
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
            if (!isLoading && !settingsViewModel.settings.value.minimalisticMode) TopBar(
                pagerState,
                coroutineScope,
                onClickBack,
                viewModel
            )
        },
        content = {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                PagerContent(pagerState, viewModel, settingsViewModel, onClickBack)
            }
        }
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
        modifier = Modifier,
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
        title = {
            ModeButton(
                pagerState = pagerState,
                coroutineScope = coroutineScope,
                isUndoRedoVisible = viewModel.isDescriptionInFocus.value
            )
        },
        navigationIcon = {
            Row {
                NavigationIcon(onClickBack)
                if (pagerState.currentPage == 0 && viewModel.isDescriptionInFocus.value) {
                    UndoButton { viewModel.undo() }
                }
            }
        },
        actions = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (pagerState.currentPage == 0 && viewModel.isDescriptionInFocus.value) {
                    RedoButton { viewModel.redo() }
                }
                TopBarActions(pagerState,  onClickBack, viewModel)
            }
        }
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
            if (isEnabled) ModeButton(pagerState, coroutineScope, isUndoRedoVisible = viewModel.isDescriptionInFocus.value ) } else {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEnabled) NavigationIcon(onClickBack)
                    Spacer(modifier = Modifier.weight(1f))
                    if (isEnabled) ModeButton(pagerState, coroutineScope, isUndoRedoVisible = viewModel.isDescriptionInFocus.value )
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
            .imePadding()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(16.dp, 16.dp, 16.dp, 0.dp)
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
                    .onFocusChanged { focusState ->
                        if (!viewModel.isMinimalAiUiVisible.value) {
                            viewModel.toggleIsDescriptionInFocus(focusState.isFocused)
                        }
                    },
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
        }
        BottomBarContainer(viewModel, settingsViewModel)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BottomBarContainer(viewModel: EditViewModel, settingsViewModel: SettingsViewModel) {
    val isAiVisible by viewModel.isMinimalAiUiVisible
    val showBar = (viewModel.isDescriptionInFocus.value || isAiVisible) && settingsViewModel.settings.value.isMarkdownEnabled

    AnimatedVisibility(
        visible = showBar,
        enter = slideInVertically { it },
        exit = slideOutVertically { it }
    ) {
        AnimatedContent(
            targetState = isAiVisible,
            transitionSpec = {
                if (targetState) {
                    (fadeIn(animationSpec = tween(220, delayMillis = 90)) + slideInVertically(initialOffsetY = { it }, animationSpec = tween(220, delayMillis = 90)))
                        .togetherWith(fadeOut(animationSpec = tween(90)))
                } else {
                    (fadeIn(animationSpec = tween(220, delayMillis = 90)) + slideInVertically(initialOffsetY = { -it }, animationSpec = tween(220, delayMillis = 90)))
                        .togetherWith(fadeOut(animationSpec = tween(90)))
                }
            },
            label = "BottomBarAnimation"
        ) { targetState ->
            if (targetState) {
                MinimalAiChatInterface(viewModel = viewModel, settingsViewModel = settingsViewModel, isDreamJournalMode = viewModel.isDreamJournalMode.value)
            } else {
                TextFormattingToolbar(viewModel = viewModel)
            }
        }
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ModeButton(
    pagerState: PagerState,
    coroutineScope: CoroutineScope,
    isUndoRedoVisible: Boolean
) {
    val options = listOf(
        stringResource(R.string.edit) to Icons.Rounded.Edit,
        stringResource(R.string.preview) to Icons.Rounded.RemoveRedEye
    )

    SingleChoiceSegmentedButtonRow(
        modifier = Modifier.height(40.dp)
    ) {
        options.forEachIndexed { index, item ->
            val shape = when (index) {
                0 -> RoundedCornerShape(topStartPercent = 100, bottomStartPercent = 100)
                options.lastIndex -> RoundedCornerShape(topEndPercent = 100, bottomEndPercent = 100)
                else -> RectangleShape
            }

            SegmentedButton(
                shape = shape,
                onClick = {
                    coroutineScope.launch {
                        pagerState.animateScrollToPage(index)
                    }
                },
                selected = pagerState.currentPage == index,
                icon = {
                    Icon(
                        imageVector = item.second,
                        contentDescription = item.first,
                        modifier = Modifier.size(20.dp)
                    )
                },
                label = {
                    Text(
                        text = if (!isUndoRedoVisible) item.first else ""
                    )
                }
            )
        }
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
            title = stringResource(R.string.ai_command_pros_cons),
            onClick = { viewModel.executeAiAssistantAction(AiAssistantAction.PROS_AND_CONS) }
        )
        CommandMenuItem(
            title = stringResource(R.string.ai_command_todo),
            onClick = { viewModel.executeAiAssistantAction(AiAssistantAction.CREATE_TODO_LIST) }
        )
        CommandMenuItem(
            title = stringResource(R.string.ai_command_simpler),
            subtitle = stringResource(R.string.ai_command_simpler_subtitle),
            onClick = { viewModel.executeAiAssistantAction(AiAssistantAction.SIMPLIFY) }
        )
        CommandMenuItem(
            title = stringResource(R.string.ai_command_title),
            onClick = { viewModel.executeAiAssistantAction(AiAssistantAction.SUGGEST_A_TITLE) }
        )
        CommandMenuItem(
            title = stringResource(R.string.translate_all_note),
            onClick = {
                onDismiss()
                viewModel.onTranslateClicked(forSelection = false)
            }
        )
    }
}

@Composable
private fun AiResultDialog(viewModel: EditViewModel) {
    val aiResult = viewModel.aiResultText.value ?: return

    AlertDialog(
        onDismissRequest = { viewModel.clearAiResult() },
        title = { Text(stringResource(R.string.ai_suggestion)) },
        text = { Text(aiResult) },
        confirmButton = {
            TextButton(onClick = { viewModel.replaceWithAiResult() }) {
                Text(stringResource(R.string.change))
            }
        },
        dismissButton = {
            TextButton(onClick = { viewModel.clearAiResult() }) {
                Text(stringResource(R.string.cancel))
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

    LaunchedEffect(viewModel.isAiActionSheetVisible.value) {
        if (viewModel.isAiActionSheetVisible.value) {
            delay(300L)
            viewModel.markSheetAsReadyForInteraction()
        }
    }

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
                    modifier = Modifier.clickable(
                        enabled = viewModel.isSheetReadyForInteraction.value
                    ) {
                        if (viewModel.isSheetReadyForInteraction.value) {
                            viewModel.executeAiAction(action = action, tone = null)
                        }
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MinimalAiChatInterface(viewModel: EditViewModel, settingsViewModel: SettingsViewModel, isDreamJournalMode: Boolean = false) {
    val isTextSelected = viewModel.noteDescription.value.selection.collapsed.not()
    val isChatActive = viewModel.isMinimalChatActive.value
    val chatText = viewModel.minimalAiChatText.value
    val chatHistory = viewModel.minimalAiChatHistory.value
    val isLoading = viewModel.isMinimalAiLoading.value
    val chatScrollState = rememberScrollState()
    val cornerRadius = settingsViewModel.settings.value.cornerRadius

    LaunchedEffect(chatHistory.size) {
        chatScrollState.animateScrollTo(chatScrollState.maxValue)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        shape = RoundedCornerShape(cornerRadius.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isDreamJournalMode) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "AirNote AI - Dream Edition",
                            tint = Color(0xFFB39DDB),
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = stringResource(R.string.dream_journal_ai_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFB39DDB)
                        )
                    }
                } else {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.AutoAwesome,
                            contentDescription = "AirNote AI",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "AirNote AI",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.weight(1f))
                IconButton(
                    onClick = { viewModel.toggleMinimalAiUi(false) },
                ) {
                    Icon(Icons.Rounded.Close, contentDescription = stringResource(R.string.close))
                }
            }

            if (isDreamJournalMode) {
                AnimatedVisibility(visible = !isChatActive) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            InputActionButton(
                                text = stringResource(R.string.interpret_my_dream),
                                icon = Icons.Rounded.Interests,
                                enabled = true,
                                onClick = {
                                    viewModel.executeDreamAnalysis()
                                }
                            )
                        }
                        item {
                            InputActionButton(
                                text = stringResource(R.string.explain_symbols),
                                icon = Icons.Rounded.EmojiSymbols,
                                enabled = true,
                                onClick = {
                                    viewModel.executeDreamSymbolAnalysis()
                                }
                            )
                        }
                        item {
                            InputActionButton(
                                text = stringResource(R.string.emotional_analysis),
                                icon = Icons.Rounded.EmojiEmotions,
                                enabled = true,
                                onClick = {
                                    viewModel.executeDreamEmotionAnalysis()
                                }
                            )
                        }
                    }
                }
            } else {
                AnimatedVisibility(visible = !isChatActive) {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(AiAction.values()) { action ->
                            InputActionButton(
                                text = action.getDisplayName(),
                                icon = action.getIcon(),
                                enabled = isTextSelected,
                                onClick = {
                                    viewModel.executeAiAction(action = action, tone = null)
                                    if (action == AiAction.CHANGE_TONE || action == AiAction.TRANSLATE) {
                                        viewModel.toggleMinimalAiUi(false)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            AnimatedVisibility(visible = isChatActive) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 300.dp)
                        .verticalScroll(chatScrollState)
                        .padding(horizontal = 16.dp)
                ) {
                    chatHistory.forEach { message ->
                        ChatMessageItem(message = message)
                    }
                }
            }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextField(
                        value = chatText,
                        onValueChange = { viewModel.updateMinimalAiChatText(it) },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text(stringResource(R.string.ask_airnote_ai)) },
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = MaterialTheme.colorScheme.primary,
                        ),
                        maxLines = 5
                    )

                    Box(modifier = Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                        } else {
                            IconButton(
                                onClick = { viewModel.sendMinimalAiMessage() },
                                enabled = chatText.isNotBlank()
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.Send,
                                    contentDescription = stringResource(R.string.send),
                                    tint = if (chatText.isNotBlank()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
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
private fun InputActionButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.5f),
            disabledContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.alpha(if (enabled) 1f else 0.5f)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, maxLines = 1)
    }
}


@Composable
fun ChatMessageItem(message: ChatMessage) {
    val isUser = message.participant == Participant.USER
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = when (message.participant) {
        Participant.USER -> MaterialTheme.colorScheme.primaryContainer
        Participant.MODEL -> MaterialTheme.colorScheme.surfaceContainerHigh
        Participant.ERROR -> MaterialTheme.colorScheme.errorContainer
    }
    val textColor = when (message.participant) {
        Participant.USER -> MaterialTheme.colorScheme.onPrimaryContainer
        Participant.MODEL -> MaterialTheme.colorScheme.onSurface
        Participant.ERROR -> MaterialTheme.colorScheme.onErrorContainer
    }
    val bubbleShape = RoundedCornerShape(
        topStart = 16.dp,
        topEnd = 16.dp,
        bottomStart = if (isUser) 16.dp else 0.dp,
        bottomEnd = if (isUser) 0.dp else 16.dp
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = alignment
    ) {
        if (message.isLoading) {
            CircularProgressIndicator(modifier = Modifier.size(24.dp))
        } else {
            Surface(
                color = bubbleColor,
                shape = bubbleShape,
                modifier = Modifier.widthIn(max = 300.dp)
            ) {
                Text(
                    text = message.text.replace(Regex("[*#]"), "").trim(),
                    style = MaterialTheme.typography.bodyLarge,
                    color = textColor,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun AiAction.getIcon(): ImageVector {
    return when (this) {
        AiAction.IMPROVE_WRITING -> Icons.Rounded.Spellcheck
        AiAction.SUMMARIZE -> Icons.Rounded.Summarize
        AiAction.MAKE_SHORTER -> Icons.Rounded.ArrowDownward
        AiAction.MAKE_LONGER -> Icons.Rounded.ArrowUpward
        AiAction.CHANGE_TONE -> Icons.Rounded.Tune
        AiAction.TRANSLATE -> Icons.Rounded.Translate
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
        AiAction.TRANSLATE -> stringResource(id = R.string.translate)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TranslateLanguageSheet(viewModel: EditViewModel) {
    val languages = viewModel.downloadedLanguages.value
    ModalBottomSheet(onDismissRequest = { viewModel.toggleTranslateSheet(false) }) {
        if (languages.isEmpty()) {
            Text(
                text = stringResource(R.string.no_downloaded_languages),
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
        } else {
            LazyColumn {
                items(languages) { (code, name) ->
                    ListItem(
                        headlineContent = { Text(name) },
                        modifier = Modifier.clickable {
                            viewModel.executeTranslation(code)
                        }
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
