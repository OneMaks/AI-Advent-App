package ru.makscorp.project.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.makscorp.project.domain.model.ContextSummary
import ru.makscorp.project.domain.model.Message
import ru.makscorp.project.domain.model.SendMessageResult

interface ChatRepository {
    suspend fun sendMessage(userMessage: String): Result<SendMessageResult>
    fun getMessages(): Flow<List<Message>>
    fun getSummaries(): Flow<List<ContextSummary>>
    fun clearMessages()
}
