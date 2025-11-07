package com.babelsoftware.airnote.presentation.screens.home.viewmodel

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.ImageSearch
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.data.provider.StringProvider
import com.babelsoftware.airnote.data.repository.AiAction
import com.babelsoftware.airnote.data.repository.AiMode
import com.babelsoftware.airnote.data.repository.AiTone
import com.babelsoftware.airnote.data.repository.GeminiRepository
import com.babelsoftware.airnote.data.repository.SecureStorageRepository
import com.babelsoftware.airnote.domain.model.AiChatMessage
import com.babelsoftware.airnote.domain.model.AiChatSession
import com.babelsoftware.airnote.domain.model.AiSuggestion
import com.babelsoftware.airnote.domain.model.ChatMessage
import com.google.ai.client.generativeai.type.ImagePart
import com.google.ai.client.generativeai.type.TextPart
import com.google.ai.client.generativeai.type.Part
import com.babelsoftware.airnote.domain.model.Folder
import com.babelsoftware.airnote.domain.model.Note
import com.babelsoftware.airnote.domain.model.Participant
import com.babelsoftware.airnote.domain.repository.SettingsRepository
import com.babelsoftware.airnote.domain.usecase.AiChatUseCase
import com.babelsoftware.airnote.domain.usecase.FolderUseCase
import com.babelsoftware.airnote.domain.usecase.NoteUseCase
import com.babelsoftware.airnote.presentation.components.DecryptionResult
import com.babelsoftware.airnote.presentation.components.EncryptionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class DraftedNote(
    val topic: String,
    val title: String,
    val content: String,
    val sourceImageUri: Uri? = null
)

data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isAwaitingDraftTopic: Boolean = false,
    val latestDraft: DraftedNote? = null,
    val hasStartedConversation: Boolean = false,
    val currentSessionId: Long? = null,
    val analyzingImageUri: Uri? = null,
    val pendingAttachmentUri: Uri? = null,
    val pendingAttachmentMimeType: String? = null
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val stringProvider: StringProvider,
    val encryptionHelper: EncryptionHelper,
    private val noteUseCase: NoteUseCase,
    private val folderUseCase: FolderUseCase,
    private val aiChatUseCase: AiChatUseCase,
    @ApplicationContext private val context: Context,
    private val geminiRepository: GeminiRepository,
    private val settingsRepository: SettingsRepository,
    private val secureStorageRepository: SecureStorageRepository
) : ViewModel() {
    sealed class UiAction {
        object RequestImageForAnalysis : UiAction()
        object RequestFileForAnalysis : UiAction()
        object RequestAttachmentType: UiAction()
    }
    var selectedNotes = mutableStateListOf<Note>()

    private var _isDeleteMode = mutableStateOf(false)
    val isDeleteMode: State<Boolean> = _isDeleteMode

    private var _isPasswordPromptVisible = mutableStateOf(false)
    val isPasswordPromptVisible: State<Boolean> = _isPasswordPromptVisible

    private val _isVaultMode = MutableStateFlow(false)
    val isVaultMode: StateFlow<Boolean> = _isVaultMode.asStateFlow()

    private val _isAddFolderDialogVisible = mutableStateOf(false)
    val isAddFolderDialogVisible: State<Boolean> = _isAddFolderDialogVisible

    private val _isMoveToFolderDialogVisible = mutableStateOf(false)
    val isMoveToFolderDialogVisible: State<Boolean> = _isMoveToFolderDialogVisible

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFolderId = MutableStateFlow<Long?>(null)
    val selectedFolderId: StateFlow<Long?> = _selectedFolderId.asStateFlow()

    private val _folderForAction = mutableStateOf<Folder?>(null)
    val folderForAction: State<Folder?> = _folderForAction

    private val _showDeleteConfirmDialog = mutableStateOf(false)
    val showDeleteConfirmDialog: State<Boolean> = _showDeleteConfirmDialog

    private val _folderToEdit = mutableStateOf<Folder?>(null)
    val folderToEdit: State<Folder?> = _folderToEdit

    private val _noteForAction = mutableStateOf<Note?>(null)
    val noteForAction: State<Note?> = _noteForAction

    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private suspend fun getApiKeyToUse(): String {
        return secureStorageRepository.getUserApiKey() ?: ""
    }

    private val _selectedNote = MutableStateFlow<Note?>(null)
    val selectedNote: StateFlow<Note?> = _selectedNote.asStateFlow()

    fun selectNote(note: Note?) {
        _selectedNote.value = note
    }

    // --- Global AI Chat States ---
    private val _isAiChatSheetVisible = mutableStateOf(false)
    val isAiChatSheetVisible: State<Boolean> = _isAiChatSheetVisible

    private val _isFabExtended = mutableStateOf(true)
    val isFabExtended: State<Boolean> = _isFabExtended

    private val _chatState = MutableStateFlow(ChatState())
    val chatState: StateFlow<ChatState> = _chatState.asStateFlow()

    private val _uiActionChannel = Channel<UiAction>()
    val uiActionChannel = _uiActionChannel.receiveAsFlow()

    private val _aiMode = MutableStateFlow(AiMode.NOTE_ASSISTANT)
    val aiMode: StateFlow<AiMode> = _aiMode.asStateFlow()

    val allChatSessions: StateFlow<List<AiChatSession>> = aiChatUseCase.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val _showAiHistoryScreen = mutableStateOf(false)
    val showAiHistoryScreen: State<Boolean> = _showAiHistoryScreen

    private val _showAskQuestionDialog = mutableStateOf(false)
    val showAskQuestionDialog: State<Boolean> = _showAskQuestionDialog

    private val _showCreateDraftDialog = mutableStateOf(false)
    val showCreateDraftDialog: State<Boolean> = _showCreateDraftDialog

    val allNotesForAi: StateFlow<List<Note>> = noteUseCase.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /**
     * Finds the note mentioned in the message text.
     * Searches for an expression starting with “@” that matches the name of a note in the note list.
     * Sorts the notes in reverse order to check the longest-named notes first.
     */
    private fun findMentionedNote(message: String, allNotes: List<Note>): Note? {
        val sortedNotes = allNotes.sortedByDescending { it.name.length }

        for (note in sortedNotes) {
            val noteNamePattern = Regex.escape(note.name)
            val noteMentionRegex = Regex(
                pattern = "@$noteNamePattern(\\b|$)",
                option = RegexOption.IGNORE_CASE
            )

            if (noteMentionRegex.containsMatchIn(message)) {
                return note
            }
        }

        return null
    }

    fun onAskQuestionClicked() {
        _showAskQuestionDialog.value = true
    }

    fun onDismissQuestionDialog() {
        _showAskQuestionDialog.value = false
    }

    fun onDraftAnythingClicked() {
        _showCreateDraftDialog.value = true
    }

    fun onDismissCreateDraftDialog() {
        _showCreateDraftDialog.value = false
    }

    val suggestions: List<AiSuggestion> by lazy {
        listOf(
            AiSuggestion(
                title = stringProvider.getString(R.string.ask_ai),
                icon = Icons.Rounded.Search,
                action = { onAskQuestionClicked() }
            ),
            AiSuggestion(
                title = stringProvider.getString(R.string.make_note),
                icon = Icons.Rounded.Edit,
                action = { onDraftAnythingClicked() }
            ),
            AiSuggestion(
                title = stringProvider.getString(R.string.generate_ideas),
                icon = Icons.Rounded.AutoAwesome,
                action = {
                    setAiMode(AiMode.CREATIVE_MIND)
                    sendMessage(stringProvider.getString(R.string.generate_ideas_prompt))
                }
            ),
            AiSuggestion(
                title = stringProvider.getString(R.string.create_note_from_object),
                icon = Icons.Rounded.ImageSearch,
                action = { requestImageForAnalysis() },
            )
        )
    }

    init {
        _chatState.asStateFlow().flatMapLatest { state: ChatState ->
            state.currentSessionId?.let { sessionId: Long ->
                aiChatUseCase.getMessagesForSession(sessionId)
            } ?: flowOf(emptyList())
        }.onEach { dbMessages: List<AiChatMessage> ->
            val chatMessages = dbMessages.map { dbMessage: AiChatMessage ->
                ChatMessage(dbMessage.text, dbMessage.participant, dbMessage.isLoading)
            }
            if (_chatState.value.latestDraft == null) {
                _chatState.value = _chatState.value.copy(messages = chatMessages)
            }
        }.launchIn(viewModelScope)


        noteUseCase.observe()
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            if (settings.openToLastUsedFolder && settings.lastUsedFolderId != null) {
                _selectedFolderId.value = settings.lastUsedFolderId
                settingsRepository.update(settings.copy(lastUsedFolderId = null))
            }
        }
    }

    // --- Global AI Chat Functions ---
    fun setAiMode(mode: AiMode) {
        _aiMode.value = mode
        viewModelScope.launch {
            _chatState.value.currentSessionId?.let {
                aiChatUseCase.updateSessionMode(it, mode)
            }
        }
    }

    fun toggleAiHistoryScreen(show: Boolean) {
        _showAiHistoryScreen.value = show
    }

    fun startNewChat() {
        resetChatState()
        toggleAiHistoryScreen(false)
    }

    fun loadChatSession(session: AiChatSession) {
        viewModelScope.launch {
            _chatState.value = ChatState(
                currentSessionId = session.id,
                hasStartedConversation = true
            )
            setAiMode(AiMode.valueOf(session.aiMode))
            toggleAiHistoryScreen(false)
        }
    }

    fun deleteChatSession(sessionId: Long) {
        viewModelScope.launch {
            aiChatUseCase.deleteSessionById(sessionId)
            if (_chatState.value.currentSessionId == sessionId) {
                resetChatState()
            }
        }
    }

    fun toggleAiChatSheet(isVisible: Boolean) {
        _isAiChatSheetVisible.value = isVisible
        if (!isVisible) {
            resetChatState()
            toggleAiHistoryScreen(false)
        }
    }

    fun setFabExtended(isExtended: Boolean) {
        _isFabExtended.value = isExtended
    }

    fun onAttachmentIconClicked() {
        viewModelScope.launch {
            _uiActionChannel.send(UiAction.RequestAttachmentType)
        }
    }

    fun onAttachmentSelected(uri: Uri, mimeType: String?) {
        _chatState.value = _chatState.value.copy(
            pendingAttachmentUri = uri,
            pendingAttachmentMimeType = mimeType
        )
    }

    fun onAttachmentRemoved() {
        _chatState.value = _chatState.value.copy(
            pendingAttachmentUri = null,
            pendingAttachmentMimeType = null
        )
    }

    private val createNoteTriggers = setOf(
        stringProvider.getString(R.string.analyze_create_note),
        stringProvider.getString(R.string.analyze_analyze),
        stringProvider.getString(R.string.analyze_take_notes),
        stringProvider.getString(R.string.analyze_summarize_and_take_notes),
        stringProvider.getString(R.string.analyze_prepare_a_draft),
        stringProvider.getString(R.string.analyze_analyze_my_file_image))

    fun sendMessage(userMessage: String) = viewModelScope.launch {
        val currentState = _chatState.value
        val attachmentUri = currentState.pendingAttachmentUri
        val attachmentMime = currentState.pendingAttachmentMimeType

        if (attachmentUri != null && attachmentMime != null) {
            val isCreateNoteRequest = createNoteTriggers.any { userMessage.contains(it, ignoreCase = true) }

            if (isCreateNoteRequest) {
                analyzeFileAndCreateDraft(userMessage, attachmentUri, attachmentMime)
            } else {
                sendChatWithAttachment(userMessage, attachmentUri, attachmentMime)
            }
        } else {
            sendChatOnly(userMessage)
        }
    }

    private fun sendChatOnly(userMessage: String) = viewModelScope.launch {
        var sessionId = _chatState.value.currentSessionId
        if (sessionId == null) {
            val newSessionId = aiChatUseCase.startNewSession(userMessage.take(40), _aiMode.value)
            _chatState.value = _chatState.value.copy(currentSessionId = newSessionId, hasStartedConversation = true)
            sessionId = newSessionId
        }

        val allNotes = allNotesForAi.first()
        val mentionedNote = findMentionedNote(userMessage, allNotes)

        val userChatMessage = ChatMessage(userMessage, Participant.USER)
        aiChatUseCase.addMessageToSession(sessionId, userChatMessage)

        val loadingMessage = ChatMessage("", Participant.MODEL, isLoading = true)
        val loadingMessageId = aiChatUseCase.addMessageToSession(sessionId, loadingMessage)

        val historyForApi = (_chatState.value.messages + userChatMessage).filter { !it.isLoading }

        val responseBuilder = StringBuilder()

        // Kai AI Modification Tags
        val TAG_UPDATE = "<NOTE_UPDATE_CONTENT>"
        val TAG_UPDATE_END = "</NOTE_UPDATE_CONTENT>"
        val TAG_APPEND = "<NOTE_APPEND_CONTENT>"
        val TAG_APPEND_END = "</NOTE_APPEND_CONTENT>"
        val TAG_DELETE = "<NOTE_DELETE_SELF>"
        val TAG_DELETE_END = "</NOTE_DELETE_SELF>"
        val TAG_RENAME = "<NOTE_CHANGE_TITLE>"
        val TAG_RENAME_END = "</NOTE_CHANGE_TITLE>"

        geminiRepository.generateChatResponse(historyForApi, getApiKeyToUse(), _aiMode.value, mentionedNote, null)
            .onCompletion {
                val fullResponse = responseBuilder.toString().trim()
                var confirmationMsg: String? = null
                var isError = false

                if (mentionedNote != null) {
                    try {
                        when {
                            fullResponse.contains(TAG_DELETE, ignoreCase = true) -> {
                                deleteNoteById(mentionedNote.id)
                                confirmationMsg = stringProvider.getString(R.string.ai_confirmation_note_deleted, mentionedNote.name)
                            }

                            fullResponse.contains(TAG_RENAME, ignoreCase = true) -> {
                                val newTitle = fullResponse.substringAfter(TAG_RENAME, "")
                                    .substringBefore(TAG_RENAME_END, "").trim()
                                if (newTitle.isNotBlank()) {
                                    updateNote(mentionedNote.copy(name = newTitle))
                                    confirmationMsg = stringProvider.getString(R.string.ai_confirmation_note_renamed, mentionedNote.name, newTitle)
                                }
                            }

                            fullResponse.contains(TAG_APPEND, ignoreCase = true) -> {
                                val contentToAppend = fullResponse.substringAfter(TAG_APPEND, "")
                                    .substringBefore(TAG_APPEND_END, "").trim()
                                val newContent = mentionedNote.description + "\n" + contentToAppend
                                updateNote(mentionedNote.copy(description = newContent))
                                confirmationMsg = stringProvider.getString(R.string.ai_confirmation_note_appended, mentionedNote.name)
                            }

                            fullResponse.contains(TAG_UPDATE, ignoreCase = true) -> {
                                val newContent = fullResponse.substringAfter(TAG_UPDATE, "")
                                    .substringBefore(TAG_UPDATE_END, "").trim()
                                updateNote(mentionedNote.copy(description = newContent))
                                confirmationMsg = stringProvider.getString(R.string.ai_confirmation_note_updated, mentionedNote.name)
                            }
                        }
                    } catch (e: Exception) {
                        confirmationMsg = stringProvider.getString(R.string.ai_error_modification_failed)
                        isError = true
                        e.printStackTrace()
                    }
                }

                val finalMessageText = confirmationMsg ?: fullResponse
                if (finalMessageText.isBlank() || (confirmationMsg == null && (fullResponse.startsWith("<") && fullResponse.endsWith(">")))) {
                    if (!isError) {
                        aiChatUseCase.deleteMessageById(loadingMessageId)
                    }
                } else {
                    aiChatUseCase.updateMessageById(loadingMessageId, finalMessageText, false, isError)
                }
            }
            .collect { chunk ->
                responseBuilder.append(chunk)
            }
    }

    private fun sendChatWithAttachment(userMessage: String, uri: Uri, mimeType: String) = viewModelScope.launch {
        val currentState = _chatState.value
        val apiKey = getApiKeyToUse()

        val attachmentPart: Part? = try {
            if (mimeType.startsWith("image/")) {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                ImagePart(bitmap)
            } else if (mimeType == "text/plain") {
                val textContent = readTextFromUri(uri)
                if (textContent.isBlank()) throw Exception(stringProvider.getString(R.string.file_empty_or_not_read))
                TextPart(textContent)
            } else {
                throw IllegalArgumentException("Unsupported file type: $mimeType")
            }
        } catch (e: Exception) {
            onAttachmentRemoved()
            sendChatOnly(userMessage)
            return@launch
        }

        if (attachmentPart == null) {
            onAttachmentRemoved()
            sendChatOnly(userMessage)
            return@launch
        }

        var sessionId = currentState.currentSessionId
        if (sessionId == null) {
            val newSessionId = aiChatUseCase.startNewSession(userMessage.take(40), _aiMode.value)
            _chatState.value = currentState.copy(currentSessionId = newSessionId, hasStartedConversation = true)
            sessionId = newSessionId
        }

        val userChatMessage = ChatMessage(userMessage, Participant.USER)
        aiChatUseCase.addMessageToSession(sessionId, userChatMessage)
        onAttachmentRemoved()

        val loadingMessage = ChatMessage("", Participant.MODEL, isLoading = true)
        val loadingMessageId = aiChatUseCase.addMessageToSession(sessionId, loadingMessage)

        val historyForApi = (currentState.messages + userChatMessage).filter { !it.isLoading }
        val responseBuilder = StringBuilder()

        geminiRepository.generateChatResponse(
            historyForApi,
            apiKey,
            _aiMode.value,
            null,
            attachmentPart
        )
            .onCompletion {
                val fullResponse = responseBuilder.toString().trim()
                if (fullResponse.isBlank()) {
                    aiChatUseCase.deleteMessageById(loadingMessageId)
                }
                else {
                    aiChatUseCase.updateMessageById(loadingMessageId, fullResponse, false, false)
                }
            }
            .collect { chunk ->
                responseBuilder.append(chunk)
            }
    }

    fun generateDraft(topic: String) = viewModelScope.launch {
        resetChatState()
        setAiMode(AiMode.NOTE_ASSISTANT)

        val sessionId = aiChatUseCase.startNewSession(topic.take(40), _aiMode.value)

        _chatState.value = _chatState.value.copy(currentSessionId = sessionId, hasStartedConversation = true)

        val userMessage = ChatMessage(text = topic, participant = Participant.USER)
        aiChatUseCase.addMessageToSession(sessionId, userMessage)

        val loadingMessage = ChatMessage("// Generating Note...", Participant.MODEL, isLoading = true)
        val loadingMessageId = aiChatUseCase.addMessageToSession(sessionId, loadingMessage)

        geminiRepository.generateDraft(topic, getApiKeyToUse(), _aiMode.value)
            .onSuccess { result ->
                val (title, content) = parseDraft(result)
                _chatState.value = _chatState.value.copy(
                    latestDraft = DraftedNote(
                        topic = topic,
                        title = title,
                        content = content,
                        sourceImageUri = null
                    )
                )
                // 8a. Update the loading message to a confirmation message in the DB
                val confirmationMessage = "Harika! '$title' üzerine bir not taslağı hazırladım."
                aiChatUseCase.updateMessageById(loadingMessageId, confirmationMessage, false)
            }
            .onFailure { exception ->
                val errorMessage = exception.message ?: stringProvider.getString(R.string.error_message_image_analysis_failed)
                aiChatUseCase.updateMessageById(loadingMessageId, errorMessage, false, isError = true)
                _chatState.value = _chatState.value.copy(latestDraft = null)
            }
    }

    private fun parseDraft(result: String): Pair<String, String> {
        val titleRegex = """(TITLE:|\*\*TITLE:\*\*)\s*(.*)""".toRegex(RegexOption.IGNORE_CASE)
        val contentRegex = """(CONTENT:|\*\*CONTENT:\*\*)\s*(.*)""".toRegex(setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))

        val titleMatch = titleRegex.find(result)
        val title = titleMatch?.groupValues?.get(2)?.trim()?.replace(Regex("[*#]"), "") ?: "Generated Note"

        val contentMatch = contentRegex.find(result)
        val content = contentMatch?.groupValues?.get(2)?.trim()?.replace(Regex("[*#]"), "") ?: ""

        return Pair(title, content)
    }

    private fun parseAndSetDraft(result: String, topic: String, imageUri: Uri?) {
        val (title, content) = parseDraft(result)
        _chatState.value = _chatState.value.copy(
            messages = emptyList(),
            latestDraft = DraftedNote(
                topic = topic,
                title = title,
                content = content,
                sourceImageUri = imageUri
            ),
            analyzingImageUri = null
        )
    }

    private fun handleFailure(exception: Throwable) {
        val finalMessages = listOf(
            ChatMessage(
                text = exception.message ?: stringProvider.getString(R.string.error_message_image_analysis_failed),
                participant = Participant.ERROR
            )
        )
        _chatState.value = _chatState.value.copy(
            messages = finalMessages,
            latestDraft = null,
            analyzingImageUri = null
        )
    }

    private suspend fun getOrCreateDreamFolderId(): Long? {
        val dreamFolderName = stringProvider.getString(R.string.dream_journal)
        val foldersFlow = folderUseCase.getAllFolders()
        var folders = foldersFlow.first()
        var dreamFolder = folders.find { it.name.equals(dreamFolderName, ignoreCase = true) }

        if (dreamFolder != null) {
            return dreamFolder.id
        }

        val newFolder = Folder(name = dreamFolderName, iconName = "Book")
        try {
            folderUseCase.addFolder(newFolder)
            folders = foldersFlow.first()
            dreamFolder = folders.find { it.name.equals(dreamFolderName, ignoreCase = true) }
            return dreamFolder?.id
        } catch (e: Exception) {
            e.printStackTrace()
            folders = foldersFlow.first()
            dreamFolder = folders.find { it.name.equals(dreamFolderName, ignoreCase = true) }
            return dreamFolder?.id
        }
    }

    fun onDreamJournalClicked(onNoteClicked: (noteId: Int, isVault: Boolean, folderId: Long?) -> Unit) {
        viewModelScope.launch {
            val folderId = getOrCreateDreamFolderId()
            onNoteClicked(0, _isVaultMode.value, folderId)
        }
    }


    fun saveDraftedNote() = viewModelScope.launch {
        val draft = _chatState.value.latestDraft ?: return@launch
        noteUseCase.addNote(
            Note(
                name = draft.title,
                description = draft.content,
                folderId = selectedFolderId.value,
                encrypted = isVaultMode.value
            )
        )
        resetChatState()
        toggleAiChatSheet(false)
    }

    fun requestImageForAnalysis() {
        viewModelScope.launch {
            _uiActionChannel.send(UiAction.RequestImageForAnalysis)
        }
    }

    fun analyzeFileAndCreateDraft(prompt: String, uri: Uri, mimeType: String) = viewModelScope.launch {
        _chatState.value = _chatState.value.copy(
            latestDraft = null,
            messages = listOf(ChatMessage(text = "", participant = Participant.MODEL, isLoading = true)),
            hasStartedConversation = true,
            analyzingImageUri = if (mimeType.startsWith("image/")) uri else null,
            pendingAttachmentUri = null,
            pendingAttachmentMimeType = null
        )

        val apiKey = getApiKeyToUse()

        val attachmentPart: Part? = try {
            if (mimeType.startsWith("image/")) {
                val bitmap: Bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri))
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                ImagePart(bitmap)
            } else if (mimeType == "text/plain") {
                val textContent = readTextFromUri(uri)
                if (textContent.isBlank()) throw Exception(stringProvider.getString(R.string.file_empty_or_not_read))
                TextPart(textContent)
            } else {
                throw IllegalArgumentException("Desteklenmeyen dosya türü: $mimeType")
            }
        } catch (e: Exception) {
            handleFailure(e)
            null
        }

        if (attachmentPart == null) return@launch

        geminiRepository.generateDraftFromAttachment(prompt, attachmentPart, apiKey, _aiMode.value)
            .onSuccess { result ->
                parseAndSetDraft(result, prompt, if (mimeType.startsWith("image/")) uri else null)
            }
            .onFailure { exception ->
                handleFailure(exception)
            }
    }


    fun regenerateDraft() {
        val draft = _chatState.value.latestDraft ?: return
        val sourceImageUri = draft.sourceImageUri
        _chatState.value = _chatState.value.copy(latestDraft = null)

        if (sourceImageUri != null) {
            analyzeFileAndCreateDraft(draft.topic, sourceImageUri, "image/*")
        } else {
            generateDraft(draft.topic)
        }
    }

    fun resetChatState() {
        _chatState.value = _chatState.value.copy(
            messages = emptyList(),
            isAwaitingDraftTopic = false,
            latestDraft = null,
            hasStartedConversation = false,
            analyzingImageUri = null,
            pendingAttachmentUri = null,
            pendingAttachmentMimeType = null
        )
        setAiMode(AiMode.NOTE_ASSISTANT)
    }

    private fun readTextFromUri(uri: Uri): String {
        return try {
            context.contentResolver.openInputStream(uri)?.bufferedReader().use { reader ->
                reader?.readText()
            } ?: ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }
    }

    // --- Note and Folder Functions ---

    val allFolders: StateFlow<List<Folder>> = folderUseCase.getAllFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val displayedNotes: StateFlow<List<Note>> =
        combine<List<Note>, Long?, Boolean, String, List<Note>>(
            noteUseCase.getAllNotes(),
            selectedFolderId,
            isVaultMode,
            searchQuery
        ) { allNotes, folderId, isVault, query ->
            val notesAfterFolderFilter = if (folderId == null) {
                allNotes
            } else {
                allNotes.filter { it.folderId == folderId }
            }
            val notesAfterVaultFilter = notesAfterFolderFilter.filter { it.encrypted == isVault }

            if (query.isBlank()) {
                notesAfterVaultFilter
            } else {
                notesAfterFolderFilter.filter { note ->
                    note.name.contains(query, ignoreCase = true) ||
                            note.description.contains(query, ignoreCase = true)
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onFolderLongPressed(folder: Folder) {
        _folderForAction.value = folder
    }

    fun onDismissFolderAction() {
        _folderForAction.value = null
        _showDeleteConfirmDialog.value = false
    }

    fun onEditFolderRequest() {
        _folderToEdit.value = _folderForAction.value
        onDismissFolderAction()
    }

    fun onDeleteFolderRequest() {
        _showDeleteConfirmDialog.value = true
    }

    fun confirmFolderDeletion() {
        viewModelScope.launch {
            _folderForAction.value?.let { folder ->
                folderUseCase.deleteFolderAndUnlinkNotes(folder)
            }
            onDismissFolderAction()
        }
    }

    fun onDismissEditFolderDialog() {
        _folderToEdit.value = null
    }

    fun setAddFolderDialogVisibility(isVisible: Boolean) {
        _isAddFolderDialogVisible.value = isVisible
    }



    fun toggleIsDeleteMode(enabled: Boolean) {
        _isDeleteMode.value = enabled
    }

    fun toggleIsVaultMode(enabled: Boolean) {
        _isVaultMode.value = enabled
        if (!enabled) {
            noteUseCase.decryptionResult = DecryptionResult.LOADING
        }
        noteUseCase.observe()
    }


    fun toggleIsPasswordPromptVisible(enabled: Boolean) {
        _isPasswordPromptVisible.value = enabled
    }

    fun changeSearchQuery(newValue: String) {
        selectNote(null)
        _searchQuery.value = newValue
    }

    fun pinOrUnpinNotes() {
        if (selectedNotes.all { it.pinned }) {
            selectedNotes.forEach { note ->
                val updatedNote = note.copy(pinned = false)
                noteUseCase.pinNote(updatedNote)
            }
        } else {
            selectedNotes.forEach { note ->
                val updatedNote = note.copy(pinned = true)
                noteUseCase.pinNote(updatedNote)
            }
        }
        selectedNotes.clear()
    }

    fun selectFolder(folderId: Long?) {
        selectNote(null)
        _selectedFolderId.value = folderId
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            if (settings.openToLastUsedFolder) {
                settingsRepository.update(settings.copy(lastUsedFolderId = folderId))
            }
        }
    }

    fun addFolder(name: String, iconName: String) {
        viewModelScope.launch {
            folderUseCase.addFolder(Folder(name = name, iconName = iconName))
            setAddFolderDialogVisibility(false)
        }
    }

    fun setMoveToFolderDialogVisibility(isVisible: Boolean) {
        _isMoveToFolderDialogVisible.value = isVisible
    }

    fun moveSelectedNotesToFolder(targetFolderId: Long) {
        viewModelScope.launch {
            val movedNotesCount = selectedNotes.size
            selectedNotes.forEach { note ->
                noteUseCase.addNote(note.copy(folderId = targetFolderId))
            }
            selectedNotes.clear()
            setMoveToFolderDialogVisibility(false)
            _uiEvent.send("$movedNotesCount not taşındı.")
        }
    }

    fun onAddNewNoteClicked(onNoteClicked: (noteId: Int, isVault: Boolean, folderId: Long?) -> Unit) {
        val targetFolderId = selectedFolderId.value
        onNoteClicked(0, isVaultMode.value, targetFolderId)
    }

    fun updateNote(note: Note) {
        viewModelScope.launch {
            noteUseCase.addNote(note)
        }
    }

    fun updateNoteDetails(noteToUpdate: Note, newName: String, newDescription: String) {
        viewModelScope.launch {
            val updatedNote = noteToUpdate.copy(name = newName, description = newDescription)
            noteUseCase.addNote(updatedNote)
        }
    }

    fun onNoteLongPressed(note: Note) {
        _noteForAction.value = note
    }

    fun onDismissNoteAction() {
        _noteForAction.value = null
    }

    fun deleteNoteAction() {
        _noteForAction.value?.let { noteToDelete ->
            deleteNoteById(noteToDelete.id)
        }
        onDismissNoteAction()
    }

    fun requestMoveNoteAction() {
        _noteForAction.value?.let { noteToMove ->
            selectedNotes.clear()
            selectedNotes.add(noteToMove)
            setMoveToFolderDialogVisibility(true)
        }
        onDismissNoteAction()
    }

    fun updateFolder(name: String, iconName: String) {
        _folderToEdit.value?.let { folder ->
            viewModelScope.launch {
                val updatedFolder = folder.copy(name = name, iconName = iconName)
                folderUseCase.updateFolder(updatedFolder)
                onDismissEditFolderDialog()
            }
        }
    }

    fun deleteNoteById(id: Int) {
        viewModelScope.launch {
            noteUseCase.deleteNoteById(id)
        }
    }

    fun observeNotes() {
        noteUseCase.observe()
    }

    fun createNewNoteForDesktop() {
        viewModelScope.launch {
            val targetFolderId = _selectedFolderId.value
            val newNote = Note(
                name = "Untitled Note",
                description = "",
                folderId = targetFolderId,
                encrypted = isVaultMode.value
            )
            noteUseCase.addNote(newNote)

            val latestNote = noteUseCase.getAllNotes().first().maxByOrNull { it.createdAt }
            if (latestNote != null) {
                selectNote(latestNote)
            }
        }
    }

    fun executeDesktopAiAction(action: AiAction, tone: AiTone? = null) {
        val currentNote = _selectedNote.value ?: return
        val currentDescription = currentNote.description

        viewModelScope.launch {
            geminiRepository.processAiAction(
                action = action,
                text = currentDescription,
                tone = tone,
                apiKey = getApiKeyToUse(),
                aiMode = _aiMode.value
            )
                .onSuccess { result ->
                    updateNoteDetails(currentNote, currentNote.name, result)
                }
                .onFailure { exception ->
                    _uiEvent.send(exception.message ?: "AI işlemi başarısız oldu.")
                }
        }
    }
}
