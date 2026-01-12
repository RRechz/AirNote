package com.babelsoftware.loudly.lyrics

data class LyricsEntry(
    val time: Long,
    val text: String,
    var translatedText: String? = null,
    var isTranslation: Boolean = false
) : Comparable<LyricsEntry> {
    override fun compareTo(other: LyricsEntry): Int = (time - other.time).toInt()

    companion object {
        val HEAD_LYRICS_ENTRY = LyricsEntry(0L, "")
    }
}