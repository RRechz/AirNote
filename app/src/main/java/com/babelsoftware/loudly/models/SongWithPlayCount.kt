package com.babelsoftware.loudly.models

import androidx.room.Embedded
import com.babelsoftware.loudly.db.entities.Song

data class SongWithPlayCount(
    @Embedded
    val song: Song,
    val playCount: Int
)