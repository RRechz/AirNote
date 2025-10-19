package com.babelsoftware.airnote.domain.usecase

import com.babelsoftware.airnote.data.local.dao.AiChatDao
import com.babelsoftware.airnote.data.repository.AiMode
import com.babelsoftware.airnote.domain.model.AiChatMessage
import com.babelsoftware.airnote.domain.model.AiChatSession
import com.babelsoftware.airnote.domain.model.ChatMessage
import com.babelsoftware.airnote.domain.model.Participant
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AiChatUseCase @Inject constructor(
    private val aiChatDao: AiChatDao
) {

    fun getAllSessions(): Flow<List<AiChatSession>> {
        return aiChatDao.getAllSessions()
    }

    fun getMessagesForSession(sessionId: Long): Flow<List<AiChatMessage>> {
        return aiChatDao.getMessagesForSession(sessionId)
    }

    suspend fun startNewSession(title: String, aiMode: AiMode): Long {
        val session = AiChatSession(title = title, aiMode = aiMode.name)
        return aiChatDao.insertSession(session)
    }

    suspend fun addMessageToSession(sessionId: Long, message: ChatMessage): Long {
        val aiChatMessage = AiChatMessage(
            sessionId = sessionId,
            text = message.text,
            participant = message.participant,
            isLoading = message.isLoading
        )
        return aiChatDao.insertMessage(aiChatMessage)
    }

    suspend fun updateMessageById(id: Long, text: String, isLoading: Boolean) {
        aiChatDao.updateMessageById(id, text, isLoading)
    }

    suspend fun getSessionById(sessionId: Long): AiChatSession? {
        return aiChatDao.getSessionById(sessionId)
    }

    suspend fun updateSessionMode(sessionId: Long, aiMode: AiMode) {
        aiChatDao.updateSessionMode(sessionId, aiMode.name)
    }

    suspend fun deleteSessionById(sessionId: Long) {
        aiChatDao.deleteSessionById(sessionId)
    }
}
