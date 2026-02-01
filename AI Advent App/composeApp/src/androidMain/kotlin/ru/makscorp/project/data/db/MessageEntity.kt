package ru.makscorp.project.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.makscorp.project.domain.model.Message
import ru.makscorp.project.domain.model.MessageRole
import ru.makscorp.project.domain.model.MessageStatus
import ru.makscorp.project.domain.model.TokenUsage
import kotlinx.datetime.Instant

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey
    val id: String,
    val content: String,
    val role: String,
    val timestamp: Long,
    val status: String,
    val isCompressed: Boolean = false,
    val summaryId: String? = null,
    // Token usage fields
    val estimatedPromptTokens: Int? = null,
    val estimatedCompletionTokens: Int? = null,
    val actualPromptTokens: Int? = null,
    val actualCompletionTokens: Int? = null,
    val actualTotalTokens: Int? = null
) {
    fun toDomainModel(): Message = Message(
        id = id,
        content = content,
        role = MessageRole.valueOf(role),
        timestamp = Instant.fromEpochMilliseconds(timestamp),
        status = MessageStatus.valueOf(status),
        tokenUsage = if (estimatedPromptTokens != null || actualPromptTokens != null) {
            TokenUsage(
                estimatedPromptTokens = estimatedPromptTokens ?: 0,
                estimatedCompletionTokens = estimatedCompletionTokens ?: 0,
                actualPromptTokens = actualPromptTokens,
                actualCompletionTokens = actualCompletionTokens,
                actualTotalTokens = actualTotalTokens
            )
        } else null
    )

    companion object {
        fun fromDomainModel(
            message: Message,
            isCompressed: Boolean = false,
            summaryId: String? = null
        ): MessageEntity = MessageEntity(
            id = message.id,
            content = message.content,
            role = message.role.name,
            timestamp = message.timestamp.toEpochMilliseconds(),
            status = message.status.name,
            isCompressed = isCompressed,
            summaryId = summaryId,
            estimatedPromptTokens = message.tokenUsage?.estimatedPromptTokens,
            estimatedCompletionTokens = message.tokenUsage?.estimatedCompletionTokens,
            actualPromptTokens = message.tokenUsage?.actualPromptTokens,
            actualCompletionTokens = message.tokenUsage?.actualCompletionTokens,
            actualTotalTokens = message.tokenUsage?.actualTotalTokens
        )
    }
}
