/*
 * Copyright (c) 2025 Babel Software.
 *
 * This file and codes created by RRechz - Babel Software
 */

package com.babelsoftware.airnote.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

// ---> A better Changelog demonstrator
sealed class ChangelogResult {
    data class Success(val body: String) : ChangelogResult()
    data class Error(val exception: Exception) : ChangelogResult()
}
// <---

// Data class that holds version information (changelog, download url, checksum) together
data class ReleaseInfo(
    val changelog: String,
    val apkDownloadUrl: String?,
    val sha256Checksum: String?
)

/**
 * Pulls the latest version tag (tag_name) from the GitHub API.
 * @return Latest version tag or null in case of error.
 */
suspend fun checkForUpdates(): String? = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://api.github.com/repos/RRechz/AirNote/releases/latest")
        val connection = url.openConnection()
        connection.connect()
        val json = connection.getInputStream().bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(json)
        return@withContext jsonObject.getString("tag_name")
    } catch (e: Exception) {
        e.printStackTrace()
        return@withContext null
    }
}

/**
 * Checks if the remote version is newer than the current version.
 * It removes the “v” prefix and suffixes such as “-default”, “-debug” from the version names for comparison.
 * @param remoteVersion The version from the remote server (e.g. “v1.2”).
 * @param currentVersion The version installed on the device (e.g. “v1.1-default”).
 * @return True if the remote version is newer, false otherwise.
 */
fun isNewerVersion(remoteVersion: String, currentVersion: String): Boolean {
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

suspend fun getLatestReleaseInfo(): ReleaseInfo? = withContext(Dispatchers.IO) {
    try {
        val url = URL("https://api.github.com/repos/RRechz/AirNote/releases/latest")
        val connection = url.openConnection()
        connection.connect()
        val json = connection.getInputStream().bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(json)

        val changelog = jsonObject.optString("body", "Değişiklik günlüğü alınamadı.")
        val checksum = changelog.lines().find { it.startsWith("SHA-256:") }?.substringAfter(":")?.trim() // Find and extract the checksum from changelog

        // ---> Find APK download link
        val assets = jsonObject.getJSONArray("assets")
        var apkUrl: String? = null
        if (assets.length() > 0) {
            val asset = assets.getJSONObject(0) // Genellikle ilk asset APK olur.
            apkUrl = asset.optString("browser_download_url")
        }
        // <---

        ReleaseInfo(changelog, apkUrl, checksum)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

/**
 * Pulls the changelog of the latest version from the GitHub API.
 * @return Returns Success(body) if successful or Error(exception) if failed.
 */
suspend fun getChangelogFromGitHub(): ChangelogResult {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("https://api.github.com/repos/RRechz/AirNote/releases/latest")
            val connection = url.openConnection()
            connection.connect()
            val json = connection.getInputStream().bufferedReader().use { it.readText() }
            val jsonObject = JSONObject(json)
            val body = jsonObject.optString("body", "")
            ChangelogResult.Success(body)
        } catch (e: Exception) {
            ChangelogResult.Error(e)
        }
    }
}