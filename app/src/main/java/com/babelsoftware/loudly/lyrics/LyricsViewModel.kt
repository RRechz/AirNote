package com.babelsoftware.loudly.lyrics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.babelsoftware.loudly.db.entities.LyricsEntity
import com.babelsoftware.loudly.models.MediaMetadata
import com.babelsoftware.loudly.reportException
import com.google.mlkit.nl.languageid.LanguageIdentification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class LyricsUiState {
    data object Loading : LyricsUiState()
    data class Success(val entries: List<LyricsEntry>) : LyricsUiState()
    data class Error(val message: String) : LyricsUiState()
}

@HiltViewModel
class LyricsViewModel @Inject constructor(
    private val lyricsHelper: LyricsHelper,
    private val translationService: MlKitTranslationService
) : ViewModel() {

    private val _lyricsState = MutableStateFlow<LyricsUiState>(LyricsUiState.Success(emptyList()))
    val lyricsState: StateFlow<LyricsUiState> = _lyricsState
    val targetLanguages: List<String> = translationService.targetLanguages

    private var originalLyrics: String? = null
    private var isTranslationActive = false
    private var sourceLanguage: String? = null

    fun loadLyricsFor(mediaMetadata: MediaMetadata) {
        viewModelScope.launch {
            isTranslationActive = false
            _lyricsState.value = LyricsUiState.Loading

            try {
                val lyricsResult = withContext(Dispatchers.IO) {
                    lyricsHelper.getLyrics(mediaMetadata)
                }

                if (lyricsResult == LyricsEntity.LYRICS_NOT_FOUND) {
                    _lyricsState.value = LyricsUiState.Error("Not_found_LYRİCS")
                    return@launch
                }

                originalLyrics = lyricsResult
                identifyLanguage(lyricsResult)

                val parsed = parseLyricsSafely(lyricsResult)
                _lyricsState.value = LyricsUiState.Success(parsed)
            } catch (e: Exception) {
                reportException(e)
                _lyricsState.value = LyricsUiState.Error("An error occurred while loading the lyrics.")
            }
        }
    }

    private fun identifyLanguage(text: String) {
        val languageIdentifier = LanguageIdentification.getClient()
        languageIdentifier.identifyLanguage(text)
            .addOnSuccessListener { languageCode ->
                sourceLanguage = if (languageCode != "und") languageCode else null
            }
            .addOnFailureListener {
                sourceLanguage = null
            }
    }

    fun translateLyrics(targetLanguage: String) {
        viewModelScope.launch {
            if (originalLyrics == null) {
                _lyricsState.value = LyricsUiState.Error("No lyrics to translate found.")
                return@launch
            }

            if (sourceLanguage != "en") {
                _lyricsState.value = LyricsUiState.Error("Currently, only English song lyrics can be translated.")
                return@launch
            }

            isTranslationActive = true
            _lyricsState.value = LyricsUiState.Loading
            try {
                val translatedText = withContext(Dispatchers.IO) {
                    translationService.translate(originalLyrics!!, targetLanguage)
                }
                val parsed = parseLyricsSafely(translatedText)
                _lyricsState.value = LyricsUiState.Success(parsed)
            } catch (e: Exception) {
                reportException(e)
                _lyricsState.value = LyricsUiState.Error("Translation failed.")
                isTranslationActive = false
            }
        }
    }

    private suspend fun parseLyricsSafely(text: String): List<LyricsEntry> {
        return withContext(Dispatchers.Default) {
            try {
                LyricsUtils.parseLyrics(text, trim = true, multilineEnable = false)
            } catch (e: Exception) {
                reportException(e)
                emptyList()
            }
        }
    }
}