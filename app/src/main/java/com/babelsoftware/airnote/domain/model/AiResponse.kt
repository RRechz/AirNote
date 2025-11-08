package com.babelsoftware.airnote.domain.model

import com.google.gson.annotations.SerializedName

/**
 * Data class representing the JSON response from Gemini.
 * Depending on the model's “intent,” it carries either a chat response or new note content.
 */
data class AiResponse(
    @SerializedName("intent") val intent: String,
    @SerializedName("response") val response: String? = null,
    @SerializedName("new_note_content") val newNoteContent: String? = null
)

/**
 * Defines the AI's possible intents
 */
enum class AiIntent {
    CHAT,
    EDIT_NOTE,
    UNKNOWN
}