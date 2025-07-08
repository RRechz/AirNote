package com.babelsoftware.airnote.data.repository

object GeminiModels {
    val supportedModels = listOf(
        "gemini-pro",
        "gemini-pro-vision",
        "gemini-1.5-flash-latest",
        "gemini-1.5-pro-latest",
        "gemini-pro-latest",
        "gemini-2.0-flash-001",
        "gemini-2.0-pro-001",
        // ---> Experimental models
        "embedding-001",
        "text-multilingual-embedding-002",
        "text-embedding-004",
        "text-embedding-ada-002",
        "text-embedding-ada-001"
        // <---
    )
}