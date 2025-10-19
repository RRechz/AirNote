package com.babelsoftware.airnote.presentation.screens.home.desktop

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.babelsoftware.airnote.domain.model.Participant
import com.babelsoftware.airnote.presentation.screens.home.ChatMessageItem
import com.babelsoftware.airnote.presentation.screens.home.DraftDisplay
import com.babelsoftware.airnote.presentation.screens.home.viewmodel.HomeViewModel
import kotlinx.coroutines.launch

/**
 * AI Assistant designed for Desktop mode, shown in a dialog window.
 */
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
            val chatState by viewModel.chatState.collectAsState()
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
                        draft = chatState.latestDraft,
                        onSave = { viewModel.saveDraftedNote() },
                        onRegenerate = { viewModel.regenerateDraft() }
                    )
                    chatState.messages.isNotEmpty() -> {
                        CompactAiChatView(viewModel = viewModel)
                    }
                    else -> {
                        NewAiScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}

/**
 * Redesigned, modern and list-based AI start screen for desktop.
 */
@Composable
private fun NewAiScreen(viewModel: HomeViewModel) {
    val chatState by viewModel.chatState.collectAsState()
    var text by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.padding(top = 32.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "How can I help you?",
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(8.dp)
        ) {
            items(viewModel.suggestions) { suggestion ->
                DesktopSuggestionItem(
                    text = suggestion.title,
                    icon = suggestion.icon,
                    onClick = { viewModel.sendMessage(suggestion.title) }
                )
            }
        }

        ChatInputBar(
            text = text,
            onValueChange = { text = it },
            isAwaitingTopic = chatState.isAwaitingDraftTopic,
            onSendMessage = {
                if (text.isNotBlank()) {
                    if (chatState.isAwaitingDraftTopic) {
                        viewModel.generateDraft(text)
                    } else {
                        viewModel.sendMessage(text)
                    }
                    text = ""
                }
            },
            onImagePickerClicked = { viewModel.requestImageForAnalysis() },
            enabled = !chatState.messages.any { it.isLoading },
            modifier = Modifier.padding(bottom = 8.dp)
        )
    }
}

/**
 * Composable, which makes the messaging interface in HomeScreen.kt reusable.
 */
@Composable
private fun CompactAiChatView(viewModel: HomeViewModel) {
    val chatState by viewModel.chatState.collectAsState()
    var text by remember { mutableStateOf("") }
    val isLoading = chatState.messages.any { it.isLoading }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()

    LaunchedEffect(chatState.messages.size) {
        if (chatState.messages.isNotEmpty()) {
            scope.launch {
                listState.animateScrollToItem(chatState.messages.size - 1)
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
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
                if (text.isNotBlank()) {
                    if (chatState.isAwaitingDraftTopic) {
                        viewModel.generateDraft(text)
                    } else {
                        viewModel.sendMessage(text)
                    }
                    text = ""
                }
            },
            onImagePickerClicked = { viewModel.requestImageForAnalysis() },
            enabled = !isLoading
        )
    }
}

/**
 * Richer and more readable list element designed for desktop AI display.
 */
@Composable
private fun DesktopSuggestionItem(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(text, fontWeight = FontWeight.Medium) },
        leadingContent = {
            Icon(
                imageVector = icon,
                contentDescription = text,
                modifier = Modifier.size(28.dp)
            )
        },
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        )
    )
}

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
        IconButton(onClick = onImagePickerClicked, enabled = enabled) {
            Icon(
                Icons.Default.Image,
                contentDescription = "Add Image",
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
                Text(if (isAwaitingTopic) "Taslak konusunu yazın..." else "Ask AirNote AI...")
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
