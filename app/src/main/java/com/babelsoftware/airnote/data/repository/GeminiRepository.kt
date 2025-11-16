/*
 * Copyright (c) 2025 Babel Software.
 *
 *
 */

package com.babelsoftware.airnote.data.repository

import android.util.Log
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.data.provider.StringProvider
import com.babelsoftware.airnote.domain.model.ChatMessage
import com.babelsoftware.airnote.domain.model.Note
import com.babelsoftware.airnote.domain.model.Participant
import com.babelsoftware.airnote.domain.repository.SettingsRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.Content
import com.google.ai.client.generativeai.type.Part
import com.google.ai.client.generativeai.type.TextPart
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import com.google.gson.annotations.SerializedName
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Represents the different personalities or modes the AI can operate in.
 */
enum class AiMode {
    NOTE_ASSISTANT, // For factual, note-taking tasks.
    CREATIVE_MIND,  // For brainstorming and creative writing.
    ACADEMIC_RESEARCHER,// For structured, evidence-based academic writing.
    PROFESSIONAL_STRATEGIST // For clear, goal-oriented business communication.
}

enum class AiAction {
    IMPROVE_WRITING,
    SUMMARIZE,
    MAKE_SHORTER,
    MAKE_LONGER,
    CHANGE_TONE,
    TRANSLATE
}

enum class AiTone {
    FORMAL,
    BALANCED,
    FRIENDLY
}

enum class AiAssistantAction {
    GIVE_IDEA,
    CONTINUE_WRITING,
    CHANGE_PERSPECTIVE,
    PROS_AND_CONS,
    CREATE_TODO_LIST,
    SIMPLIFY,
    SUGGEST_A_TITLE
}

data class AiActionPlan(
    @SerializedName("thought")
    val thought: String?,

    @SerializedName("actions")
    val actions: List<AiActionCommand>,

    @SerializedName("response_message")
    val response_message: String
)

data class AiActionCommand(
    @SerializedName("action_type")
    val action_type: String,
    @SerializedName("title")
    val title: String?,
    @SerializedName("content")
    val content: String?,
    @SerializedName("tasks")
    val tasks: List<String>?,
    @SerializedName("name")
    val name: String?,
    @SerializedName("iconName")
    val iconName: String?,
    @SerializedName("note_title")
    val note_title: String?,
    @SerializedName("folder_name")
    val folder_name: String?,
    @SerializedName("response")
    val response: String?,
    @SerializedName("new_content")
    val new_content: String?,
    @SerializedName("search_term")
    val search_term: String?
)

class GeminiRepository @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val stringProvider: StringProvider
) {
    private companion object {
        const val DEFAULT_TEMPERATURE = 0.7f
        const val CREATIVE_TEMPERATURE = 0.9f
    }
    class ApiKeyMissingException(message: String) : Exception(message)

    suspend fun validateApiKey(apiKey: String, modelName: String): Result<Unit> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(ApiKeyMissingException("API key cannot be empty."))
        }
        return@withContext try {
            GenerativeModel(modelName = modelName, apiKey = apiKey).countTokens("test")
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun processAiAction(text: String, action: AiAction, tone: AiTone? = null, apiKey: String, aiMode: AiMode = AiMode.NOTE_ASSISTANT): Result<String> {
        if (apiKey.isBlank()) {
            return Result.failure(ApiKeyMissingException(stringProvider.getString(R.string.error_no_user_api_key)))
        }
        if (action == AiAction.CHANGE_TONE) {
            require(tone != null) { "CHANGE_TONE eylemi için bir ton belirtilmelidir." }
        }

        val modelName = settingsRepository.settings.first().selectedModelName
        val generativeModel: GenerativeModel = try {
            GenerativeModel(
                modelName = modelName,
                apiKey = apiKey,
                generationConfig = generationConfig {
                    this.temperature = if (aiMode == AiMode.CREATIVE_MIND) CREATIVE_TEMPERATURE else DEFAULT_TEMPERATURE
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }

        val systemPrompt = when (aiMode) {
            AiMode.NOTE_ASSISTANT -> stringProvider.getString(R.string.system_prompt_note_assistant)
            AiMode.CREATIVE_MIND -> stringProvider.getString(R.string.system_prompt_creative_mind)
            AiMode.ACADEMIC_RESEARCHER -> stringProvider.getString(R.string.system_prompt_academic_researcher)
            AiMode.PROFESSIONAL_STRATEGIST -> stringProvider.getString(R.string.system_prompt_professional_strategist)
        }

        val userPrompt = when (action) {
            AiAction.IMPROVE_WRITING -> stringProvider.getString(R.string.prompt_improve_writing, text)
            AiAction.SUMMARIZE -> stringProvider.getString(R.string.prompt_summarize, text)
            AiAction.MAKE_SHORTER -> stringProvider.getString(R.string.prompt_make_shorter, text)
            AiAction.MAKE_LONGER -> stringProvider.getString(R.string.prompt_make_longer, text)
            AiAction.CHANGE_TONE -> {
                val tonePromptResId = when (tone) {
                    AiTone.FORMAL -> R.string.prompt_instruction_tone_formal
                    AiTone.BALANCED -> R.string.prompt_instruction_tone_balanced
                    AiTone.FRIENDLY -> R.string.prompt_instruction_tone_friendly
                    null -> throw IllegalArgumentException("Tone cannot be null for CHANGE_TONE action")
                }
                stringProvider.getString(R.string.prompt_change_tone_template, stringProvider.getString(tonePromptResId), text)
            }
            AiAction.TRANSLATE -> "" // This action is handled on-device
        }

        val finalPrompt = "$systemPrompt\n\n---\n\n$userPrompt"

        return try {
            val response = generativeModel.generateContent(finalPrompt)
            response.text?.let { Result.success(it) } ?: Result.failure(Exception("API'den boş yanıt alındı."))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    fun processAssistantAction(
        noteName: String,
        noteDescription: String,
        action: AiAssistantAction,
        apiKey: String,
        aiMode: AiMode = AiMode.CREATIVE_MIND
    ): Flow<String> = flow {
        if (apiKey.isBlank()) {
            throw ApiKeyMissingException(stringProvider.getString(R.string.error_no_user_api_key))
        }

        val modelName = settingsRepository.settings.first().selectedModelName
        val generativeModel = GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            generationConfig = generationConfig {
                this.temperature = if (aiMode == AiMode.CREATIVE_MIND) CREATIVE_TEMPERATURE else DEFAULT_TEMPERATURE
            }
        )

        val systemPrompt = when (aiMode) {
            AiMode.NOTE_ASSISTANT -> stringProvider.getString(R.string.system_prompt_note_assistant)
            AiMode.CREATIVE_MIND -> stringProvider.getString(R.string.system_prompt_creative_mind)
            AiMode.ACADEMIC_RESEARCHER -> stringProvider.getString(R.string.system_prompt_academic_researcher)
            AiMode.PROFESSIONAL_STRATEGIST -> stringProvider.getString(R.string.system_prompt_professional_strategist)
        }

        val userPrompt = when (action) {
            AiAssistantAction.GIVE_IDEA -> stringProvider.getString(R.string.prompt_assistant_give_idea, noteName)
            AiAssistantAction.CONTINUE_WRITING -> stringProvider.getString(R.string.prompt_assistant_continue_writing, noteName, noteDescription)
            AiAssistantAction.CHANGE_PERSPECTIVE -> stringProvider.getString(R.string.prompt_assistant_change_perspective, noteName, noteDescription)
            AiAssistantAction.PROS_AND_CONS -> stringProvider.getString(R.string.prompt_assistant_pros_and_cons, noteDescription)
            AiAssistantAction.CREATE_TODO_LIST -> stringProvider.getString(R.string.prompt_assistant_create_todo, noteDescription)
            AiAssistantAction.SIMPLIFY -> stringProvider.getString(R.string.prompt_assistant_simplify, noteDescription)
            AiAssistantAction.SUGGEST_A_TITLE -> stringProvider.getString(R.string.prompt_assistant_suggest_title, noteDescription)
        }

        val finalPrompt = "$systemPrompt\n\n---\n\n$userPrompt"

        generativeModel.generateContentStream(finalPrompt).collect { chunk ->
            emit(chunk.text ?: "")
        }
    }.catch {
        emit(stringProvider.getString(R.string.error_api_request_failed, it.message ?: "Unknown error"))
    }

    fun generateChatResponse(
        history: List<ChatMessage>,
        apiKey: String,
        aiMode: AiMode = AiMode.NOTE_ASSISTANT,
        mentionedNote: com.babelsoftware.airnote.domain.model.Note? = null,
        attachment: Part? = null
    ): Flow<String> = flow {
        if (apiKey.isBlank()) {
            throw ApiKeyMissingException(stringProvider.getString(R.string.error_no_user_api_key))
        }

        val modelName = settingsRepository.settings.first().selectedModelName
        val generativeModel = GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            generationConfig = generationConfig {
                this.temperature = if (aiMode == AiMode.CREATIVE_MIND) CREATIVE_TEMPERATURE else DEFAULT_TEMPERATURE
            }
        )

        val baseSystemPrompt = when (aiMode) {
            AiMode.NOTE_ASSISTANT -> stringProvider.getString(R.string.system_prompt_note_assistant)
            AiMode.CREATIVE_MIND -> stringProvider.getString(R.string.system_prompt_creative_mind)
            AiMode.ACADEMIC_RESEARCHER -> stringProvider.getString(R.string.system_prompt_academic_researcher)
            AiMode.PROFESSIONAL_STRATEGIST -> stringProvider.getString(R.string.system_prompt_professional_strategist)
        }

        val chatHistoryForModel = mutableListOf<Content>()
        chatHistoryForModel.add(content("user") { text(baseSystemPrompt) })
        chatHistoryForModel.add(content("model") { text("OK.") })

        if (mentionedNote != null) {
            val noteContextPrompt = stringProvider.getString(
                R.string.prompt_mention_context,
                mentionedNote.name,
                mentionedNote.description
            )
            chatHistoryForModel.add(content("user") { text(noteContextPrompt) })
            val ackPrompt = stringProvider.getString(R.string.prompt_mention_ack, mentionedNote.name)
            chatHistoryForModel.add(content("model") { text(ackPrompt) })
        }

        val userHistoryContent = history
            .filter { it.participant != Participant.ERROR && !it.isLoading }
            .map { msg ->
                content(role = if (msg.participant == Participant.USER) "user" else "model") {
                    text(msg.text)
                }
            }
        chatHistoryForModel.addAll(userHistoryContent)


        val lastMessageContent = chatHistoryForModel.lastOrNull() ?: return@flow
        val historyWithoutLast = chatHistoryForModel.dropLast(1)
        val chat = generativeModel.startChat(history = historyWithoutLast)

        val finalMessageToSend: Content
        if (attachment != null) {
            val lastUserText = (lastMessageContent.parts.firstOrNull() as? TextPart)?.text ?: ""

            finalMessageToSend = content(lastMessageContent.role) {
                part(attachment)
                text(lastUserText)
            }
        } else {
            finalMessageToSend = lastMessageContent
        }

        chat.sendMessageStream(finalMessageToSend).collect { chunk ->
            emit(chunk.text ?: "")
        }
    }.catch {
        emit(stringProvider.getString(R.string.error_api_request_failed, it.message ?: "Unknown error"))
    }

    suspend fun generateDraft(topic: String, apiKey: String, aiMode: AiMode = AiMode.NOTE_ASSISTANT): Result<String> {
        if (apiKey.isBlank()) {
            return Result.failure(ApiKeyMissingException(stringProvider.getString(R.string.error_no_user_api_key)))
        }

        val modelName = settingsRepository.settings.first().selectedModelName
        val generativeModel: GenerativeModel = try {
            GenerativeModel(
                modelName = modelName,
                apiKey = apiKey,
                generationConfig = generationConfig {
                    this.temperature = if (aiMode == AiMode.CREATIVE_MIND) CREATIVE_TEMPERATURE else DEFAULT_TEMPERATURE
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }

        val systemPrompt = when (aiMode) {
            AiMode.NOTE_ASSISTANT -> stringProvider.getString(R.string.system_prompt_note_assistant)
            AiMode.CREATIVE_MIND -> stringProvider.getString(R.string.system_prompt_creative_mind)
            AiMode.ACADEMIC_RESEARCHER -> stringProvider.getString(R.string.system_prompt_academic_researcher)
            AiMode.PROFESSIONAL_STRATEGIST -> stringProvider.getString(R.string.system_prompt_professional_strategist)
        }
        val userPrompt = stringProvider.getString(R.string.prompt_assistant_draft_anything, topic)
        val finalPrompt = "$systemPrompt\n\n---\n\n$userPrompt"

        return try {
            val response = generativeModel.generateContent(finalPrompt)
            response.text?.let { Result.success(it) } ?: Result.failure(Exception("API'den boş yanıt alındı."))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun generateDraftFromAttachment(
        prompt: String,
        attachment: Part,
        apiKey: String,
        aiMode: AiMode
    ): Result<String> {
        if (apiKey.isBlank()) {
            return Result.failure(ApiKeyMissingException(stringProvider.getString(R.string.error_no_user_api_key)))
        }

        val generativeModel: GenerativeModel = try {
            val modelName = settingsRepository.settings.first().selectedModelName
            val temperature = if (aiMode == AiMode.CREATIVE_MIND) CREATIVE_TEMPERATURE else DEFAULT_TEMPERATURE

            GenerativeModel(
                modelName = modelName,
                apiKey = apiKey,
                generationConfig = generationConfig {
                    this.temperature = temperature
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }

        val systemPrompt = when (aiMode) {
            AiMode.NOTE_ASSISTANT -> stringProvider.getString(R.string.system_prompt_note_assistant)
            AiMode.CREATIVE_MIND -> stringProvider.getString(R.string.system_prompt_creative_mind)
            AiMode.ACADEMIC_RESEARCHER -> stringProvider.getString(R.string.system_prompt_academic_researcher)
            AiMode.PROFESSIONAL_STRATEGIST -> stringProvider.getString(R.string.system_prompt_professional_strategist)
        }

        val userPrompt = """
        Kullanıcı prompt'u: "$prompt"
        
        Ekli dosyayı/görseli analiz et ve bu prompt'a göre bir not taslağı oluştur.
        Cevabını MUTLAKA şu formatta ver:
        TITLE: [buraya başlığı yaz]
        
        CONTENT: [buraya not içeriğini yaz]
        """.trimIndent()

        val finalPrompt = "$systemPrompt\n\n---\n\n$userPrompt"

        val inputContent = content {
            part(attachment)
            text(finalPrompt)
        }

        return try {
            val response = generativeModel.generateContent(inputContent)
            response.text?.let { Result.success(it) } ?: Result.failure(Exception("API'den boş yanıt alındı."))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun generateChatOrCommandResponse(
        noteContext: String,
        userRequest: String,
        chatHistory: List<ChatMessage>,
        apiKey: String
    ): Result<String> = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            return@withContext Result.failure(ApiKeyMissingException(stringProvider.getString(R.string.error_no_user_api_key)))
        }

        try {
            val modelName = settingsRepository.settings.first().selectedModelName
            val generativeModel = GenerativeModel(
                modelName = modelName,
                apiKey = apiKey,
                generationConfig = generationConfig {
                    temperature = 0.4f
                }
            )

            val systemPrompt = buildString {
                append(stringProvider.getString(R.string.system_prompt_command_chat_header))
                append("\n\n")
                append(stringProvider.getString(R.string.system_prompt_intent_chat_desc))
                append("\n\n")
                append(stringProvider.getString(R.string.system_prompt_intent_edit_desc))
                append("\n\n")
                append(stringProvider.getString(R.string.system_prompt_json_instruction))
            }

            val historyForModel = mutableListOf<Content>()
            val initialContextPrompt = buildString {
                append(systemPrompt)
                append("\n\n")
                append(stringProvider.getString(R.string.system_prompt_context_header, noteContext))
            }
            historyForModel.add(content("user") { text(initialContextPrompt) })
            historyForModel.add(content("model") { text("OK. I am ready. Waiting for user request.") })

            val previousMessages = chatHistory
                .filter { !it.isLoading && it.participant != Participant.ERROR }
                .map { msg ->
                    content(role = if (msg.participant == Participant.USER) "user" else "model") {
                        text(msg.text)
                    }
                }
            historyForModel.addAll(previousMessages)

            val finalRequestPrompt = stringProvider.getString(R.string.system_prompt_request_header, userRequest)
            val chat = generativeModel.startChat(history = historyForModel)
            val response = chat.sendMessage(finalRequestPrompt)
            val responseText = response.text

            if (responseText.isNullOrBlank()) {
                Result.failure(Exception("API'den boş yanıt alındı."))
            } else {
                val cleanJson = responseText.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                Result.success(cleanJson)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun generateActionPlan(
        userRequest: String,
        chatHistory: List<ChatMessage>,
        apiKey: String,
        aiMode: AiMode,
        mentionedNote: Note?
    ): Result<String> = withContext(Dispatchers.IO) {

        if (apiKey.isBlank()) {
            return@withContext Result.failure(ApiKeyMissingException(stringProvider.getString(R.string.error_no_user_api_key)))
        }

        try {
            val modelName = settingsRepository.settings.first().selectedModelName
            val generativeModel = GenerativeModel(
                modelName = modelName,
                apiKey = apiKey,
                generationConfig = generationConfig {
                    this.temperature = if (aiMode == AiMode.CREATIVE_MIND) 0.5f else 0.2f
                }
            )
            val systemPrompt = stringProvider.getString(R.string.system_prompt_automation_engine)
            val historyForModel = mutableListOf<Content>()
            historyForModel.add(content("user") { text(systemPrompt) })
            historyForModel.add(content("model") { text("OK. I am ready to generate JSON action plans.") })

            if (mentionedNote != null) {
                val noteContextPrompt = """
                --- CURRENT NOTE CONTEXT ---
                Title: "${mentionedNote.name}"
                Content: "${mentionedNote.description}"
            """.trimIndent()
                historyForModel.add(content("user") { text(noteContextPrompt) })
                historyForModel.add(content("model") { text("OK. Context for note '${mentionedNote.name}' has been loaded.") })
            }

            val previousMessages = chatHistory
                .filter { !it.isLoading && it.participant != Participant.ERROR }
                .map { msg ->
                    content(role = if (msg.participant == Participant.USER) "user" else "model") {
                        text(msg.text)
                    }
                }
            historyForModel.addAll(previousMessages)

            val chat = generativeModel.startChat(history = historyForModel)
            val response = chat.sendMessage(userRequest)
            val responseText = response.text

            if (responseText.isNullOrBlank()) {
                Result.failure(Exception("AI'den boş yanıt alındı."))
            } else {
                val cleanJson = responseText.trim()
                    .removePrefix("```json")
                    .removePrefix("```")
                    .removeSuffix("```")
                    .trim()

                Log.d("GeminiRepository", "Raw JSON Plan: $cleanJson")
                Result.success(cleanJson)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
    val supportedLanguages = mapOf(
        TranslateLanguage.ENGLISH to "English",
        TranslateLanguage.TURKISH to "Türkçe",
        TranslateLanguage.GERMAN to "Deutsch",
        TranslateLanguage.FRENCH to "Français",
        TranslateLanguage.SPANISH to "Español",
        TranslateLanguage.ITALIAN to "Italiano",
        TranslateLanguage.JAPANESE to "日本語",
        TranslateLanguage.RUSSIAN to "Русский"
    )

    suspend fun getDownloadedModels(): Result<Set<String>> = withContext(Dispatchers.IO) {
        suspendCoroutine { continuation ->
            RemoteModelManager.getInstance().getDownloadedModels(TranslateRemoteModel::class.java)
                .addOnSuccessListener { models ->
                    continuation.resume(Result.success(models.map { it.language }.toSet()))
                }
                .addOnFailureListener {
                    continuation.resume(Result.failure(it))
                }
        }
    }

    suspend fun downloadLanguageModel(languageCode: String): Result<Unit> = withContext(Dispatchers.IO) {
        suspendCoroutine { continuation ->
            val model = TranslateRemoteModel.Builder(languageCode).build()
            val conditions = DownloadConditions.Builder().requireWifi().build()
            RemoteModelManager.getInstance().download(model, conditions)
                .addOnSuccessListener {
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener {
                    continuation.resume(Result.failure(it))
                }
        }
    }
    suspend fun deleteLanguageModel(languageCode: String): Result<Unit> = withContext(Dispatchers.IO) {
        suspendCoroutine { continuation ->
            val model = TranslateRemoteModel.Builder(languageCode).build()
            RemoteModelManager.getInstance().deleteDownloadedModel(model)
                .addOnSuccessListener {
                    continuation.resume(Result.success(Unit))
                }
                .addOnFailureListener {
                    continuation.resume(Result.failure(it))
                }
        }
    }

    private suspend fun identifyLanguage(text: String): Result<String> = suspendCoroutine { continuation ->
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                if (languageCode == "und") {
                    continuation.resume(Result.failure(Exception("The source language could not be identified.")))
                } else {
                    continuation.resume(Result.success(languageCode))
                }
                languageIdentifier.close()
            }
            .addOnFailureListener {
                continuation.resume(Result.failure(it))
                languageIdentifier.close()
            }
    }

    suspend fun translateOnDevice(text: String, targetLanguage: String): Result<String> = withContext(Dispatchers.IO) {
        if (text.isBlank()) return@withContext Result.success("")

        val langIdResult = identifyLanguage(text)
        if (langIdResult.isFailure) {
            return@withContext Result.failure(langIdResult.exceptionOrNull()!!)
        }
        val sourceLanguageCode = langIdResult.getOrNull()!!

        if (sourceLanguageCode.equals(targetLanguage, ignoreCase = true)) {
            return@withContext Result.failure(Exception("The note text is already in the language you want to translate."))
        }

        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLanguageCode)
            .setTargetLanguage(targetLanguage)
            .build()
        val translator = Translation.getClient(options)

        try {
            val downloadResult = suspendCoroutine<Result<Unit>> { continuation ->
                translator.downloadModelIfNeeded()
                    .addOnSuccessListener { continuation.resume(Result.success(Unit)) }
                    .addOnFailureListener { continuation.resume(Result.failure(it)) }
            }
            if (downloadResult.isFailure) {
                throw downloadResult.exceptionOrNull()!!
            }

            val originalLines = text.split('\n')
            val translatedLines = mutableListOf<String>()

            for (line in originalLines) {
                if (line.isBlank()) {
                    translatedLines.add(line)
                } else {
                    val translatedLineResult = suspendCoroutine<Result<String>> { continuation ->
                        translator.translate(line)
                            .addOnSuccessListener { translatedText -> continuation.resume(Result.success(translatedText)) }
                            .addOnFailureListener { exception -> continuation.resume(Result.failure(exception)) }
                    }

                    if (translatedLineResult.isSuccess) {
                        translatedLines.add(translatedLineResult.getOrThrow())
                    } else {
                        throw translatedLineResult.exceptionOrNull()!!
                    }
                }
            }
            Result.success(translatedLines.joinToString("\n"))

        } catch (e: Exception) {
            Log.e("TranslateError", "Satır satır çeviri sırasında hata oluştu", e)
            Result.failure(e)
        } finally {
            translator.close()
        }
    }
}
