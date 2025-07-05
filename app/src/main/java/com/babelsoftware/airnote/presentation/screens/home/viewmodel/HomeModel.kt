package com.babelsoftware.airnote.presentation.screens.home.viewmodel

import android.content.Context
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babelsoftware.airnote.data.repository.GeminiRepository
import com.babelsoftware.airnote.domain.model.ChatMessage
import com.babelsoftware.airnote.domain.model.Folder
import com.babelsoftware.airnote.domain.model.Note
import com.babelsoftware.airnote.domain.model.Participant
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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


// Sohbet arayüzünün tüm durumunu bir arada tutan bir data class
data class ChatState(
    val messages: List<ChatMessage> = emptyList()
    // "Draft Anything" gibi özel durumlar için state'leri daha sonra buraya ekleyeceğiz
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    val encryptionHelper: EncryptionHelper,
    private val noteUseCase: NoteUseCase,
    private val folderUseCase: FolderUseCase,
    @ApplicationContext private val context: Context,
    private val geminiRepository: GeminiRepository
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

    // --- YENİ: Global AI Sohbeti State'leri ---

    // Sohbet penceresinin (BottomSheet) açık olup olmadığını kontrol eder.
    private val _isAiChatSheetVisible = mutableStateOf(false)
    val isAiChatSheetVisible: State<Boolean> = _isAiChatSheetVisible

    // Ana ekrandaki FAB (Floating Action Button) butonunun genişletilmiş (extended) olup olmadığını kontrol eder.
// Varsayılan olarak genişletilmiş başlar.
    private val _isFabExtended = mutableStateOf(true)
    val isFabExtended: State<Boolean> = _isFabExtended

    private val _chatState = mutableStateOf(ChatState())
    val chatState: State<ChatState> = _chatState

    // --- BİTTİ ---

    // --- YENİ: Global AI Sohbeti Fonksiyonları ---

    // Sohbet penceresini açıp kapatan fonksiyon.
    fun toggleAiChatSheet(isVisible: Boolean) {
        _isAiChatSheetVisible.value = isVisible
    }

    // FAB'ın durumunu değiştiren fonksiyon. (Kaydırma durumuna göre çağrılacak)
    fun setFabExtended(isExtended: Boolean) {
        _isFabExtended.value = isExtended
    }

    // YENİ: Mesaj gönderme fonksiyonu
    fun sendMessage(userMessage: String) {
        // Kullanıcının mesajını hemen listeye ekle
        val currentMessages = _chatState.value.messages.toMutableList()
        currentMessages.add(ChatMessage(text = userMessage, participant = Participant.USER))
        // AI cevabı için bir yükleme göstergesi ekle
        currentMessages.add(ChatMessage(text = "", participant = Participant.MODEL, isLoading = true))

        _chatState.value = _chatState.value.copy(messages = currentMessages)

        viewModelScope.launch {
            // TODO: GeminiRepository'deki yeni sohbet fonksiyonunu burada çağıracağız.
            // Şimdilik sahte bir cevapla test edelim.
            kotlinx.coroutines.delay(2000) // 2 saniye bekle
            val aiResponse = "Bu, '$userMessage' mesajınıza verilmiş bir test cevabıdır."

            // Yükleme göstergesini kaldırıp yerine AI'ın cevabını koy
            val finalMessages = _chatState.value.messages.dropLast(1).toMutableList() // Yükleniyor... mesajını sil
            finalMessages.add(ChatMessage(text = aiResponse, participant = Participant.MODEL)) // Gerçek cevabı ekle
            _chatState.value = _chatState.value.copy(messages = finalMessages)
        }
    }

    // --- BİTTİ ---

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
