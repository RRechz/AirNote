/*
 * Copyright (c) 2025 Babel Software.
 */

package com.babelsoftware.airnote.data.repository

import android.graphics.Bitmap
import com.babelsoftware.airnote.R
import com.babelsoftware.airnote.data.provider.StringProvider
import com.babelsoftware.airnote.domain.model.ChatMessage
import com.babelsoftware.airnote.domain.model.Participant
import com.babelsoftware.airnote.domain.repository.SettingsRepository
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext

enum class AiAction {
    IMPROVE_WRITING,
    SUMMARIZE,
    MAKE_SHORTER,
    MAKE_LONGER,
    CHANGE_TONE
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
    /**
     * Tests if the given API key is valid.
     */
    suspend fun validateApiKey(apiKey: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            GenerativeModel(modelName = "gemini-2.0-flash-001", apiKey = apiKey)
                .countTokens("test")
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Sends a request to the Gemini API based on the given text and AI action.
     */
    suspend fun processAiAction(text: String, action: AiAction, tone: AiTone? = null, apiKey: String): String? {
        if (action == AiAction.CHANGE_TONE) {
            require(tone != null) { "CHANGE_TONE eylemi için bir ton belirtilmelidir." }
        }

        val currentSettings = settingsRepository.settings.first()

        if (apiKey.isBlank()) {
            return stringProvider.getString(R.string.error_no_user_api_key)
        }

        val generativeModel = GenerativeModel(
            modelName = currentSettings.selectedModelName,
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.7f
            }
        )

        // Custom prompt by action | AI Functions
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
                // Ana prompt şablonu ile ton talimatını birleştiriyoruz
                stringProvider.getString(R.string.prompt_change_tone_template, stringProvider.getString(tonePromptResId), text)
            }
        }

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text
        } catch (e: Exception) {
            e.printStackTrace()
            stringProvider.getString(R.string.error_api_request_failed, e.message ?: "Unknown error")
        }
    }

    fun processAssistantAction(
        noteName: String,
        noteDescription: String,
        action: AiAssistantAction,
        apiKey: String
    ): Flow<String> = flow {
        val currentSettings = settingsRepository.settings.first()

        if (apiKey.isBlank()) {
            emit(stringProvider.getString(R.string.error_no_user_api_key))
            return@flow
        }

        val generativeModel = GenerativeModel(
            modelName = currentSettings.selectedModelName,
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.8f
            }
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
        val currentSettings = settingsRepository.settings.first()

        if (apiKey.isBlank()) {
            emit(stringProvider.getString(R.string.error_no_user_api_key))
            return@flow
        }

        val generativeModel = GenerativeModel(
            modelName = currentSettings.selectedModelName,
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.8f
            }
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

    // ---> Function that creates a one-time note outline
    suspend fun generateDraft(
        topic: String,
        apiKey: String
        ): String? {
        val currentSettings = settingsRepository.settings.first()

        if (apiKey.isNullOrBlank()) {
            return stringProvider.getString(R.string.error_no_user_api_key)
        }

        val generativeModel = GenerativeModel(
            modelName = currentSettings.selectedModelName,
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.8f
            }
        )

        val prompt = stringProvider.getString(R.string.prompt_assistant_draft_anything, topic)

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text
        } catch (e: Exception) {
            e.printStackTrace()
            stringProvider.getString(R.string.error_api_request_failed, e.message ?: "Bilinmeyen Hata")
        }
    }
    // <---

    /**
     * Creates a note draft from Gemini using a given image and command.
     */
    suspend fun generateDraftFromImage(
        prompt: String,
        image: Bitmap,
        apiKey: String
    ): String? {
        val currentSettings = settingsRepository.settings.first()

        if (apiKey.isBlank()) {
            return stringProvider.getString(R.string.error_no_user_api_key)
        }

        val generativeModel = GenerativeModel(
            modelName = currentSettings.selectedModelName,
            apiKey = apiKey,
            generationConfig = generationConfig {
                temperature = 0.7f
            }
        )

        val inputContent = content {
            image(image)
            text(prompt)
        }

        return try {
            val response = generativeModel.generateContent(inputContent)
            response.text
        } catch (e: Exception) {
            e.printStackTrace()
            stringProvider.getString(R.string.error_api_request_failed, e.message ?: "Bilinmeyen Hata")
        }
    }
}