package ru.makscorp.project.data.storage

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock
import ru.makscorp.project.data.db.AppDatabase
import ru.makscorp.project.data.db.MessageEntity
import ru.makscorp.project.data.db.SummaryEntity
import ru.makscorp.project.domain.model.ContextSummary
import ru.makscorp.project.domain.model.Message

actual class ChatDatabase(context: Context) {
    private val database = AppDatabase.getInstance(context)
    private val messageDao = database.messageDao()
    private val summaryDao = database.summaryDao()

    actual fun getAllMessages(): Flow<List<Message>> {
        return messageDao.getAllMessages().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    actual fun getUncompressedMessages(): Flow<List<Message>> {
        return messageDao.getUncompressedMessages().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    actual fun getSummaries(): Flow<List<ContextSummary>> {
        return summaryDao.getAllSummaries().map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    actual suspend fun getAllMessagesList(): List<Message> {
        return messageDao.getAllMessagesList().map { it.toDomainModel() }
    }

    actual suspend fun getUncompressedMessagesList(): List<Message> {
        return messageDao.getUncompressedMessagesList().map { it.toDomainModel() }
    }

    actual suspend fun getSummariesList(): List<ContextSummary> {
        return summaryDao.getAllSummariesList().map { it.toDomainModel() }
    }

    actual suspend fun insertMessage(message: Message, isCompressed: Boolean, summaryId: String?) {
        val entity = MessageEntity.fromDomainModel(message, isCompressed, summaryId)
        messageDao.insertMessage(entity)
    }

    actual suspend fun insertSummary(summary: ContextSummary) {
        val entity = SummaryEntity.fromDomainModel(summary, Clock.System.now().toEpochMilliseconds())
        summaryDao.insertSummary(entity)
    }

    actual suspend fun markMessagesAsCompressed(messageIds: List<String>, summaryId: String) {
        messageDao.markMessagesAsCompressed(messageIds, summaryId)
    }

    actual suspend fun deleteAllMessages() {
        messageDao.deleteAllMessages()
    }

    actual suspend fun deleteAllSummaries() {
        summaryDao.deleteAllSummaries()
    }

    actual suspend fun clearAll() {
        deleteAllMessages()
        deleteAllSummaries()
    }

    actual suspend fun getUncompressedMessagesCount(): Int {
        return messageDao.getUncompressedMessagesCount()
    }
}
