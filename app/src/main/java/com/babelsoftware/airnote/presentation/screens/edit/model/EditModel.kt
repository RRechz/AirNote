package com.babelsoftware.airnote.presentation.screens.edit.model

import androidx.compose.runtime.State
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babelsoftware.airnote.domain.model.Note
import com.babelsoftware.airnote.domain.usecase.NoteUseCase
import com.babelsoftware.airnote.presentation.components.DecryptionResult
import com.babelsoftware.airnote.presentation.components.EncryptionHelper
import com.babelsoftware.airnote.presentation.screens.edit.components.UndoRedoState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.babelsoftware.airnote.data.repository.GeminiRepository
import com.babelsoftware.airnote.data.repository.AiAction
import com.babelsoftware.airnote.data.repository.AiAssistantAction
import com.babelsoftware.airnote.data.repository.AiTone
import com.babelsoftware.airnote.data.repository.SecureStorageRepository
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow

@HiltViewModel
class EditViewModel @Inject constructor(
    private val noteUseCase: NoteUseCase,
    private val encryption: EncryptionHelper,
    private val geminiRepository: GeminiRepository,
    private val secureStorageRepository: SecureStorageRepository
) : ViewModel() {
    private val _noteName = mutableStateOf(TextFieldValue())
    val noteName: State<TextFieldValue> get() = _noteName

    private val _isDescriptionInFocus = mutableStateOf(false)
    val isDescriptionInFocus: State<Boolean> get() = _isDescriptionInFocus

    private val _isEncrypted = mutableStateOf(false)
    val isEncrypted: State<Boolean> get() = _isEncrypted

    private val _noteDescription = mutableStateOf(TextFieldValue())
    val noteDescription: State<TextFieldValue> get() = _noteDescription

    private val _noteId = mutableIntStateOf(0)
    val noteId: State<Int> get() = _noteId

    private val _noteCreatedTime = mutableLongStateOf(System.currentTimeMillis())
    val noteCreatedTime: State<Long> get() = _noteCreatedTime

    private val _isNoteInfoVisible = mutableStateOf(false)
    val isNoteInfoVisible: State<Boolean> get() = _isNoteInfoVisible

    private val _isEditMenuVisible = mutableStateOf(false)
    val isEditMenuVisible: State<Boolean> get() = _isEditMenuVisible

    private val _isPinned = mutableStateOf(false)
    val isPinned: State<Boolean> get() = _isPinned

    private val undoRedoState = UndoRedoState()

    private val _folderId = mutableStateOf<Long?>(null)
    val folderId: State<Long?> get() = _folderId

    // --- AI'S STATES ---
    private suspend fun getApiKeyToUse(): String {
        return secureStorageRepository.getUserApiKey() ?: ""
    }
    private val _isAiActionSheetVisible = mutableStateOf(false)
    val isAiActionSheetVisible: State<Boolean> = _isAiActionSheetVisible

    private val _isSheetReadyForInteraction = mutableStateOf(false)
    val isSheetReadyForInteraction: State<Boolean> = _isSheetReadyForInteraction

    private val _isToneActionSheetVisible = mutableStateOf(false)
    val isToneActionSheetVisible: State<Boolean> = _isToneActionSheetVisible

    private val _aiResultText = mutableStateOf<String?>(null)
    val aiResultText: State<String?> = _aiResultText

    private val _isAiLoading = mutableStateOf(false)
    val isAiLoading: State<Boolean> = _isAiLoading

    private val _isAiAssistantStreaming = mutableStateOf(false)
    val isAiAssistantStreaming: State<Boolean> = _isAiAssistantStreaming
    private var _lastSelection: TextRange? = null // Tentative texts to be sent to AI
    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _isAiAssistantSheetVisible = mutableStateOf(false)
    val isAiAssistantSheetVisible: State<Boolean> = _isAiAssistantSheetVisible
    private val _titleSuggestions = mutableStateOf<List<String>>(emptyList())
    val titleSuggestions: State<List<String>> = _titleSuggestions
    // --- AI'S STATES | END---

    // --- AI FUNCTIONS ---
    fun toggleAiActionSheet(isVisible: Boolean) {
        if (isVisible) {
            _isSheetReadyForInteraction.value = false // Set the interaction status to “not ready” (false) each time the menu is opened.
        }
        _isAiActionSheetVisible.value = isVisible
    }

    fun markSheetAsReadyForInteraction() {
        _isSheetReadyForInteraction.value = true
    }

    fun toggleToneActionSheet(isVisible: Boolean) {
        _isToneActionSheetVisible.value = isVisible
    }

    fun clearAiResult() {
        _aiResultText.value = null
        _lastSelection = null // Reset stored selection
    }

    /**
     * Central function that runs all AI actions.
     * Operates only on the selected text.
     */
    fun executeAiAction(action: AiAction, tone: AiTone? = null) {
        val selection = _lastSelection ?: noteDescription.value.selection
        toggleAiActionSheet(false)
        toggleToneActionSheet(false)

        if (action == AiAction.CHANGE_TONE && tone == null) {
            toggleToneActionSheet(true)
            return
        }

        if (selection.collapsed) {
            viewModelScope.launch {
                _uiEvent.send("Lütfen üzerinde işlem yapmak istediğiniz metni seçin.")
            }
            return
        }

        val selectedText = noteDescription.value.text.substring(selection.min, selection.max)

        if (selectedText.isNotBlank()) {
            viewModelScope.launch {
                _isAiLoading.value = true
                try {
                    val apiKey = getApiKeyToUse()
                    geminiRepository.processAiAction(selectedText, action, tone, apiKey)
                        .onSuccess { responseText ->
                            _lastSelection = selection
                            _aiResultText.value = responseText
                        }
                        .onFailure { exception ->
                            _uiEvent.send(exception.message ?: "Unknow error")
                        }
                } catch (e: Exception) {
                    _uiEvent.send("Beklenmedik bir hata oluştu: ${e.message}")
                } finally {
                    _isAiLoading.value = false
                }
            }
        }
    }

    fun executeAiAssistantAction(action: AiAssistantAction) {
        toggleAiAssistantSheet(false)

        viewModelScope.launch {
            var fullResponse = StringBuilder()
            val apiKey = getApiKeyToUse()

            geminiRepository.processAssistantAction(
                noteName = noteName.value.text,
                noteDescription = noteDescription.value.text,
                action = action,
                apiKey = apiKey
            )
                .onStart {
                    if (action != AiAssistantAction.SUGGEST_A_TITLE) _isAiAssistantStreaming.value = true
                }
                .onCompletion {
                    _isAiAssistantStreaming.value = false
                }
                .collect { chunk ->
                    if (chunk.startsWith("API isteği başarısız oldu") || chunk.startsWith("Kullanıcı API anahtarı bulunamadı")) {
                        _uiEvent.send(chunk)
                        this.coroutineContext.cancel()
                        return@collect
                    }
                    fullResponse.append(chunk)
                    if (action == AiAssistantAction.PROS_AND_CONS || action == AiAssistantAction.CREATE_TODO_LIST || action == AiAssistantAction.SIMPLIFY) {
                        val currentText = _noteDescription.value.text
                        val newText = currentText + chunk
                        _noteDescription.value = TextFieldValue(text = newText, selection = TextRange(newText.length))
                    }
                }
            val finalResponse = fullResponse.toString()
            when (action) {
                AiAssistantAction.CREATE_TODO_LIST -> {
                    if (finalResponse.trim() == "NO_TASKS") {
                        _uiEvent.send("Önce gün içerisinde yapmanız gerekenleri birkaç cümle ile anlatın.")
                    }
                }
                AiAssistantAction.SUGGEST_A_TITLE -> {
                    val suggestions = finalResponse.lines().mapNotNull {
                        it.replaceFirst(Regex("^\\d+\\.?\\s*"), "").trim().takeIf { s -> s.isNotEmpty() }
                    }
                    _titleSuggestions.value = suggestions
                }
                else -> {
                }
            }
        }
    }

    /**
     * Replaces AI generated text with the selected text.
     */
    fun replaceWithAiResult() {
        val originalText = noteDescription.value.text
        val selection = _lastSelection ?: return // Use stored selection
        val newText = _aiResultText.value ?: return

        val updatedText = originalText.replaceRange(selection.start, selection.end, newText)
        val newSelectionStart = selection.start + newText.length
        updateNoteDescription(
            TextFieldValue(
                text = updatedText,
                selection = TextRange(newSelectionStart)
            )
        )
        clearAiResult()
    }

    fun toggleAiAssistantSheet(isVisible: Boolean) {
        _isAiAssistantSheetVisible.value = isVisible
    }

    fun clearTitleSuggestions() {
        _titleSuggestions.value = emptyList()
    }
    // --- AI FUNCTIONS | END ---

    fun updateFolderId(newFolderId: Long?) {
        _folderId.value = newFolderId
    }

    fun saveNote(id: Int) {
        if (noteName.value.text.isNotEmpty() || noteDescription.value.text.isNotBlank()) {
            viewModelScope.launch {
                noteUseCase.addNote(
                    Note(
                        id = id,
                        name = noteName.value.text,
                        description = noteDescription.value.text,
                        pinned = isPinned.value,
                        folderId = folderId.value,
                        encrypted = isEncrypted.value,
                        createdAt = if (noteCreatedTime.value != 0L) noteCreatedTime.value else System.currentTimeMillis(),
                    )
                )
                fetchLastNoteAndUpdate()
            }
        }
    }

    fun deleteNote(id: Int) {
        noteUseCase.deleteNoteById(id = id)
    }

    private fun syncNote(note: Note) {
        if (note.encrypted) {
            val (name, nameStatus) = encryption.decrypt(note.name)
            val (description, descriptionStatus) = encryption.decrypt(note.description)
            if (nameStatus == DecryptionResult.SUCCESS && descriptionStatus == DecryptionResult.SUCCESS) {
                updateNoteName(TextFieldValue(name!!, selection = TextRange(note.name.length)))
                updateNoteDescription(TextFieldValue(description!!, selection = TextRange(note.description.length)))
            }
        } else {
            updateNoteName(TextFieldValue(note.name, selection = TextRange(note.name.length)))
            updateNoteDescription(TextFieldValue(note.description, selection = TextRange(note.description.length)))
        }
        updateNoteCreatedTime(note.createdAt)
        updateNoteId(note.id)
        updateNotePin(note.pinned)
        updateIsEncrypted(note.encrypted)
        updateFolderId(note.folderId)
    }

    fun setupNoteData(id : Int, folderIdFromNav: Long? = null) {
        if (id != 0) {
            viewModelScope.launch {
                noteUseCase.getNoteById(id).collectLatest { note ->
                    if (note != null) {
                        syncNote(note)
                    }
                }
            }
        } else {
            updateFolderId(folderIdFromNav)
        }
    }

    private fun fetchLastNoteAndUpdate() {
        if (noteName.value.text.isNotEmpty() || noteDescription.value.text.isNotBlank()) {
            if (noteId.value == 0) {
                viewModelScope.launch {
                    noteUseCase.getLastNoteId { lastId ->
                        viewModelScope.launch {
                            setupNoteData(lastId?.toInt() ?: 1)
                        }
                    }
                }
            }
        }
    }

    fun toggleEditMenuVisibility(value: Boolean) {
        _isEditMenuVisible.value = value
    }

    fun toggleNoteInfoVisibility(value: Boolean) {
        _isNoteInfoVisible.value = value
    }

    fun toggleIsDescriptionInFocus(value: Boolean) {
        _isDescriptionInFocus.value = value
    }

    fun toggleNotePin(value: Boolean) {
        _isPinned.value = value
    }

    fun updateNoteName(newName: TextFieldValue) {
        _noteName.value = newName
        undoRedoState.onInput(newName)
    }

    fun updateIsEncrypted(value: Boolean) {
        _isEncrypted.value = value
    }

    fun updateNoteDescription(newDescription: TextFieldValue) {
        _noteDescription.value = newDescription
        if (!newDescription.selection.collapsed) {
            _lastSelection = newDescription.selection
        }
        undoRedoState.onInput(newDescription)
    }

    private fun updateNoteCreatedTime(newTime: Long) {
        _noteCreatedTime.longValue = newTime
    }

    private fun updateNotePin(pinned: Boolean) {
        _isPinned.value = pinned
    }

    fun updateNoteId(newId: Int) {
        _noteId.intValue = newId
    }

    fun undo() {
        undoRedoState.undo()
        _noteDescription.value = undoRedoState.input
    }

    fun redo() {
        undoRedoState.redo()
        _noteDescription.value = undoRedoState.input
    }

    private fun isSelectorAtStartOfNonEmptyLine(): Boolean {
        val text = _noteDescription.value.text
        val selectionStart = _noteDescription.value.selection.start

        if (selectionStart == 0) {
            return true
        }
        return text[selectionStart - 1] == '\n'
    }


    private fun getIntRangeForCurrentLine(): IntRange {
        val text = _noteDescription.value.text
        val selectionStart = _noteDescription.value.selection.start
        val selectionEnd = _noteDescription.value.selection.end
        var lineStart = selectionStart
        var lineEnd = selectionEnd

        while (lineStart > 0 && text[lineStart - 1] != '\n') {
            lineStart--
        }

        while (lineEnd < text.length && text[lineEnd] != '\n') {
            lineEnd++
        }
        return IntRange(lineStart, lineEnd - 1);
    }

    fun insertText(insertText: String, offset: Int = 1, newLine: Boolean = true) {
        val currentText = _noteDescription.value.text
        val resultSelectionIndex: Int
        val rangeOfCurrentLine = getIntRangeForCurrentLine()
        val updatedText = if (!rangeOfCurrentLine.isEmpty()) {
            val currentLineContents = currentText.substring(rangeOfCurrentLine)
            val newLine = if (isSelectorAtStartOfNonEmptyLine()) {
                insertText + currentLineContents
            } else {
                if (newLine) {
                    currentLineContents + "\n" + insertText
                } else {
                    currentLineContents + insertText
                }
            }
            resultSelectionIndex = rangeOfCurrentLine.first + newLine.length - 1
            currentText.replaceRange(rangeOfCurrentLine, newLine)
        } else {
            resultSelectionIndex = (currentText + insertText).length
            currentText + insertText
        }

        _noteDescription.value = TextFieldValue(
            text = updatedText,
            selection = TextRange(resultSelectionIndex + offset)
        )
    }
}