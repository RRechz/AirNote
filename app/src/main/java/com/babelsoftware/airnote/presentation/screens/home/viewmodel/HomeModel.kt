package com.babelsoftware.airnote.presentation.screens.home.viewmodel

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Search
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.data.provider.StringProvider
import com.babelsoftware.airnote.data.repository.GeminiRepository
import com.babelsoftware.airnote.data.repository.SecureStorageRepository
import com.babelsoftware.airnote.domain.model.AiSuggestion
import com.babelsoftware.airnote.domain.model.ChatMessage
import com.babelsoftware.airnote.domain.model.Folder
import com.babelsoftware.airnote.domain.model.Note
import com.babelsoftware.airnote.domain.model.Participant
import com.babelsoftware.airnote.domain.repository.SettingsRepository
import com.babelsoftware.airnote.domain.usecase.FolderUseCase
import com.babelsoftware.airnote.domain.usecase.NoteUseCase
import com.babelsoftware.airnote.presentation.components.DecryptionResult
import com.babelsoftware.airnote.presentation.components.EncryptionHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ---> Data class to hold the note outline created by AI
data class DraftedNote(
    val topic: String,
    val title: String,
    val content: String
)
// <---

// ---> A data class that keeps all the state of the chat interface together
data class ChatState(
    val messages: List<ChatMessage> = emptyList(),
    val isAwaitingDraftTopic: Boolean = false,
    val latestDraft: DraftedNote? = null,
    val hasStartedConversation: Boolean = false
)
// <---

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val stringProvider: StringProvider,
    val encryptionHelper: EncryptionHelper,
    private val noteUseCase: NoteUseCase,
    private val folderUseCase: FolderUseCase,
    @ApplicationContext private val context: Context,
    private val geminiRepository: GeminiRepository,
    private val settingsRepository: SettingsRepository,
    private val secureStorageRepository: SecureStorageRepository
) : ViewModel() {
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

    private var _searchQuery = mutableStateOf("")
    val searchQuery: State<String> = _searchQuery

    private val _selectedFolderId = MutableStateFlow<Long?>(null)
    val selectedFolderId: StateFlow<Long?> = _selectedFolderId.asStateFlow()

    private val _folderForAction = mutableStateOf<Folder?>(null)
    val folderForAction: State<Folder?> = _folderForAction

    private val _showDeleteConfirmDialog = mutableStateOf(false)
    val showDeleteConfirmDialog: State<Boolean> = _showDeleteConfirmDialog

    private val _folderToEdit = mutableStateOf<Folder?>(null)
    val folderToEdit: State<Folder?> = _folderToEdit

    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private suspend fun getApiKeyToUse(): String {
        return secureStorageRepository.getUserApiKey() ?: ""
    }

    // --- Global AI Chat States ---
    private val _isAiChatSheetVisible = mutableStateOf(false)
    val isAiChatSheetVisible: State<Boolean> = _isAiChatSheetVisible

    private val _isFabExtended = mutableStateOf(true)
    val isFabExtended: State<Boolean> = _isFabExtended

    private val _chatState = mutableStateOf(ChatState())
    val chatState: State<ChatState> = _chatState

    val suggestions: List<AiSuggestion> = listOf(
        AiSuggestion(
            title = stringProvider.getString(R.string.ask_ai),
            icon = Icons.Rounded.Search,
            action = { /* TODO: Question asking logic will be added */ }
        ),
        AiSuggestion(
            title = stringProvider.getString(R.string.make_note),
            icon = Icons.Rounded.Edit,
            action = { onDraftAnythingClicked() }
        ),
        AiSuggestion(
            title = stringProvider.getString(R.string.generate_ideas),
            icon = Icons.Rounded.AutoAwesome,
            action = { sendMessage(stringProvider.getString(R.string.generate_ideas_prompt)) }
        )
        // TODO New suggestions can be added here...
    )

    // --- Global AI Chat States | END ---

    // --- Global AI Chat Functions ---
    fun toggleAiChatSheet(isVisible: Boolean) {
        _isAiChatSheetVisible.value = isVisible
    }

    fun setFabExtended(isExtended: Boolean) {
        _isFabExtended.value = isExtended
    }

    fun sendMessage(userMessage: String) {
        val currentMessages = _chatState.value.messages.toMutableList() // Create a temporary message list

        // ---> Add user's message and loading indicator for AI
        currentMessages.add(ChatMessage(text = userMessage, participant = Participant.USER))
        currentMessages.add(ChatMessage(text = "", participant = Participant.MODEL, isLoading = true))
        // <---

        _chatState.value = _chatState.value.copy(messages = currentMessages, hasStartedConversation = true) // update UI

        viewModelScope.launch {
            val history = _chatState.value.messages
            val responseBuilder = StringBuilder()
            val apiKey = getApiKeyToUse()

            geminiRepository.generateChatResponse(history, apiKey)
                .onCompletion {
                    // Sets the ‘isLoading’ status of the last message to “false” when the flow is finished
                    val finalMessages = _chatState.value.messages.toMutableList()
                    val lastMessage = finalMessages.lastOrNull()
                    if (lastMessage != null && lastMessage.participant == Participant.MODEL) {
                        finalMessages[finalMessages.size - 1] = lastMessage.copy(isLoading = false)
                        _chatState.value = _chatState.value.copy(messages = finalMessages)
                    }
                }
                .collect { chunk ->
                    responseBuilder.append(chunk)

                    val updatedMessages = _chatState.value.messages.toMutableList() // Creates the streaming effect by updating the last message (AI's response)
                    val lastMessageIndex = updatedMessages.size - 1
                    if (lastMessageIndex >= 0) {
                        updatedMessages[lastMessageIndex] = updatedMessages[lastMessageIndex].copy(
                            text = responseBuilder.toString(),
                            isLoading = true // Stays “true” as long as the flow continues
                        )
                        _chatState.value = _chatState.value.copy(messages = updatedMessages)
                    }
                }
        }
    }

    fun onDraftAnythingClicked() {
        val promptMessage = ChatMessage(
            text = stringProvider.getString(R.string.ai_prompt_for_draft_topic),
            participant = Participant.MODEL
        )
        _chatState.value = _chatState.value.copy(
            isAwaitingDraftTopic = true,
            messages = listOf(promptMessage),
            hasStartedConversation = true
        )
    }

    fun generateDraft(topic: String) {
        val currentMessages = _chatState.value.messages.toMutableList()
        currentMessages.add(ChatMessage(text = topic, participant = Participant.USER))
        currentMessages.add(ChatMessage(text = "", participant = Participant.MODEL, isLoading = true))

        _chatState.value = _chatState.value.copy(
            isAwaitingDraftTopic = false,
            messages = currentMessages
        )

        viewModelScope.launch {
            val apiKey = getApiKeyToUse()
            val result = geminiRepository.generateDraft(topic, apiKey)
            if (result != null && result.contains("BAŞLIK:") && result.contains("İÇERİK:")) {
                val title = result.substringAfter("BAŞLIK:").substringBefore("İÇERİK:").trim()
                val content = result.substringAfter("İÇERİK:").trim()
                _chatState.value = _chatState.value.copy(
                    messages = emptyList(),
                    latestDraft = DraftedNote(topic, title, content)
                )
            } else {
                val finalMessages = _chatState.value.messages.dropLast(1).toMutableList()
                finalMessages.add(
                    ChatMessage(
                        text = result ?: "Bilinmeyen bir hata oluştu.",
                        participant = Participant.ERROR
                    )
                )
                _chatState.value = _chatState.value.copy(
                    messages = finalMessages,
                    hasStartedConversation = false
                )
            }
        }
    }

    fun saveDraftedNote() {
        val draft = _chatState.value.latestDraft ?: return
        viewModelScope.launch {
            noteUseCase.addNote(
                Note(
                    name = draft.title,
                    description = draft.content,
                    // ... other Note properties
                )
            )
            resetChatState()
            toggleAiChatSheet(false)
        }
    }

    fun regenerateDraft() {
        val topic = _chatState.value.latestDraft?.topic ?: return
        _chatState.value = _chatState.value.copy(latestDraft = null) // Clear previous draft
        generateDraft(topic) // Re-create with the same issue
    }

    fun resetChatState() {
        _chatState.value = ChatState()
    }

    // --- Global AI Chat Functions | END ---

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

    val allFolders: StateFlow<List<Folder>> = folderUseCase.getAllFolders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val displayedNotes: StateFlow<List<Note>> =
        combine(
            noteUseCase.getAllNotes(),
            selectedFolderId,
            isVaultMode
        ) { allNotes, folderId, isVault ->
            val notesAfterFolderFilter = if (folderId == null) {
                allNotes
            } else {
                allNotes.filter { it.folderId == folderId }
            }
            notesAfterFolderFilter.filter { it.encrypted == isVault }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        noteUseCase.observe()
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
        _selectedFolderId.value = folderId
    }

    fun addFolder(name: String, color: String) {
        viewModelScope.launch {
            folderUseCase.addFolder(Folder(name = name, color = color))
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

    fun updateFolder(name: String, color: String) {
        _folderToEdit.value?.let { folder ->
            viewModelScope.launch {
                val updatedFolder = folder.copy(name = name, color = color)
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
}
