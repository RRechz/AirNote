package com.babelsoftware.airnote.presentation.screens.edit.model

import android.content.Intent
import android.util.Patterns
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
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.data.provider.StringProvider
import com.babelsoftware.airnote.domain.model.ChatMessage
import com.babelsoftware.airnote.domain.model.Participant
import com.babelsoftware.airnote.domain.usecase.FolderUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

@HiltViewModel
class EditViewModel @Inject constructor(
    private val noteUseCase: NoteUseCase,
    private val folderUseCase: FolderUseCase,
    private val encryption: EncryptionHelper,
    private val geminiRepository: GeminiRepository,
    private val secureStorageRepository: SecureStorageRepository,
    private val stringProvider: StringProvider
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

    private var isShareIntentHandled = false

    private val _isLoading = mutableStateOf(true) // Başlangıçta true
    val isLoading: State<Boolean> get() = _isLoading

    private val _isDreamJournalMode = mutableStateOf(false)
    val isDreamJournalMode: State<Boolean> get() = _isDreamJournalMode

    // --- AI'S STATES ---
    private suspend fun getApiKeyToUse(): String {
        return secureStorageRepository.getUserApiKey() ?: ""
    }
    private val _isAiActionSheetVisible = mutableStateOf(false)
    val isAiActionSheetVisible: State<Boolean> get() = _isAiActionSheetVisible

    private val _isSheetReadyForInteraction = mutableStateOf(false)
    val isSheetReadyForInteraction: State<Boolean> get() = _isSheetReadyForInteraction

    private val _isToneActionSheetVisible = mutableStateOf(false)
    val isToneActionSheetVisible: State<Boolean> get() = _isToneActionSheetVisible

    private val _aiResultText = mutableStateOf<String?>(null)
    val aiResultText: State<String?> = _aiResultText

    private val _isAiLoading = mutableStateOf(false)
    val isAiLoading: State<Boolean> get() = _isAiLoading

    // --- NEW MINIMAL AI CHAT STATES ---
    private val _isMinimalAiUiVisible = mutableStateOf(false)
    val isMinimalAiUiVisible: State<Boolean> get() = _isMinimalAiUiVisible

    private val _minimalAiChatText = mutableStateOf("")
    val minimalAiChatText: State<String> get() = _minimalAiChatText

    private val _minimalAiChatHistory = mutableStateOf<List<ChatMessage>>(emptyList())
    val minimalAiChatHistory: State<List<ChatMessage>> = _minimalAiChatHistory

    private val _isMinimalChatActive = mutableStateOf(false)
    val isMinimalChatActive: State<Boolean> get() = _isMinimalChatActive

    private val _isMinimalAiLoading = mutableStateOf(false)
    val isMinimalAiLoading: State<Boolean> get() = _isMinimalAiLoading
    // --- END NEW STATES ---

    private val _isAiAssistantStreaming = mutableStateOf(false)
    val isAiAssistantStreaming: State<Boolean> get() = _isAiAssistantStreaming
    private var _lastSelection: TextRange? = null // Tentative texts to be sent to AI
    private val _uiEvent = Channel<String>()
    val uiEvent = _uiEvent.receiveAsFlow()

    private val _isAiAssistantSheetVisible = mutableStateOf(false)
    val isAiAssistantSheetVisible: State<Boolean> get() = _isAiAssistantSheetVisible
    private val _titleSuggestions = mutableStateOf<List<String>>(emptyList())
    val titleSuggestions: State<List<String>> = _titleSuggestions

    private val _isTranslateSheetVisible = mutableStateOf(false)
    val isTranslateSheetVisible: State<Boolean> get() = _isTranslateSheetVisible

    private val _downloadedLanguages = mutableStateOf<List<Pair<String, String>>>(emptyList())
    val downloadedLanguages: State<List<Pair<String, String>>> = _downloadedLanguages

    private var _translationSelection: TextRange? = null
    // --- AI'S STATES | END---


    // --- YENİ EKLENEN FONKSİYONLAR (PAYLAŞ VE LİNK) ---

    /**
     * "Link" adında bir klasör arar. Bulamazsa oluşturur ve ID'sini döndürür.
     * (Daha kararlı hale getirildi)
     */
    private suspend fun getOrCreateLinkFolderId(): Long? {
        val linkFolderName = "Link"
        val foldersFlow = folderUseCase.getAllFolders()
        var folders = foldersFlow.first() // Mevcut listeyi al
        var linkFolder = folders.find { it.name.equals(linkFolderName, ignoreCase = true) }

        if (linkFolder != null) {
            return linkFolder.id // Klasör zaten var, ID'sini döndür
        }

        // Klasör yok, oluşturalım.
        // iconName için HomeModel.kt'de kullanılan "Link" ikonunu varsayıyorum.
        val newFolder = com.babelsoftware.airnote.domain.model.Folder(name = linkFolderName, iconName = "Link")

        try {
            folderUseCase.addFolder(newFolder) // suspend fonksiyon, eklemenin bitmesini bekle

            // Ekleme bittiğine göre, veritabanı güncellenmiş olmalı.
            // Flow'dan en güncel listeyi tekrar çek.
            folders = foldersFlow.first()
            linkFolder = folders.find { it.name.equals(linkFolderName, ignoreCase = true) }

            return linkFolder?.id // Yeni oluşturulan klasörün ID'sini döndür

        } catch (e: Exception) {
            // Ekleme veya bulma sırasında hata oluştu
            e.printStackTrace()
            // Hata durumunda bile son bir kez daha dene
            folders = foldersFlow.first()
            linkFolder = folders.find { it.name.equals(linkFolderName, ignoreCase = true) }
            return linkFolder?.id
        }
    }

    /**
     * Gelen paylaşım verisini (link veya metin) işler.
     */
    fun handleSharedIntent(intent: Intent) {
        // Intent'in zaten işlenip işlenmediğini kontrol et
        if (isShareIntentHandled) return

        if (intent.action == Intent.ACTION_SEND && "text/plain" == intent.type) {
            // Intent'i işlendi olarak işaretle
            isShareIntentHandled = true

            viewModelScope.launch {
                val sharedText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: return@launch
                val sharedTitle = intent.getStringExtra(Intent.EXTRA_SUBJECT)

                // 1. Paylaşılan metin bir URL mi kontrol et
                if (Patterns.WEB_URL.matcher(sharedText).matches()) {
                    // EVET, BU BİR LİNK

                    // 2. "Link" klasörünün ID'sini al veya oluştur
                    val targetFolderId = getOrCreateLinkFolderId() // <-- Artık Long? döndürür

                    if(targetFolderId != null) {
                        updateFolderId(targetFolderId) // Notun folderId'sini ayarla
                    } else {
                        // Opsiyonel: Klasör oluşturma başarısız olursa kullanıcıyı bilgilendir
                        _uiEvent.send("The link folder could not be created.")
                    }

                    // 3. Site başlığını çek
                    _isAiLoading.value = true // Başlık çekilirken yükleniyor göster
                    val title = fetchTitleFromUrl(sharedText) ?: sharedTitle ?: "Saved links"
                    _isAiLoading.value = false

                    // 4. ViewModel'i doldur
                    updateNoteName(TextFieldValue(title))
                    updateNoteDescription(TextFieldValue(sharedText))

                } else {
                    // HAYIR, BU DÜZ METİN
                    // Normal davran, klasör ataması yapma (varsayılan klasörde kalsın)
                    val title = sharedTitle ?: sharedText.take(30)
                    updateNoteName(TextFieldValue(title))
                    updateNoteDescription(TextFieldValue(sharedText))
                }
            }
        }
    }

    /**
     * Asenkron olarak URL'den başlık çeker.
     * Bu bir ağ işlemidir, bu yüzden 'suspend' olmalıdır.
     */
    private suspend fun fetchTitleFromUrl(url: String): String? {
        return withContext(Dispatchers.IO) {
            try {
                val doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36")
                    .timeout(5000) // 5 saniye zaman aşımı
                    .get()
                // Başlığın boş olmadığını kontrol et
                doc.title().takeIf { it.isNotBlank() }
            } catch (e: Exception) {
                e.printStackTrace()
                null // Hata durumunda null döndür
            }
        }
    }

    // --- BİTİŞ: YENİ EKLENEN FONKSİYONLAR ---


    // --- AI FUNCTIONS ---

    // --- NEW MINIMAL AI CHAT FUNCTIONS ---
    fun toggleMinimalAiUi(isVisible: Boolean) {
        _isMinimalAiUiVisible.value = isVisible
        if (!isVisible) {
            // Reset chat state when closing
            _minimalAiChatText.value = ""
            _minimalAiChatHistory.value = emptyList()
            _isMinimalChatActive.value = false
            _isMinimalAiLoading.value = false
        }
    }

    fun updateMinimalAiChatText(newText: String) {
        _minimalAiChatText.value = newText
    }

    fun sendMinimalAiMessage() {
        if (isMinimalAiLoading.value || minimalAiChatText.value.isBlank()) return

        val userMessageText = _minimalAiChatText.value
        _minimalAiChatText.value = ""

        val userMessage = ChatMessage(userMessageText, Participant.USER)
        val loadingMessage = ChatMessage("", Participant.MODEL, isLoading = true)

        val currentHistory = _minimalAiChatHistory.value
        _minimalAiChatHistory.value = currentHistory + userMessage + loadingMessage
        _isMinimalChatActive.value = true
        _isMinimalAiLoading.value = true

        val historyToSend = mutableListOf<ChatMessage>()

        // If this is the first message, add the note context
        val userMessagesCount = _minimalAiChatHistory.value.count { it.participant == Participant.USER }
        if (userMessagesCount == 1) {
            val noteContext = noteDescription.value.text
            if (noteContext.isNotBlank()) {
                // Dream Journal modu için özel başlık
                val promptHeader: String = if (_isDreamJournalMode.value) {
                    stringProvider.getString(R.string.prompt_dream_journal_header)
                } else {
                    "My note content is:\n\n"
                }
                historyToSend.add(ChatMessage("$promptHeader\"\"\"$noteContext\"\"\"\n\nMy question is: $userMessageText", Participant.USER))
            } else {
                historyToSend.add(userMessage) // Kontekst yok, sadece mesajı gönder
            }
        } else {
            // For follow-up messages, build history from our state
            _minimalAiChatHistory.value.dropLast(1).forEachIndexed { index, msg ->
                if (msg.participant == Participant.USER && index == 0) {
                    val noteContext = noteDescription.value.text
                    if (noteContext.isNotBlank()) {
                        // Dream Journal modu için özel başlık
                        val promptHeader: String = if (_isDreamJournalMode.value) {
                            stringProvider.getString(R.string.prompt_dream_journal_header)
                        } else {
                            "My note content is:\n\n"
                        }
                        historyToSend.add(ChatMessage("$promptHeader\"\"\"$noteContext\"\"\"\n\nMy question is: ${msg.text}", Participant.USER))
                    } else {
                        historyToSend.add(msg)
                    }
                } else {
                    historyToSend.add(msg)
                }
            }
        }

        viewModelScope.launch {
            try {
                val apiKey = getApiKeyToUse()
                val responseBuilder = StringBuilder()

                geminiRepository.generateChatResponse(historyToSend, apiKey)
                    .onCompletion {
                        _isMinimalAiLoading.value = false
                        val finalResponse = ChatMessage(responseBuilder.toString(), Participant.MODEL, isLoading = false)
                        _minimalAiChatHistory.value = _minimalAiChatHistory.value.dropLast(1) + finalResponse
                    }
                    .collect { chunk ->
                        responseBuilder.append(chunk)
                        // Stream response to UI
                        _minimalAiChatHistory.value = _minimalAiChatHistory.value.dropLast(1) + ChatMessage(responseBuilder.toString(), Participant.MODEL, isLoading = true)
                    }

            } catch (e: Exception) {
                _isMinimalAiLoading.value = false
                val errorMessage = ChatMessage(e.message ?: stringProvider.getString(R.string.error_unknown), Participant.ERROR, isLoading = false)
                _minimalAiChatHistory.value = _minimalAiChatHistory.value.dropLast(1) + errorMessage
            }
        }
    }
    // --- END NEW FUNCTIONS ---

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
        if (action != AiAction.CHANGE_TONE && action != AiAction.TRANSLATE) {
            toggleAiActionSheet(false)
        } else {
            toggleMinimalAiUi(false)
        }
        toggleToneActionSheet(false)

        if (action == AiAction.CHANGE_TONE && tone == null) {
            toggleToneActionSheet(true)
            return
        }

        if (action == AiAction.TRANSLATE) {
            onTranslateClicked(forSelection = true)
            return
        }

        if (selection.collapsed) {
            viewModelScope.launch {
                _uiEvent.send(stringProvider.getString(R.string.ai_error_no_text_selected))
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
                            _uiEvent.send(exception.message ?: stringProvider.getString(R.string.error_unknown))
                        }
                } catch (e: Exception) {
                    _uiEvent.send(stringProvider.getString(R.string.error_unexpected_with_message, e.message ?: ""))
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
                        _uiEvent.send(stringProvider.getString(R.string.ai_error_no_tasks_for_todo))
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

    fun executeDreamAnalysis() {
        toggleMinimalAiUi(true)
        _isMinimalChatActive.value = true

        val dreamText = noteDescription.value.text
        if (dreamText.isBlank()) {
            viewModelScope.launch {
                _uiEvent.send(stringProvider.getString(R.string.dream_not_found))
            }
            return
        }

        val userMessageText = stringProvider.getString(R.string.interpret_my_dream)
        val userMessage = ChatMessage(userMessageText, Participant.USER)
        val loadingMessage = ChatMessage("", Participant.MODEL, isLoading = true)

        _minimalAiChatHistory.value = listOf(userMessage, loadingMessage)
        _isMinimalAiLoading.value = true

        val historyToSend = mutableListOf<ChatMessage>()
        val dreamPrompt = stringProvider.getString(R.string.prompt_dream_journal_header)
        historyToSend.add(ChatMessage("$dreamPrompt\"\"\"$dreamText\"\"\"\n\n$userMessageText", Participant.USER))

        viewModelScope.launch {
            try {
                val apiKey = getApiKeyToUse()
                val responseBuilder = StringBuilder()

                geminiRepository.generateChatResponse(historyToSend, apiKey)
                    .onCompletion {
                        _isMinimalAiLoading.value = false
                        val finalResponse = ChatMessage(responseBuilder.toString(), Participant.MODEL, isLoading = false)
                        _minimalAiChatHistory.value = _minimalAiChatHistory.value.dropLast(1) + finalResponse
                    }
                    .collect { chunk ->
                        responseBuilder.append(chunk)
                        // Akıcı yanıtı arayüze yansıt
                        _minimalAiChatHistory.value = _minimalAiChatHistory.value.dropLast(1) + ChatMessage(responseBuilder.toString(), Participant.MODEL, isLoading = true)
                    }

            } catch (e: Exception) {
                _isMinimalAiLoading.value = false
                val errorMessage = ChatMessage(e.message ?: stringProvider.getString(R.string.error_unknown), Participant.ERROR, isLoading = false)
                _minimalAiChatHistory.value = _minimalAiChatHistory.value.dropLast(1) + errorMessage
            }
        }
    }

    fun executeDreamSymbolAnalysis() {
        toggleMinimalAiUi(true)
        _isMinimalChatActive.value = true

        val dreamText = noteDescription.value.text
        if (dreamText.isBlank()) {
            viewModelScope.launch {
                _uiEvent.send(stringProvider.getString(R.string.dream_not_found))
            }
            return
        }

        val userMessageText = stringProvider.getString(R.string.explain_symbols)
        val userMessage = ChatMessage(userMessageText, Participant.USER)
        val loadingMessage = ChatMessage("", Participant.MODEL, isLoading = true)

        _minimalAiChatHistory.value = listOf(userMessage, loadingMessage)
        _isMinimalAiLoading.value = true

        val historyToSend = mutableListOf<ChatMessage>()
        val dreamPrompt = stringProvider.getString(R.string.prompt_dream_journal_symbol_analysis)

        historyToSend.add(ChatMessage("$dreamPrompt\"\"\"$dreamText\"\"\"\n\n$userMessageText", Participant.USER))

        viewModelScope.launch {
            try {
                val apiKey = getApiKeyToUse()
                val responseBuilder = StringBuilder()

                geminiRepository.generateChatResponse(historyToSend, apiKey)
                    .onCompletion {
                        _isMinimalAiLoading.value = false
                        val finalResponse = ChatMessage(responseBuilder.toString(), Participant.MODEL, isLoading = false)
                        _minimalAiChatHistory.value = _minimalAiChatHistory.value.dropLast(1) + finalResponse
                    }
                    .collect { chunk ->
                        responseBuilder.append(chunk)
                        _minimalAiChatHistory.value = _minimalAiChatHistory.value.dropLast(1) + ChatMessage(responseBuilder.toString(), Participant.MODEL, isLoading = true)
                    }

            } catch (e: Exception) {
                _isMinimalAiLoading.value = false
                val errorMessage = ChatMessage(e.message ?: stringProvider.getString(R.string.error_unknown), Participant.ERROR, isLoading = false)
                _minimalAiChatHistory.value = _minimalAiChatHistory.value.dropLast(1) + errorMessage
            }
        }
    }

    fun executeDreamEmotionAnalysis() {
        toggleMinimalAiUi(true)
        _isMinimalChatActive.value = true

        val dreamText = noteDescription.value.text
        if (dreamText.isBlank()) {
            viewModelScope.launch {
                _uiEvent.send(stringProvider.getString(R.string.dream_not_found))
            }
            return
        }

        val userMessageText = stringProvider.getString(R.string.emotional_analysis)
        val userMessage = ChatMessage(userMessageText, Participant.USER)
        val loadingMessage = ChatMessage("", Participant.MODEL, isLoading = true)

        _minimalAiChatHistory.value = listOf(userMessage, loadingMessage)
        _isMinimalAiLoading.value = true

        val historyToSend = mutableListOf<ChatMessage>()
        val dreamPrompt = stringProvider.getString(R.string.prompt_dream_journal_emotion_analysis)

        historyToSend.add(ChatMessage("$dreamPrompt\"\"\"$dreamText\"\"\"\n\n$userMessageText", Participant.USER))

        viewModelScope.launch {
            try {
                val apiKey = getApiKeyToUse()
                val responseBuilder = StringBuilder()

                geminiRepository.generateChatResponse(historyToSend, apiKey)
                    .onCompletion {
                        _isMinimalAiLoading.value = false
                        val finalResponse = ChatMessage(responseBuilder.toString(), Participant.MODEL, isLoading = false)
                        _minimalAiChatHistory.value = _minimalAiChatHistory.value.dropLast(1) + finalResponse
                    }
                    .collect { chunk ->
                        responseBuilder.append(chunk)
                        _minimalAiChatHistory.value = _minimalAiChatHistory.value.dropLast(1) + ChatMessage(responseBuilder.toString(), Participant.MODEL, isLoading = true)
                    }

            } catch (e: Exception) {
                _isMinimalAiLoading.value = false
                val errorMessage = ChatMessage(e.message ?: stringProvider.getString(R.string.error_unknown), Participant.ERROR, isLoading = false)
                _minimalAiChatHistory.value = _minimalAiChatHistory.value.dropLast(1) + errorMessage
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

    fun toggleTranslateSheet(isVisible: Boolean) {
        _isTranslateSheetVisible.value = isVisible
    }

    fun onTranslateClicked(forSelection: Boolean) {
        _translationSelection = if (forSelection) {
            noteDescription.value.selection.takeIf { !it.collapsed }
        } else {
            TextRange(0, noteDescription.value.text.length)
        }

        if (forSelection && _translationSelection == null) {
            viewModelScope.launch {
                _uiEvent.send(stringProvider.getString(R.string.ai_error_select_text_to_translate))
            }
            return
        }


        viewModelScope.launch {
            _isAiLoading.value = true
            geminiRepository.getDownloadedModels()
                .onSuccess { downloadedCodes ->
                    val availableLanguages = geminiRepository.supportedLanguages
                        .filter { downloadedCodes.contains(it.key) }
                        .map { it.key to it.value }
                    _downloadedLanguages.value = availableLanguages
                    toggleTranslateSheet(true)
                }
                .onFailure {
                    _uiEvent.send(stringProvider.getString(R.string.error_fetching_downloaded_models, it.message ?: ""))
                }
            _isAiLoading.value = false
        }
    }

    fun executeTranslation(targetLanguageCode: String) {
        toggleTranslateSheet(false)

        val selection = _translationSelection
        if (selection == null || selection.end > noteDescription.value.text.length) {
            return
        }

        val textToTranslate = noteDescription.value.text.substring(selection.start, selection.end)
        if (textToTranslate.isBlank()) return

        viewModelScope.launch {
            _isAiLoading.value = true
            geminiRepository.translateOnDevice(textToTranslate, targetLanguageCode)
                .onSuccess { translatedText ->
                    val originalText = noteDescription.value.text
                    val updatedText = originalText.replaceRange(selection.start, selection.end, translatedText)
                    val newSelectionStart = selection.start + translatedText.length
                    updateNoteDescription(
                        TextFieldValue(
                            text = updatedText,
                            selection = TextRange(newSelectionStart)
                        )
                    )
                }
                .onFailure {
                    _uiEvent.send(stringProvider.getString(R.string.error_translation_failed, it.message ?: ""))
                }
            _isAiLoading.value = false
        }
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
        viewModelScope.launch {
            // --- DÜZELTME BAŞLANGIÇ ---
            val dreamFolderName = stringProvider.getString(R.string.dream_journal)
            // --- DÜZELTME BİTİŞ ---
            try {
                val folders = folderUseCase.getAllFolders().first()
                val folder = folders.find { it.id == note.folderId }
                // --- DÜZELTME BAŞLANGIÇ ---
                if (folder?.name.equals(dreamFolderName, ignoreCase = true)) {
                    // --- DÜZELTME BİTİŞ ---
                    _isDreamJournalMode.value = true
                } else {
                    _isDreamJournalMode.value = false
                }
            } catch (e: Exception) {
                _isDreamJournalMode.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun setupNoteData(id : Int, folderIdFromNav: Long? = null) {
        _isLoading.value = true
        if (id != 0) {
            viewModelScope.launch {
                noteUseCase.getNoteById(id).collectLatest { note ->
                    if (note != null) {
                        syncNote(note)
                    } else {
                        _isLoading.value = false
                    }
                }
            }
        } else {
            updateFolderId(folderIdFromNav)
            if (folderIdFromNav != null) {
                viewModelScope.launch {
                    val dreamFolderName = stringProvider.getString(R.string.dream_journal)
                    try {
                        val folders = folderUseCase.getAllFolders().first()
                        val folder = folders.find { it.id == folderIdFromNav }
                        if (folder?.name.equals(dreamFolderName, ignoreCase = true)) {
                            _isDreamJournalMode.value = true
                        }
                    } catch (e: Exception) {
                        _isDreamJournalMode.value = false
                    } finally {
                        _isLoading.value = false
                    }
                }
            } else {
                _isLoading.value = false
            }
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