package ru.makscorp.project.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.makscorp.project.domain.model.Message

interface ChatRepository {
    suspend fun sendMessage(userMessage: String): Result<Message>
    fun getMessages(): Flow<List<Message>>
}
