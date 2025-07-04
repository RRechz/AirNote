/*
 * Copyright (c) 2025 Babel Software.
 */

package com.babelsoftware.airnote.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.babelsoftware.airnote.domain.repository.SettingsRepository
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

enum class AiAction(val displayName: String) {
    IMPROVE_WRITING("Yazımı İyileştir"),
    SUMMARIZE("Özetle"),
    MAKE_SHORTER("Daha Kısa Yap"),
    MAKE_LONGER("Daha Uzun Yap"),
    CHANGE_TONE("Change Tone")
}

enum class AiTone(val displayName: String, val promptInstruction: String) {
    FORMAL(
        displayName = "Formal",
        promptInstruction = "Aşağıdaki metni profesyonel ve resmi bir dille yeniden yaz. Anlamı koru, sadece tonu değiştir. Sadece sonuç metnini cevap olarak ver:"
    ),
    BALANCED(
        displayName = "Balanced",
        promptInstruction = "Aşağıdaki metni ne çok resmi ne de çok samimi, dengeli bir dille yeniden yaz. Anlamı koru. Sadece sonuç metnini cevap olarak ver:"
    ),
    FRIENDLY(
        displayName = "Friendly",
        promptInstruction = "Aşağıdaki metni daha samimi, sıcak ve arkadaşça bir dille yeniden yaz. Anlamı koru. Sadece sonuç metnini cevap olarak ver:"
    )
}

enum class AiAssistantAction(val displayName: String) {
    GIVE_IDEA("Bana bir fikir ver"),
    CONTINUE_WRITING("Yazmaya devam et"),
    CHANGE_PERSPECTIVE("Farklı bir açıdan yaklaş"),
    PROS_AND_CONS("Artı ve Eksileri"),
    CREATE_TODO_LIST("Yapılacaklar Listesi Oluştur"),
    SIMPLIFY("Basitleştir"),
    SUGGEST_A_TITLE("Başlık Öner")
}

class GeminiRepository @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val secureStorageRepository: SecureStorageRepository
) {
    private val airNoteApiKey = "AIzaSyDwQkPI6M4r3Yx9kM2J8MaSmZF2JfHVwHQ"

    /**
     * Verilen API anahtarının geçerli olup olmadığını test eder.
     */
    suspend fun validateApiKey(apiKey: String): Result<Unit> = withContext(Dispatchers.IO) {
        return@withContext try {
            GenerativeModel(modelName = "gemini-1.5-flash-latest", apiKey = apiKey)
                .countTokens("test")
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * Verilen metin ve AI aksiyonuna göre Gemini API'sine istek gönderir.
     */
    suspend fun processAiAction(text: String, action: AiAction, tone: AiTone? = null): String? {
        if (action == AiAction.CHANGE_TONE) {
            require(tone != null) { "CHANGE_TONE eylemi için bir ton belirtilmelidir." }
        }

        val currentSettings = settingsRepository.settings.first()
        val apiKeyToUse = if (currentSettings.useAirNoteApi) {
            airNoteApiKey
        } else {
            secureStorageRepository.getUserApiKey()
        }

        if (apiKeyToUse.isNullOrBlank()) {
            return "Kullanıcı API anahtarı bulunamadı. Lütfen ayarlardan kontrol edin."
        }

        val generativeModel = GenerativeModel(
            modelName = currentSettings.selectedModelName,
            apiKey = apiKeyToUse,
            generationConfig = generationConfig {
                temperature = 0.7f
            }
        )

        val prompt = when (action) {
            AiAction.IMPROVE_WRITING -> "Aşağıdaki metindeki dilbilgisi ve anlatım hatalarını düzelterek daha akıcı hale getir. Sadece düzeltilmiş metni cevap olarak ver:\n\n---\n\n$text"
            AiAction.SUMMARIZE -> "Aşağıdaki metni ana fikrini koruyarak bir veya iki cümleyle özetle. Sadece özet metnini cevap olarak ver:\n\n---\n\n$text"
            AiAction.MAKE_SHORTER -> "Aşağıdaki metni anlamını koruyarak önemli ölçüde kısalt. Sadece kısaltılmış metni cevap olarak ver:\n\n---\n\n$text"
            AiAction.MAKE_LONGER -> "Aşağıdaki metne yaratıcı detaylar ekleyerek daha uzun bir hale getir. Sadece uzatılmış metni cevap olarak ver:\n\n---\n\n$text"
            AiAction.CHANGE_TONE -> "${tone!!.promptInstruction}\n\n---\n\n$text" // tone'un null olmayacağından emin olduğumuz için !! kullanabiliriz.
        }

        return try {
            val response = generativeModel.generateContent(prompt)
            response.text
        } catch (e: Exception) {
            e.printStackTrace()
            "API isteği başarısız oldu: ${e.message}"
        }
    }

    suspend fun processAssistantAction(
        noteName: String,
        noteDescription: String,
        action: AiAssistantAction
    ): Flow<String> {
        // API Anahtarı yönetimi (processAiAction'dan kopyalanabilir)
        val currentSettings = settingsRepository.settings.first()
        val apiKeyToUse = if (currentSettings.useAirNoteApi) {
            airNoteApiKey
        } else {
            secureStorageRepository.getUserApiKey()
        }

        if (apiKeyToUse.isNullOrBlank()) {
            // Hata durumunda flow ile tek bir hata mesajı emit ediyoruz.
            return flowOf("Kullanıcı API anahtarı bulunamadı. Lütfen ayarlardan kontrol edin.")
        }

        val generativeModel = GenerativeModel(
            modelName = currentSettings.selectedModelName,
            apiKey = apiKeyToUse,
            generationConfig = generationConfig {
                temperature = 0.8f // İçerik üretiminde biraz daha yaratıcı olması için sıcaklığı artırabiliriz
            }
        )

        // Eyleme göre özel prompt oluşturma
        val prompt = when (action) {
            AiAssistantAction.GIVE_IDEA ->
                "Bir not alma uygulaması kullanıcısı, \"$noteName\" başlıklı bir not üzerinde çalışıyor. Bu başlığa uygun, yaratıcı ve ilham verici birkaç fikir veya başlangıç noktası öner. Cevabın kısa ve liste formatında olsun."

            AiAssistantAction.CONTINUE_WRITING ->
                "Aşağıdaki metni, anlam bütünlüğünü ve yazarın tarzını koruyarak devam ettir. Sadece eklenecek yeni metni cevap olarak ver, mevcut metni tekrarlama:\n\n---\n\nBaşlık: $noteName\n\nİçerik:\n$noteDescription"

            AiAssistantAction.CHANGE_PERSPECTIVE ->
                "Aşağıdaki metni analiz et ve bu konuya tamamen farklı bir bakış açısıyla nasıl yaklaşılabileceğine dair bir paragraf yaz. Mevcut metni özetleme, ona alternatif bir perspektif sun:\n\n---\n\nBaşlık: $noteName\n\nİçerik:\n$noteDescription"
            AiAssistantAction.PROS_AND_CONS ->
                "Aşağıdaki metni analiz et ve bir '### Artılar' ve bir '### Eksiler' başlığı altında, konuyla ilgili olumlu ve olumsuz noktaları madde madde listele. Sadece bu iki başlığı ve maddeleri cevap olarak ver:\n\n---\n\n$noteDescription"

            AiAssistantAction.CREATE_TODO_LIST ->
                "Aşağıdaki metni analiz et. Eğer metin gün içinde yapılacak görevlerden bahsediyorsa, bu görevleri her satırın başına '[] ' koyarak bir yapılacaklar listesine dönüştür. Eğer metin görevlerden bahsetmiyorsa, başka hiçbir şey yazmadan SADECE 'NO_TASKS' kelimesini cevap olarak ver. Metnin dilini algıla ve listeyi o dilde oluştur:\n\n---\n\n$noteDescription"

            AiAssistantAction.SIMPLIFY ->
                "Aşağıdaki karmaşık metni, herkesin anlayabileceği basit ve akıcı bir dile çevir. Metnin ana fikrini ve önemli detaylarını koru. Sadece basitleştirilmiş metni cevap olarak ver:\n\n---\n\n$noteDescription"

            AiAssistantAction.SUGGEST_A_TITLE ->
                "Aşağıdaki metnin içeriğine en uygun, yaratıcı ve dikkat çekici 5 adet başlık önerisi sun. Cevabını numaralandırılmış bir liste olarak ver:\n\n---\n\n$noteDescription"
        }

        return generativeModel
            .generateContentStream(prompt)
            .map { response ->
                // Gelen her parçanın metnini alıyoruz.
                response.text ?: ""
            }.catch {
                // Akış sırasında bir hata olursa yakalayıp mesaj emit ediyoruz.
                emit("API isteği başarısız oldu: ${it.message}")
            }
    }
}