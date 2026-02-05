package com.babelsoftware.loudly.lyrics // veya services paketi

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.nl.translate.TranslateLanguage
import com.google.mlkit.nl.translate.Translation
import com.google.mlkit.nl.translate.Translator
import com.google.mlkit.nl.translate.TranslatorOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@Singleton
class MlKitTranslationService @Inject constructor() {

    // ---> Desteklenen hedef dillerin listesi.
    val targetLanguages: List<String> = listOf(
        TranslateLanguage.TURKISH,
        TranslateLanguage.RUSSIAN,
        TranslateLanguage.CHINESE,
        TranslateLanguage.FRENCH,
        TranslateLanguage.GERMAN,
        TranslateLanguage.ITALIAN,
        TranslateLanguage.SPANISH,
        TranslateLanguage.PORTUGUESE,
        TranslateLanguage.JAPANESE,
        TranslateLanguage.KOREAN,
        TranslateLanguage.ARABIC,
        TranslateLanguage.INDONESIAN,
        TranslateLanguage.THAI,
        TranslateLanguage.VIETNAMESE,
        TranslateLanguage.HEBREW,
        TranslateLanguage.URDU,
        TranslateLanguage.GREEK,
        TranslateLanguage.BULGARIAN,
        TranslateLanguage.PERSIAN,
        TranslateLanguage.ROMANIAN,
        TranslateLanguage.UKRAINIAN,
        TranslateLanguage.NORWEGIAN,
        TranslateLanguage.CZECH,
        TranslateLanguage.HUNGARIAN,
        TranslateLanguage.FINNISH,
        TranslateLanguage.SWEDISH,
        TranslateLanguage.DANISH,
        TranslateLanguage.SLOVAK,
        TranslateLanguage.SLOVENIAN,
        TranslateLanguage.LATVIAN,
        TranslateLanguage.LITHUANIAN,
        TranslateLanguage.MACEDONIAN,
        TranslateLanguage.ALBANIAN
    )
    // <---

    /**
     * Creates a Translator object for the specified target language.
     */
    private fun createTranslator(targetLanguage: String): Translator {
        val options = TranslatorOptions.Builder()
            .setSourceLanguage(TranslateLanguage.ENGLISH)
            .setTargetLanguage(targetLanguage)
            .build()
        return Translation.getClient(options)
    }

    /**
     * Downloads the required language model.
     */
    private fun downloadModelIfNeeded(translator: Translator) {
        val conditions = DownloadConditions.Builder()
            .requireWifi()
            .build()
        translator.downloadModelIfNeeded(conditions)
            .addOnSuccessListener { /* The model has been downloaded or is already available. */ }
            .addOnFailureListener { e -> /* Model download error. */ }
    }

    /**
     * Translates the given text from English into the specified target language.
     */
    suspend fun translate(text: String, targetLanguage: String): String {
        val translator = createTranslator(targetLanguage)
        downloadModelIfNeeded(translator)
        return suspendCancellableCoroutine { continuation ->
            translator.translate(text)
                .addOnSuccessListener { translatedText ->
                    continuation.resume(translatedText)
                }
                .addOnFailureListener { e ->
                    continuation.resumeWithException(e)
                }
        }
    }
}