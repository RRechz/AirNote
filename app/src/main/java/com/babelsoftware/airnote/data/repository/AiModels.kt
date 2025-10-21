package com.babelsoftware.airnote.data.repository

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Accessibility
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.EditNote
import androidx.compose.material.icons.rounded.FlashOn
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.ui.graphics.vector.ImageVector

data class GeminiModelInfo(
    val name: String,
    val icon: ImageVector
)

object GeminiModels {
    val supportedModels = listOf(
        GeminiModelInfo("gemini-pro", Icons.Rounded.Accessibility),
        GeminiModelInfo("gemini-pro-vision", Icons.Rounded.Image),
        GeminiModelInfo("gemini-1.5-flash-latest", Icons.Rounded.EditNote),
        GeminiModelInfo("gemini-1.5-pro-latest", Icons.Rounded.AutoAwesome),
        "gemini-2.0-flash-001" to Icons.Rounded.FlashOn,
        "gemini-2.0-pro-001" to Icons.Rounded.Psychology,
    ).map {
        if (it is Pair<*, *>) {
            GeminiModelInfo(it.first as String, it.second as ImageVector)
        } else {
            it as GeminiModelInfo
        }
    }
}
