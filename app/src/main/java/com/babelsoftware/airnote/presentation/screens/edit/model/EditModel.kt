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
import com.babelsoftware.airnote.data.repository.AiTone
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

@HiltViewModel
class EditViewModel @Inject constructor(
    private val noteUseCase: NoteUseCase,
    private val encryption: EncryptionHelper,
    private val geminiRepository: GeminiRepository
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

    // --- AI STATE'LERİ ---
    private val _isAiActionSheetVisible = mutableStateOf(false)
    val isAiActionSheetVisible: State<Boolean> = _isAiActionSheetVisible

    private val _isToneActionSheetVisible = mutableStateOf(false)
    val isToneActionSheetVisible: State<Boolean> = _isToneActionSheetVisible

    private val _aiResultText = mutableStateOf<String?>(null)
    val aiResultText: State<String?> = _aiResultText

    private val _isAiLoading = mutableStateOf(false)
    val isAiLoading: State<Boolean> = _isAiLoading
    // --- BİTTİ ---

    // --- UI Olayları için Channel ---
    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()
    // --- BİTTİ ---

    // --- AI FONKSİYONLARI ---
    fun toggleAiActionSheet(isVisible: Boolean) {
        _isAiActionSheetVisible.value = isVisible
    }

    fun toggleToneActionSheet(isVisible: Boolean) {
        _isToneActionSheetVisible.value = isVisible
    }

    fun clearAiResult() {
        _aiResultText.value = null
    }

    /**
     * Tüm AI aksiyonlarını çalıştıran merkezi fonksiyon.
     */
    fun executeAiAction(action: AiAction, tone: AiTone? = null) {
        // Eğer aksiyon "Ton Değiştir" ise ve henüz bir ton seçilmemişse,
        // sadece ton menüsünü aç ve fonksiyondan çık.
        if (action == AiAction.CHANGE_TONE && tone == null) {
            toggleAiActionSheet(false)
            toggleToneActionSheet(true)
            return
        }

        // Üzerinde işlem yapılacak seçili metni al
        val selectedText = noteDescription.value.text.substring(
            noteDescription.value.selection.min,
            noteDescription.value.selection.max
        )

        // Seçili metin varsa API isteğini başlat
        if (selectedText.isNotBlank()) {
            viewModelScope.launch {
                _isAiLoading.value = true
                try {
                    val result = geminiRepository.processAiAction(selectedText, action, tone)
                    if (result != null) {
                        _aiResultText.value = result
                    } else {
                        _uiEvent.send("Yapay zeka bir yanıt üretemedi.")
                    }
                } catch (e: Exception) {
                    _uiEvent.send("İşlem başarısız oldu. İnternet bağlantınızı kontrol edin.")
                } finally {
                    _isAiLoading.value = false
                    // İşlem bitince açık olabilecek tüm menüleri kapat
                    toggleAiActionSheet(false)
                    toggleToneActionSheet(false)
                }
            }
        }
    }

    /**
     * AI tarafından üretilen metni, seçili metinle değiştirir.
     */
    fun replaceWithAiResult() {
        val originalText = noteDescription.value.text
        val selection = noteDescription.value.selection
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
    // --- BİTTİ ---

    /**
     * Seçilen AI aksiyonunu çalıştırır ve sonucu state'e yazar.
     */
    fun runAiAction(action: AiAction) {
        // Sadece açıklama alanında seçilen metin üzerinde çalışacak
        val selectedText = noteDescription.value.text.substring(
            noteDescription.value.selection.min,
            noteDescription.value.selection.max
        )
        if (selectedText.isNotBlank()) {
            viewModelScope.launch {
                _isAiLoading.value = true
                try { // try-catch bloğunu güncelliyoruz
                    val result = geminiRepository.processAiAction(selectedText, action)
                    if (result != null) {
                        _aiResultText.value = result
                    } else {
                        // Eğer sonuç null ise hata mesajı gönder
                        _uiEvent.send("Yapay zeka bir yanıt üretemedi.")
                    }
                } catch (e: Exception) {
                    // Genel bir hata durumunda mesaj gönder
                    _uiEvent.send("İşlem başarısız oldu. İnternet bağlantınızı kontrol edin.")
                } finally {
                    _isAiLoading.value = false
                }
            }
        }
    }

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

    // DÜZELTME: folderIdFromNav parametresine varsayılan null değeri ekleyin
    fun setupNoteData(id : Int, folderIdFromNav: Long? = null) {
        if (id != 0) { // Mevcut bir not ise
            viewModelScope.launch {
                noteUseCase.getNoteById(id).collectLatest { note ->
                    if (note != null) {
                        syncNote(note)
                    }
                }
            }
        } else { // Yeni bir not ise
            // Sadece navigasyon ile gelen folderId'yi state'e ata
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