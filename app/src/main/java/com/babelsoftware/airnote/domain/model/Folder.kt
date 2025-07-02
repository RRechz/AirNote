/*
 * Copyright (c) 2025 Babel Software.
 */

package com.babelsoftware.airnote.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Folder(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val color: String, // Rengi hex kodu olarak saklayabiliriz, ör: "#FF5733"
    val createdAt: Long = System.currentTimeMillis()
)