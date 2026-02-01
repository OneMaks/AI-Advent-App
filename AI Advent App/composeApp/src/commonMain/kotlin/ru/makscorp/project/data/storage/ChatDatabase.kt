package ru.makscorp.project.data.storage

import kotlinx.coroutines.flow.Flow
import ru.makscorp.project.domain.model.ContextSummary
import ru.makscorp.project.domain.model.Message

expect class ChatDatabase {
    fun getAllMessages(): Flow<List<Message>>
    fun getUncompressedMessages(): Flow<List<Message>>
    fun getSummaries(): Flow<List<ContextSummary>>

    suspend fun getAllMessagesList(): List<Message>
    suspend fun getUncompressedMessagesList(): List<Message>
    suspend fun getSummariesList(): List<ContextSummary>

    suspend fun insertMessage(message: Message, isCompressed: Boolean = false, summaryId: String? = null)
    suspend fun insertSummary(summary: ContextSummary)
    suspend fun markMessagesAsCompressed(messageIds: List<String>, summaryId: String)

    suspend fun deleteAllMessages()
    suspend fun deleteAllSummaries()
    suspend fun clearAll()

    suspend fun getUncompressedMessagesCount(): Int
}
