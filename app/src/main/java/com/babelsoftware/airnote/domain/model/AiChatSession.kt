package com.babelsoftware.airnote.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "ai_chat_sessions")
data class AiChatSession(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val createdAt: Date = Date(),
    val aiMode: String
)
