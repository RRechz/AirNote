package com.babelsoftware.airnote.data.repository

import androidx.annotation.StringRes
import com.babelsoftware.airnote.R

data class GeminiModelInfo(
    val name: String,
    @StringRes val displayNameResId: Int
)

object GeminiModels {
    val supportedModels = listOf(
        GeminiModelInfo(
            name = "gemini-2.5-flash",
            displayNameResId = R.string.gemini_models_25
        ),
        GeminiModelInfo(
            name = "gemini-2.0-flash-001",
            displayNameResId = R.string.gemini_models_20
        ),
        GeminiModelInfo(
            name = "gemini-1.5-flash-latest",
            displayNameResId = R.string.gemini_models_15
        )
    )
}