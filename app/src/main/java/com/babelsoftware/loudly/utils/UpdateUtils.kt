// com/babelsoftware/loudly/utils/UpdateUtils.kt
package com.babelsoftware.loudly.utils

import com.babelsoftware.loudly.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

// Sürüm bilgilerini (sürüm notu, indirme linki vb.) bir arada tutan veri sınıfı
data class ReleaseInfo(
    val tagName: String,
    val changelog: String,
    val apkDownloadUrl: String?
)

/**
 * GitHub API'sinden en son sürüm bilgilerini çeker.
 * @return ReleaseInfo nesnesi veya hata durumunda null.
 */
suspend fun getLatestReleaseInfo(): ReleaseInfo? = withContext(Dispatchers.IO) {
    try {
        // DİKKAT: URL, Loudly projesinin GitHub reposunu işaret edecek şekilde güncellendi.
        val url = URL("https://api.github.com/repos/RRechz/Loudly/releases/latest")
        val json = url.readText()
        val jsonObject = JSONObject(json)

        val tagName = jsonObject.getString("tag_name")
        val changelog = jsonObject.optString("body", "Değişiklik günlüğü alınamadı.")

        // .apk dosyasının indirme linkini bul
        val assets = jsonObject.getJSONArray("assets")
        var apkUrl: String? = null
        for (i in 0 until assets.length()) {
            val asset = assets.getJSONObject(i)
            val downloadUrl = asset.optString("browser_download_url")
            if (downloadUrl.endsWith(".apk")) {
                apkUrl = downloadUrl
                break
            }
        }

        ReleaseInfo(tagName, changelog, apkUrl)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Uzak sunucudaki versiyonun mevcut versiyondan daha yeni olup olmadığını kontrol eder.
 * @param remoteVersion Uzak sunucudaki versiyon (örn: "v1.2").
 * @param currentVersion Cihazda yüklü olan versiyon (örn: "v1.1").
 * @return Uzak versiyon daha yeniyse true, aksi halde false.
 */
fun isNewerVersion(remoteVersion: String, currentVersion: String): Boolean {
    // AirNote'tan alınan versiyon karşılaştırma mantığı
    val remote = remoteVersion.removePrefix("v").split(".").map { it.toIntOrNull() ?: 0 }
    val current = currentVersion.removePrefix("v").split("-")[0].split(".").map { it.toIntOrNull() ?: 0 }

    for (i in 0 until maxOf(remote.size, current.size)) {
        val r = remote.getOrNull(i) ?: 0
        val c = current.getOrNull(i) ?: 0
        if (r > c) return true
        if (r < c) return false
    }
    return false
}