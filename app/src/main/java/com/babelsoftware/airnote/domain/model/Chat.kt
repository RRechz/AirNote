package com.babelsoftware.airnote.domain.model

import androidx.compose.ui.graphics.vector.ImageVector

// Indicates who the message belongs to (User or Model)
enum class Participant {
    USER, MODEL, ERROR
}

// Data class representing a single chat message
data class ChatMessage(
    val text: String,
    val participant: Participant,
    val isLoading: Boolean = false // Loading animation while waiting for the model response
)

data class AiSuggestion(
    val title: String,
    val icon: ImageVector,
    val action: () -> Unit // Tıklandığında ViewModel'deki hangi fonksiyonu çağıracağı
)