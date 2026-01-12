package com.babelsoftware.loudly.lyrics

import android.content.Context
import com.babelsoftware.loudly.constants.EnableLrcLibKey
import com.babelsoftware.loudly.utils.dataStore
import com.babelsoftware.loudly.utils.get
import com.babelsoftware.lrclib.LrcLib

object LrcLibLyricsProvider : LyricsProvider {
    override val name = "LrcLib"

    override fun isEnabled(context: Context): Boolean =
        context.dataStore[EnableLrcLibKey] ?: true

    override suspend fun getLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
    ): Result<String> = LrcLib.getLyrics(title, artist, duration)

    override suspend fun getAllLyrics(
        id: String,
        title: String,
        artist: String,
        duration: Int,
        callback: (String) -> Unit,
    ) {
        LrcLib.getAllLyrics(title, artist, duration, null, callback)
    }
}
