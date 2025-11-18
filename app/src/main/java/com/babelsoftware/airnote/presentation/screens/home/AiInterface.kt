package com.babelsoftware.airnote.presentation.screens.home

import android.content.ActivityNotFoundException
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
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
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.AddComment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.FilePresent
import androidx.compose.material.icons.rounded.GolfCourse
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.data.repository.AiMode
import com.babelsoftware.airnote.domain.model.AiChatSession
import com.babelsoftware.airnote.domain.model.Participant
import com.babelsoftware.airnote.domain.model.Settings
import com.babelsoftware.airnote.presentation.screens.home.viewmodel.AiService
import com.babelsoftware.airnote.presentation.screens.home.viewmodel.DraftedNote
import com.babelsoftware.airnote.presentation.screens.home.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

// =================================================================
// === Enhanced AirNote Kai AI Interface (with THEME) ======================
// =================================================================

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AiChatContainer(viewModel: HomeViewModel, settings: Settings) {
    val isDark = isSystemInDarkTheme()
    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(
            colors = listOf(Color(0xFF10141C), Color(0xFF0A0D12))
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(MaterialTheme.colorScheme.surface, MaterialTheme.colorScheme.surfaceContainerLowest)
        )
    }

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
            onToggleHistory = { viewModel.toggleAiHistoryScreen(!showHistoryScreen) },
            isPerplexityEnabled = settings.isPerplexityEnabled
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
    val isChatActive = chatState.hasStartedConversation || chatState.messages.isNotEmpty()
    val allNotes by viewModel.allNotesForAi.collectAsState()
    val isDark = isSystemInDarkTheme()
    val selectedService by viewModel.selectedAiService.collectAsState()
    val showAttachmentButton = selectedService == AiService.GEMINI
    val placeholderText = if (selectedService == AiService.GEMINI) {
        if (chatState.isAwaitingDraftTopic) stringResource(R.string.draft_topic_placeholder) else stringResource(R.string.ask_airnote_ai)
    } else {
        stringResource(R.string.ask_perplexity_placeholder)
    }

    val (mentionQuery, showMentionSuggestions) = remember(text) {
        val cursorPosition = text.length
        val atIndex = text.lastIndexOf('@', cursorPosition - 1)

        if (atIndex != -1) {
            val query = text.substring(atIndex + 1, cursorPosition)
            if (" " !in query && query.length < 30) {
                Pair(query, true)
            } else {
                Pair("", false)
            }
        } else {
            Pair("", false)
        }
    }

    val mentionSuggestions = remember(mentionQuery, allNotes, showMentionSuggestions) {
        if (showMentionSuggestions) {
            allNotes.filter {
                it.name.contains(mentionQuery, ignoreCase = true)
            }.take(5)
        } else {
            emptyList()
        }
    }

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
        if (message.isNotBlank() || (chatState.pendingAttachmentUri != null && showAttachmentButton)) {
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
            val (isLoading, isAnalyzingImage) = remember(chatState) {
                val loadingMessage = chatState.messages.any { it.isLoading }
                val imageUri = chatState.analyzingImageUri
                (loadingMessage to (imageUri != null))
            }

            when {
                chatState.latestDraft != null && chatState.latestDraft?.sourceImageUri != null -> {
                    DraftDisplayWithImage(
                        draft = chatState.latestDraft!!,
                        onSave = { viewModel.saveDraftedNote() },
                        onRegenerate = { viewModel.regenerateDraft() }
                    )
                }

                isLoading && isAnalyzingImage -> {
                    ImageAnalysisScreen(imageUri = chatState.analyzingImageUri!!)
                }

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

        AnimatedVisibility(visible = mentionSuggestions.isNotEmpty() && showAttachmentButton) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .offset(y = (-8).dp),
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                color = if (isDark) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHigh,
                border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outlineVariant)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .heightIn(max = 150.dp)
                        .padding(vertical = 8.dp)
                ) {
                    items(items = mentionSuggestions, key = { it.id }) { note ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    val atIndex = text.lastIndexOf('@')
                                    text = text.substring(0, atIndex + 1) + note.name + " "
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Rounded.FilePresent,
                                contentDescription = null,
                                tint = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(12.dp))
                            Text(
                                text = note.name,
                                color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(visible = chatState.pendingAttachmentUri != null && showAttachmentButton) {
            AttachedFileChip(
                uri = chatState.pendingAttachmentUri,
                mimeType = chatState.pendingAttachmentMimeType,
                onRemove = { viewModel.onAttachmentRemoved() }
            )
        }

        val showInputBar = chatState.latestDraft == null && chatState.analyzingImageUri == null
        val isLoading = chatState.messages.any { it.isLoading }

        if (showInputBar) {
            if (isChatActive) {
                RedesignedChatInputBar(
                    text = text,
                    onValueChange = { text = it },
                    onSendMessage = { onSendMessage(text) },
                    onImagePickerClicked = { viewModel.onAttachmentIconClicked() },
                    enabled = !isLoading,
                    showAttachmentButton = showAttachmentButton,
                    placeholderText = placeholderText
                )
            } else {
                PreChatInputBar(
                    text = text,
                    onValueChange = { text = it },
                    onSendMessage = onSendMessage,
                    onImagePickerClicked = { viewModel.onAttachmentIconClicked() },
                    enabled = !isLoading,
                    showAttachmentButton = showAttachmentButton
                )
            }
            AiDisclaimerText()
        }
    }
}

@Composable
fun AttachedFileChip(
    uri: Uri?,
    mimeType: String?,
    onRemove: () -> Unit
) {
    if (uri == null || mimeType == null) return

    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    val displayName = remember(uri, mimeType) {
        if (mimeType.startsWith("image/")) {
            "Image"
        } else {
            try {
                context.contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DISPLAY_NAME), null, null, null)
                    ?.use { cursor ->
                        if (cursor.moveToFirst()) {
                            cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
                        } else {
                            "File"
                        }
                    } ?: "File"
            } catch (e: Exception) {
                "File"
            }
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .offset(y = (-8).dp),
        shape = RoundedCornerShape(12.dp),
        color = if (isDark) Color.White.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                Icon(
                    imageVector = if (mimeType.startsWith("image/")) Icons.Default.Image else Icons.Default.Description,
                    contentDescription = "File type",
                    tint = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = displayName,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            IconButton(onClick = onRemove, modifier = Modifier.size(24.dp)) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = "Remove file",
                    tint = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}


@Composable
fun ImageAnalysisScreen(imageUri: Uri) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        AsyncImage(
            model = imageUri,
            contentDescription = stringResource(R.string.image_being_analyzed),
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Fit
        )

        MagicAnalysisAnimation()
    }
}

@Composable
fun MagicAnalysisAnimation() {
    val infiniteTransition = rememberInfiniteTransition(label = "analysis_animation")
    val scanLinePosition by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scan_line"
    )

    val particles = remember {
        List(50) {
            Pair(
                Random.nextFloat(), // x
                Random.nextFloat()  // y
            )
        }
    }
    val particleAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particles"
    )

    val primaryColor = if (isSystemInDarkTheme()) Color(0xFF33A2FF) else MaterialTheme.colorScheme.primary

    Canvas(modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(16.dp))) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        val y = scanLinePosition * canvasHeight

        drawLine(
            brush = Brush.verticalGradient(
                colors = listOf(Color.Transparent, primaryColor.copy(alpha = 0.5f), Color.Transparent),
                startY = y - 20f,
                endY = y + 20f
            ),
            start = Offset(x = 0f, y = y),
            end = Offset(x = canvasWidth, y = y),
            strokeWidth = 4f,
            cap = StrokeCap.Round
        )

        particles.forEach { (x, y) ->
            drawCircle(
                color = primaryColor,
                radius = (Random.nextFloat() * 4f + 2f),
                center = Offset(x * canvasWidth, y * canvasHeight),
                alpha = particleAlpha * Random.nextFloat()
            )
        }

        val bracketSize = 30f
        val bracketStroke = 6f

        // Top left
        drawLine(primaryColor, Offset(0f, bracketSize), Offset(0f, 0f), bracketStroke)
        drawLine(primaryColor, Offset(0f, 0f), Offset(bracketSize, 0f), bracketStroke)
        // Top right
        drawLine(primaryColor, Offset(canvasWidth - bracketSize, 0f), Offset(canvasWidth, 0f), bracketStroke)
        drawLine(primaryColor, Offset(canvasWidth, 0f), Offset(canvasWidth, bracketSize), bracketStroke)
        // Lower Left
        drawLine(primaryColor, Offset(0f, canvasHeight - bracketSize), Offset(0f, canvasHeight), bracketStroke)
        drawLine(primaryColor, Offset(0f, canvasHeight), Offset(bracketSize, canvasHeight), bracketStroke)
        // Lower Right
        drawLine(primaryColor, Offset(canvasWidth - bracketSize, canvasHeight), Offset(canvasWidth, canvasHeight), bracketStroke)
        drawLine(primaryColor, Offset(canvasWidth, canvasHeight), Offset(canvasWidth, canvasHeight - bracketSize), bracketStroke)
    }
}

@Composable
fun DraftDisplayWithImage(draft: DraftedNote, onSave: () -> Unit, onRegenerate: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val primaryAccentColor = if (isDark) Color(0xFF33A2FF) else MaterialTheme.colorScheme.primary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        AsyncImage(
            model = draft.sourceImageUri,
            contentDescription = draft.title,
            modifier = Modifier
                .fillMaxWidth()
                .height(150.dp)
                .clip(RoundedCornerShape(16.dp)),
            contentScale = ContentScale.Crop
        )
        Spacer(modifier = Modifier.height(16.dp))
        Surface(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(16.dp),
            color = if (isDark) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceContainer,
            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.outlineVariant)
        ) {
            LazyColumn(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
            ) {
                item {
                    Text(
                        text = draft.title,
                        style = MaterialTheme.typography.titleLarge,
                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                }
                item {
                    Text(
                        text = draft.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
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
                border = BorderStroke(1.dp, primaryAccentColor)
            ) {
                Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = primaryAccentColor)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.regenerate_note), color = primaryAccentColor)
            }
            Button(
                onClick = onSave,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primaryAccentColor,
                    contentColor = if (isDark) Color(0xFF10141C) else MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text(stringResource(R.string.save_note))
            }
        }
    }
}


@Composable
fun NewAiHomeScreen(viewModel: HomeViewModel) {
    val chatState by viewModel.chatState.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()

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
    val isDark = isSystemInDarkTheme()

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
                border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outline),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(Icons.Rounded.AddComment, contentDescription = stringResource(R.string.new_chat))
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.new_chat))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                stringResource(R.string.history),
                style = MaterialTheme.typography.titleMedium,
                color = if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
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
                    color = if (isDark) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
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
    val isDark = isSystemInDarkTheme()

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
        color = if (isDark) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceContainer,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val serviceIcon = if (session.serviceName == AiService.PERPLEXITY.name) {
                Icons.Rounded.Search
            } else {
                Icons.Rounded.AutoAwesome
            }

            Icon(
                imageVector = serviceIcon,
                contentDescription = session.serviceName,
                tint = if (isDark) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = session.title,
                color = if (isDark) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface,
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
    onToggleHistory: () -> Unit,
    isPerplexityEnabled: Boolean
) {
    val currentAiMode by viewModel.aiMode.collectAsState()
    var showModelMenu by remember { mutableStateOf(false) }
    val selectedService by viewModel.selectedAiService.collectAsState()
    var showServiceMenu by remember { mutableStateOf(false) }
    val isDark = isSystemInDarkTheme()

    val geminiIconUrl = "https://cdn-1.webcatalog.io/catalog/google-bard/google-bard-icon-filled-256.webp?v=1760920816570"
    val perplexityIconUrl = "https://framerusercontent.com/images/gcMkPKyj2RX8EOEja8A1GWvCb7E.jpg?width=2000&height=2000"

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Start) {
            AnimatedVisibility(visible = selectedService == AiService.GEMINI) {
                Box {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (isDark) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceContainerHigh)
                            .clickable { showModelMenu = true }
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (currentAiMode) {
                                AiMode.NOTE_ASSISTANT -> Icons.AutoMirrored.Rounded.Notes
                                AiMode.CREATIVE_MIND -> Icons.Rounded.Psychology
                                AiMode.ACADEMIC_RESEARCHER -> Icons.Rounded.School
                                AiMode.PROFESSIONAL_STRATEGIST -> Icons.Rounded.GolfCourse
                            },
                            contentDescription = "AI Model",
                            tint = if (isDark) Color(0xFF33A2FF) else MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = when (currentAiMode) {
                                AiMode.NOTE_ASSISTANT -> stringResource(R.string.ai_mode_note_assistant)
                                AiMode.CREATIVE_MIND -> stringResource(R.string.ai_mode_creative_mind)
                                AiMode.ACADEMIC_RESEARCHER -> stringResource(R.string.ai_mode_academic_research)
                                AiMode.PROFESSIONAL_STRATEGIST -> stringResource(R.string.ai_mode_professional_strategy)
                            },
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Icon(
                            Icons.Rounded.ExpandMore,
                            contentDescription = "Change Model",
                            tint = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
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
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.ai_mode_academic_research)) },
                            onClick = {
                                viewModel.setAiMode(AiMode.ACADEMIC_RESEARCHER)
                                showModelMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.School,
                                    contentDescription = stringResource(R.string.ai_mode_academic_research)
                                )
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.ai_mode_professional_strategy)) },
                            onClick = {
                                viewModel.setAiMode(AiMode.PROFESSIONAL_STRATEGIST)
                                showModelMenu = false
                            },
                            leadingIcon = {
                                Icon(
                                    Icons.Rounded.GolfCourse,
                                    contentDescription = stringResource(R.string.ai_mode_professional_strategy)
                                )
                            }
                        )
                    }
                }
            }

            Box(modifier = Modifier.padding(start = if (selectedService == AiService.GEMINI) 8.dp else 0.dp)) {
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(if (isDark) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceContainerHigh)
                        .clickable { showServiceMenu = true }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = if (selectedService == AiService.GEMINI) geminiIconUrl else perplexityIconUrl),
                        contentDescription = "AI Service",
                        modifier = Modifier
                            .size(20.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (selectedService == AiService.GEMINI) "Gemini" else "Perplexity",
                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Icon(
                        Icons.Rounded.ExpandMore,
                        contentDescription = "Change Service",
                        tint = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                DropdownMenu(
                    expanded = showServiceMenu,
                    onDismissRequest = { showServiceMenu = false },
                    modifier = Modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
                ) {
                    DropdownMenuItem(
                        text = { Text("Gemini") },
                        onClick = {
                            viewModel.selectAiService(AiService.GEMINI)
                            showServiceMenu = false
                        },
                        leadingIcon = {
                            Image(
                                painter = rememberAsyncImagePainter(model = geminiIconUrl),
                                contentDescription = "Gemini",
                                modifier = Modifier.size(24.dp).clip(CircleShape),
                                contentScale = ContentScale.Crop
                            )
                        }
                    )
                    if (isPerplexityEnabled) {
                        DropdownMenuItem(
                            text = { Text("Perplexity") },
                            onClick = {
                                viewModel.selectAiService(AiService.PERPLEXITY)
                                showServiceMenu = false
                            },
                            leadingIcon = {
                                Image(
                                    painter = rememberAsyncImagePainter(model = perplexityIconUrl),
                                    contentDescription = "Perplexity",
                                    modifier = Modifier.size(24.dp).clip(CircleShape),
                                    contentScale = ContentScale.Crop
                                )
                            }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onToggleHistory) {
            Icon(
                if (showHistory) Icons.AutoMirrored.Filled.Chat else Icons.Rounded.History,
                contentDescription = "Toggle History",
                tint = if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
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

    val isDark = isSystemInDarkTheme()
    val primaryColor = if (isDark) Color(0xFF33A2FF) else MaterialTheme.colorScheme.primary

    Box(
        modifier = Modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.1f), Color.Transparent),
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
                            0.7f to primaryColor,
                            1.0f to Color.Transparent
                        )
                    ),
                    CircleShape
                ),
        )
        Icon(
            imageVector = Icons.Rounded.AutoAwesome,
            contentDescription = "AI Core",
            tint = primaryColor,
            modifier = Modifier.size(50.dp)
        )
    }
}

@Composable
fun ActionCard(text: String, icon: ImageVector, onClick: () -> Unit) {
    val isDark = isSystemInDarkTheme()
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.outlineVariant),
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
                tint = if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = text,
                color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
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
    val isDark = isSystemInDarkTheme()

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

            val textColor = if (isDark) {
                if (message.participant == Participant.ERROR) Color.White else Color.White.copy(alpha = 0.9f)
            } else {
                when (message.participant) {
                    Participant.USER -> MaterialTheme.colorScheme.onPrimaryContainer
                    Participant.MODEL -> MaterialTheme.colorScheme.onSurfaceVariant
                    Participant.ERROR -> MaterialTheme.colorScheme.onErrorContainer
                }
            }

            Box(
                modifier = Modifier
                    .clip(bubbleShape)
                    .then(
                        if (isDark) {
                            Modifier
                                .background(
                                    brush = when (message.participant) {
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
                                )
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
                        } else {
                            Modifier.background(
                                color = when (message.participant) {
                                    Participant.USER -> MaterialTheme.colorScheme.primaryContainer
                                    Participant.MODEL -> MaterialTheme.colorScheme.surfaceVariant
                                    Participant.ERROR -> MaterialTheme.colorScheme.errorContainer
                                }
                            )
                        }
                    )
                    .padding(12.dp)
            ) {
                RenderMessageWithSources(
                    fullText = message.text.replace(Regex("[*#]"), "").trim(),
                    baseColor = textColor,
                    linkColor = if (isDark) Color(0xFF33A2FF) else MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun RenderMessageWithSources(
    fullText: String,
    baseColor: Color,
    linkColor: Color
) {
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current
    val sourcesHeaderText = "**Sources:**"
    val parts = fullText.split(sourcesHeaderText, limit = 2)
    val mainText = parts[0]
    val sourcesText = if (parts.size > 1) parts[1] else null
    val sourceRegex = remember { Regex("""\[\d+\]\s*(.*?)\s*\((https?://[^\s)]+)\)""") }

    val annotatedString = buildAnnotatedString {
        withStyle(style = SpanStyle(color = baseColor)) {
            append(mainText)
        }
        if (sourcesText != null) {
            withStyle(style = SpanStyle(color = baseColor, fontWeight = FontWeight.Bold)) {
                append("\n\n\nSources:")
            }

            var lastIndex = 0
            sourceRegex.findAll(sourcesText).forEach { matchResult ->
                val title = matchResult.groupValues[1]
                val url = matchResult.groupValues[2]
                withStyle(style = SpanStyle(color = baseColor)) {
                    append(sourcesText.substring(lastIndex, matchResult.range.first))
                }
                pushStringAnnotation(tag = "URL", annotation = url)
                withStyle(style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)) {
                    append("\n[${matchResult.value.substring(1,2)}] $title")
                }
                pop()

                lastIndex = matchResult.range.last + 1
            }
            if (lastIndex < sourcesText.length) {
                withStyle(style = SpanStyle(color = baseColor)) {
                    append(sourcesText.substring(lastIndex))
                }
            }
        }
    }

    ClickableText(
        text = annotatedString,
        style = MaterialTheme.typography.bodyLarge,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    try {
                        uriHandler.openUri(annotation.item)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "Web tarayıcısı bulunamadı.", Toast.LENGTH_SHORT).show()
                    } catch (e: IllegalArgumentException) {
                        Toast.makeText(context, "URL açılamadı.", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    )
}

@Composable
fun RedesignedChatInputBar(
    text: String,
    onValueChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onImagePickerClicked: () -> Unit,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    showAttachmentButton: Boolean,
    placeholderText: String
) {
    val haptic = LocalHapticFeedback.current
    val isDark = isSystemInDarkTheme()
    val primaryAccentColor = if (isDark) Color(0xFF33A2FF) else MaterialTheme.colorScheme.primary
    val chatState by hiltViewModel<HomeViewModel>().chatState.collectAsState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        color = if (isDark) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.outline)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AnimatedVisibility(visible = showAttachmentButton) {
                IconButton(onClick = onImagePickerClicked, enabled = enabled) {
                    Icon(
                        Icons.Default.AttachFile,
                        contentDescription = "Attach File",
                        tint = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            TextField(
                value = text,
                onValueChange = onValueChange,
                modifier = Modifier.weight(1f),
                enabled = enabled,
                placeholder = {
                    Text(placeholderText, color = if (isDark) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant)
                },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    cursorColor = primaryAccentColor,
                    focusedTextColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                    unfocusedTextColor = if (isDark) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface,
                    disabledPlaceholderColor = if (isDark) Color.White.copy(alpha = 0.3f) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                )
            )
            AnimatedVisibility(visible = (text.isNotBlank() || chatState.pendingAttachmentUri != null) && enabled) {
                IconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSendMessage()
                    },
                    modifier = Modifier.padding(start = 8.dp),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = primaryAccentColor,
                        contentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onPrimary
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
    showAttachmentButton: Boolean
) {
    val isDark = isSystemInDarkTheme()
    val primaryAccentColor = if (isDark) Color(0xFF33A2FF) else MaterialTheme.colorScheme.primary
    val chatState by hiltViewModel<HomeViewModel>().chatState.collectAsState()

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        shape = RoundedCornerShape(24.dp),
        color = if (isDark) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceContainerHigh,
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.outline)
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
                        Text(stringResource(R.string.ask_airnote_ai), color = if (isDark) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = if (isDark) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = primaryAccentColor,
                        focusedTextColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = if (isDark) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface,
                    ),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AnimatedVisibility(visible = showAttachmentButton) {
                    IconButton(onClick = onImagePickerClicked, enabled = enabled) {
                        Icon(
                            Icons.Default.AttachFile,
                            contentDescription = "Attach File",
                            tint = if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
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
                    onClick = { if (text.isNotBlank() || chatState.pendingAttachmentUri != null) onSendMessage(text) },
                    enabled = (text.isNotBlank() || chatState.pendingAttachmentUri != null) && enabled,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape),
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = primaryAccentColor,
                        contentColor = if (isDark) Color.White else MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = (if (isDark) Color.White else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.12f),
                        disabledContentColor = (if (isDark) Color.White else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.38f)
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
    val isDark = isSystemInDarkTheme()
    TextButton(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.textButtonColors(
            containerColor = if (isDark) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceContainer,
            contentColor = if (isDark) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface
        ),
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outlineVariant),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
fun AiDisclaimerText() {
    val uriHandler = LocalUriHandler.current
    val disclaimerText = stringResource(R.string.ai_disclaimer_text)
    val linkText = stringResource(R.string.ai_disclaimer_link_text)
    val linkUrl = stringResource(R.string.ai_disclaimer_link_url)
    val isDark = isSystemInDarkTheme()

    val annotatedString = buildAnnotatedString {
        append(disclaimerText)
        pushStringAnnotation(tag = "URL", annotation = linkUrl)
        withStyle(
            style = SpanStyle(
                color = if (isDark) Color(0xFF33A2FF) else MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline
            )
        ) {
            append(linkText)
        }
        pop()
    }

    ClickableText(
        text = annotatedString,
        onClick = { offset ->
            annotatedString.getStringAnnotations(tag = "URL", start = offset, end = offset)
                .firstOrNull()?.let { annotation ->
                    uriHandler.openUri(annotation.item)
                }
        },
        style = MaterialTheme.typography.bodySmall.copy(
            color = if (isDark) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp)
    )
}

@Composable
fun AskAiQuestionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var question by remember { mutableStateOf("") }
    val maxChars = 350
    val isDark = isSystemInDarkTheme()
    val primaryAccentColor = if (isDark) Color(0xFF33A2FF) else MaterialTheme.colorScheme.primary

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (isDark) Color(0xFF10141C) else MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Text(
                    text = stringResource(R.string.ask_a_question_title),
                    style = MaterialTheme.typography.headlineSmall,
                    color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
                TextField(
                    value = question,
                    onValueChange = { if (it.length <= maxChars) question = it },
                    placeholder = { Text(stringResource(R.string.ask_a_question), color = if (isDark) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = if (isDark) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceContainer,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = primaryAccentColor,
                        focusedTextColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = if (isDark) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface,
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                Text(
                    text = "${question.length} / $maxChars",
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
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
                        Text(stringResource(R.string.cancel), color = if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(question) },
                        enabled = question.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryAccentColor,
                            contentColor = if (isDark) Color(0xFF10141C) else MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = (if (isDark) Color.White else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.12f)
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
    val maxChars = 250
    val isDark = isSystemInDarkTheme()
    val primaryAccentColor = if (isDark) Color(0xFF33A2FF) else MaterialTheme.colorScheme.primary

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = if (isDark) Color(0xFF10141C) else MaterialTheme.colorScheme.surfaceContainerHigh,
            border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = null,
                        tint = if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.new_ai_note_draft),
                        style = MaterialTheme.typography.headlineSmall,
                        color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = stringResource(R.string.sample_question_request),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isDark) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = topic,
                    onValueChange = { if (it.length <= maxChars) topic = it },
                    placeholder = { Text(stringResource(R.string.example_question), color = if (isDark) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = if (isDark) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = if (isDark) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceContainer,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        cursorColor = primaryAccentColor,
                        focusedTextColor = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = if (isDark) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurface,
                    ),
                    shape = RoundedCornerShape(16.dp)
                )
                Text(
                    text = "${topic.length} / $maxChars",
                    textAlign = TextAlign.End,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDark) Color.White.copy(alpha = 0.6f) else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel), color = if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirm(topic) },
                        enabled = topic.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primaryAccentColor,
                            contentColor = if (isDark) Color(0xFF10141C) else MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = (if (isDark) Color.White else MaterialTheme.colorScheme.onSurface).copy(alpha = 0.12f)
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
    val isDark = isSystemInDarkTheme()
    val primaryAccentColor = if (isDark) Color(0xFF33A2FF) else MaterialTheme.colorScheme.primary

    Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Surface(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(16.dp),
                color = if (isDark) Color.White.copy(alpha = 0.05f) else MaterialTheme.colorScheme.surfaceContainer,
                border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.1f) else MaterialTheme.colorScheme.outlineVariant)
            ) {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
                ) {
                    item {
                        Text(
                            text = draft.title,
                            style = MaterialTheme.typography.titleLarge,
                            color = if (isDark) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )
                    }
                    item {
                        Text(
                            text = draft.content,
                            style = MaterialTheme.typography.bodyLarge,
                            color = if (isDark) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant,
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
                    border = BorderStroke(1.dp, primaryAccentColor)
                ) {
                    Icon(Icons.Rounded.AutoAwesome, contentDescription = null, tint = primaryAccentColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.regenerate_note), color = primaryAccentColor)
                }
                Button(
                    onClick = onSave,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primaryAccentColor,
                        contentColor = if (isDark) Color(0xFF10141C) else MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(stringResource(R.string.save_note))
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
                    color = (if (isSystemInDarkTheme()) Color.White else MaterialTheme.colorScheme.onSurfaceVariant).copy(
                        alpha = 0.5f
                    ),
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
[INFO] Connecting to AirNote's Kai AI services...
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

    val isDark = isSystemInDarkTheme()
    val textColor = if (isDark) Color.Green.copy(alpha = 0.8f) else MaterialTheme.colorScheme.primary

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isDark) Color.Black.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceContainerLowest,
        border = BorderStroke(1.dp, if (isDark) Color.White.copy(alpha = 0.2f) else MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(modifier = Modifier.padding(12.dp)) {
            Text(
                text = displayedText,
                color = textColor,
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp,
                lineHeight = 20.sp
            )
            Text(
                text = "█",
                color = textColor.copy(alpha = cursorAlpha),
                fontFamily = FontFamily.Monospace,
                fontSize = 14.sp
            )
        }
    }
}