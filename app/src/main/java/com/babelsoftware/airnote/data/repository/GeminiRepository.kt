/*
 * Copyright (c) 2025 Babel Software.
 *
 *
 */

package com.babelsoftware.airnote.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.data.provider.StringProvider
import com.babelsoftware.airnote.domain.model.ChatMessage
import com.babelsoftware.airnote.domain.model.Participant
import com.babelsoftware.airnote.domain.repository.SettingsRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.nl.languageid.LanguageIdentification
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.TranslateRemoteModel
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.TranslatorOptions
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

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

class GeminiRepository @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val stringProvider: StringProvider
) {
    private companion object {
        const val DEFAULT_TEMPERATURE = 0.7f
        const val CREATIVE_TEMPERATURE = 0.8f
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

    suspend fun processAiAction(text: String, action: AiAction, tone: AiTone? = null, apiKey: String): Result<String> {
        if (apiKey.isBlank()) {
            return Result.failure(ApiKeyMissingException(stringProvider.getString(R.string.error_no_user_api_key)))
        }
        if (action == AiAction.CHANGE_TONE) {
            require(tone != null) { "CHANGE_TONE eylemi için bir ton belirtilmelidir." }
        }

        val generativeModel: GenerativeModel = try {
            val modelName = settingsRepository.settings.first().selectedModelName
            GenerativeModel(
                modelName = modelName,
                apiKey = apiKey,
                generationConfig = generationConfig {
                    temperature = DEFAULT_TEMPERATURE
                }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }

        val prompt = when (action) {
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

        return try {
            val response = generativeModel.generateContent(prompt)
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
        apiKey: String
    ): Flow<String> = flow {
        if (apiKey.isBlank()) {
            throw ApiKeyMissingException(stringProvider.getString(R.string.error_no_user_api_key))
        }

        val modelName = settingsRepository.settings.first().selectedModelName
        val generativeModel = GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            generationConfig = generationConfig { temperature = CREATIVE_TEMPERATURE }
        )

        val prompt = when (action) {
            AiAssistantAction.GIVE_IDEA -> stringProvider.getString(R.string.prompt_assistant_give_idea, noteName)
            AiAssistantAction.CONTINUE_WRITING -> stringProvider.getString(R.string.prompt_assistant_continue_writing, noteName, noteDescription)
            AiAssistantAction.CHANGE_PERSPECTIVE -> stringProvider.getString(R.string.prompt_assistant_change_perspective, noteName, noteDescription)
            AiAssistantAction.PROS_AND_CONS -> stringProvider.getString(R.string.prompt_assistant_pros_and_cons, noteDescription)
            AiAssistantAction.CREATE_TODO_LIST -> stringProvider.getString(R.string.prompt_assistant_create_todo, noteDescription)
            AiAssistantAction.SIMPLIFY -> stringProvider.getString(R.string.prompt_assistant_simplify, noteDescription)
            AiAssistantAction.SUGGEST_A_TITLE -> stringProvider.getString(R.string.prompt_assistant_suggest_title, noteDescription)
        }

        generativeModel.generateContentStream(prompt).collect { chunk ->
            emit(chunk.text ?: "")
        }
    }.catch {
        emit(stringProvider.getString(R.string.error_api_request_failed, it.message ?: "Unknown error"))
    }


    fun generateChatResponse(history: List<ChatMessage>, apiKey: String): Flow<String> = flow {
        if (apiKey.isBlank()) {
            throw ApiKeyMissingException(stringProvider.getString(R.string.error_no_user_api_key))
        }

        val modelName = settingsRepository.settings.first().selectedModelName
        val generativeModel = GenerativeModel(
            modelName = modelName,
            apiKey = apiKey,
            generationConfig = generationConfig { temperature = CREATIVE_TEMPERATURE }
        )

        val chatHistoryContent = history
            .filter { it.participant != Participant.ERROR && !it.isLoading }
            .map { msg ->
                content(role = if (msg.participant == Participant.USER) "user" else "model") {
                    text(msg.text)
                }
            }

        val lastMessage = chatHistoryContent.lastOrNull() ?: return@flow
        val historyWithoutLast = chatHistoryContent.dropLast(1)
        val chat = generativeModel.startChat(history = historyWithoutLast)

        chat.sendMessageStream(lastMessage).collect { chunk ->
            emit(chunk.text ?: "")
        }
    }.catch {
        emit(stringProvider.getString(R.string.error_api_request_failed, it.message ?: "Unknown error"))
    }

    suspend fun generateDraft(topic: String, apiKey: String): Result<String> {
        if (apiKey.isBlank()) {
            return Result.failure(ApiKeyMissingException(stringProvider.getString(R.string.error_no_user_api_key)))
        }

        val generativeModel: GenerativeModel = try {
            val modelName = settingsRepository.settings.first().selectedModelName
            GenerativeModel(
                modelName = modelName,
                apiKey = apiKey,
                generationConfig = generationConfig { temperature = CREATIVE_TEMPERATURE }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }

        val prompt = stringProvider.getString(R.string.prompt_assistant_draft_anything, topic)

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text?.let { Result.success(it) } ?: Result.failure(Exception("API'den boş yanıt alındı."))
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    suspend fun generateDraftFromImage(prompt: String, image: Bitmap, apiKey: String): Result<String> {
        if (apiKey.isBlank()) {
            return Result.failure(ApiKeyMissingException(stringProvider.getString(R.string.error_no_user_api_key)))
        }

        val generativeModel: GenerativeModel = try {
            val modelName = settingsRepository.settings.first().selectedModelName
            GenerativeModel(
                modelName = modelName,
                apiKey = apiKey,
                generationConfig = generationConfig { temperature = DEFAULT_TEMPERATURE }
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure(e)
        }

        val inputContent = content {
            image(image)
            text(prompt)
        }

        return try {
            val response = generativeModel.generateContent(inputContent)
            response.text?.let { Result.success(it) } ?: Result.failure(Exception("API'den boş yanıt alındı."))
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
                    continuation.resume(Result.failure(Exception("Source language could not be detected!")))
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

    private suspend fun performTranslation(text: String, sourceLang: String, targetLang: String): Result<String> = suspendCoroutine { continuation ->
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(sourceLang)
            .setTargetLanguage(targetLang)
            .build()
        val translator = Translation.getClient(options)

        translator.downloadModelIfNeeded()
            .addOnSuccessListener {
                translator.translate(text)
                    .addOnSuccessListener { translatedText ->
                        continuation.resume(Result.success(translatedText))
                        translator.close()
                    }
                    .addOnFailureListener { exception ->
                        continuation.resume(Result.failure(exception))
                        translator.close()
                    }
            }
            .addOnFailureListener { exception ->
                continuation.resume(Result.failure(exception))
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
            return@withContext Result.failure(Exception("The note text is already in the language you want to translate it into"))
        }

        val originalLines = text.split('\n')
        val translatedParagraphs = mutableListOf<String>()
        val currentParagraph = StringBuilder()

        try {
            for (line in originalLines) {
                if (line.isBlank()) {
                    if (currentParagraph.isNotEmpty()) {
                        val paragraphToTranslate = currentParagraph.toString()
                        val translatedResult = performTranslation(paragraphToTranslate, sourceLanguageCode, targetLanguage)
                        if (translatedResult.isSuccess) {
                            translatedParagraphs.add(translatedResult.getOrThrow())
                        } else {
                            throw translatedResult.exceptionOrNull()!!
                        }
                        currentParagraph.clear()
                    }
                    translatedParagraphs.add(line)
                } else {
                    if (currentParagraph.isNotEmpty()) {
                        currentParagraph.append("\n")
                    }
                    currentParagraph.append(line)
                }
            }

            if (currentParagraph.isNotEmpty()) {
                val paragraphToTranslate = currentParagraph.toString()
                val translatedResult = performTranslation(paragraphToTranslate, sourceLanguageCode, targetLanguage)
                if (translatedResult.isSuccess) {
                    translatedParagraphs.add(translatedResult.getOrThrow())
                } else {
                    throw translatedResult.exceptionOrNull()!!
                }
            }

            Result.success(translatedParagraphs.joinToString("\n"))

        } catch (e: Exception) {
            Log.e("TranslateError", "Paragraf çevirilirken hata oluştu", e)
            Result.failure(e)
        }
    }
}