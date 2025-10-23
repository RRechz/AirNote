package com.babelsoftware.airnote.presentation.screens.home.desktop

import android.net.Uri
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
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.AddComment
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Notes
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.data.repository.AiMode
import com.babelsoftware.airnote.domain.model.AiChatSession
import com.babelsoftware.airnote.domain.model.Participant
import com.babelsoftware.airnote.presentation.screens.home.viewmodel.DraftedNote
import com.babelsoftware.airnote.presentation.screens.home.viewmodel.HomeViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun DesktopAiAssistantDialog(
    viewModel: HomeViewModel,
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .fillMaxHeight(0.85f),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            AiChatContainer(viewModel = viewModel)
        }
    }
}

// =================================================================
// === Enhanced AirNote AI Interface
// =================================================================

@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun AiChatContainer(viewModel: HomeViewModel) {
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
private fun AiMainContent(viewModel: HomeViewModel) {
    val chatState by viewModel.chatState.collectAsState()
    var text by remember { mutableStateOf("") }
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

        val showInputBar = chatState.latestDraft == null && chatState.analyzingImageUri == null
        val isLoading = chatState.messages.any { it.isLoading }

        if (showInputBar) {
            if (isChatActive) {
                RedesignedChatInputBar(
                    text = text,
                    onValueChange = { text = it },
                    onSendMessage = { onSendMessage(text) },
                    onImagePickerClicked = { viewModel.requestImageForAnalysis() },
                    enabled = !isLoading,
                    placeholderText = if (chatState.isAwaitingDraftTopic) stringResource(R.string.draft_topic_placeholder) else stringResource(R.string.ask_airnote_ai)
                )
            } else {
                PreChatInputBar(
                    text = text,
                    onValueChange = { text = it },
                    onSendMessage = onSendMessage,
                    onImagePickerClicked = { viewModel.requestImageForAnalysis() },
                    enabled = !isLoading
                )
            }
            AiDisclaimerText()
        }
    }
}

@Composable
private fun ImageAnalysisScreen(imageUri: Uri) {
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
private fun MagicAnalysisAnimation() {
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

    val primaryColor = Color(0xFF33A2FF)

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
private fun DraftDisplayWithImage(draft: DraftedNote, onSave: () -> Unit, onRegenerate: () -> Unit) {
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

@Composable
private fun NewAiHomeScreen(viewModel: HomeViewModel) {
    val chatState by viewModel.chatState.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(bottom = 16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(32.dp))
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
private fun ChatScreenContent(
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
private fun AiHistoryScreen(
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

@OptIn(ExperimentalFoundationApi::class, ExperimentalFoundationApi::class)
@Composable
private fun HistoryItem(session: AiChatSession, onClick: () -> Unit, onDelete: () -> Unit) {
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
private fun AiTopBar(
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
private fun AiCentralGraphic(isThinking: Boolean) {
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
private fun ActionCard(text: String, icon: ImageVector, onClick: () -> Unit) {
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
private fun ChatMessageItem(
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
            } else if (message.text.isNotBlank()) {
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
private fun RedesignedChatInputBar(
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
private fun PreChatInputBar(
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
                        Icons.Default.AttachFile,
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
private fun AiDisclaimerText() {
    val uriHandler = LocalUriHandler.current
    val disclaimerText = stringResource(R.string.ai_disclaimer_text)
    val linkText = stringResource(R.string.ai_disclaimer_link_text)
    val linkUrl = stringResource(R.string.ai_disclaimer_link_url)

    val annotatedString = buildAnnotatedString {
        append(disclaimerText)
        pushStringAnnotation(tag = "URL", annotation = linkUrl)
        withStyle(
            style = SpanStyle(
                color = Color(0xFF33A2FF), // Link color
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
            color = Color.White.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp)
            .padding(bottom = 8.dp)
    )
}

@Composable
private fun AskAiQuestionDialog(
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
private fun CreateDraftDialog(
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
private fun DraftDisplay(draft: DraftedNote?, onSave: () -> Unit, onRegenerate: () -> Unit) {
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
private fun TerminalLoadingIndicator(topic: String) {
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
