package com.babelsoftware.airnote.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.babelsoftware.airnote.domain.model.AiChatMessage
import com.babelsoftware.airnote.domain.model.AiChatSession
import com.babelsoftware.airnote.domain.model.Participant
import kotlinx.coroutines.flow.Flow

@Dao
interface AiChatDao {
    // --- Session Operations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: AiChatSession): Long

    @Query("SELECT * FROM ai_chat_sessions ORDER BY createdAt DESC")
    fun getAllSessions(): Flow<List<AiChatSession>>

    @Query("SELECT * FROM ai_chat_sessions WHERE id = :sessionId")
    suspend fun getSessionById(sessionId: Long): AiChatSession?

    @Query("UPDATE ai_chat_sessions SET aiMode = :aiMode WHERE id = :sessionId")
    suspend fun updateSessionMode(sessionId: Long, aiMode: String)

    @Query("DELETE FROM ai_chat_sessions WHERE id = :sessionId")
    suspend fun deleteSessionById(sessionId: Long)

    // --- Message Operations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: AiChatMessage): Long

    @Query("SELECT * FROM ai_chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: Long): Flow<List<AiChatMessage>>

    @Query("UPDATE ai_chat_messages SET text = :text, isLoading = :isLoading, participant = :participant WHERE id = :id")
    suspend fun updateMessageById(
        id: Long,
        text: String,
        isLoading: Boolean,
        participant: Participant
    )
}
