package com.babelsoftware.airnote.domain.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "ai_chat_messages",
    foreignKeys = [
        ForeignKey(
            entity = AiChatSession::class,
            parentColumns = ["id"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class AiChatMessage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sessionId: Long,
    val text: String,
    val participant: Participant,
    val timestamp: Date = Date(),
    val isLoading: Boolean = false
)
