package ru.makscorp.project.domain.model

import kotlinx.datetime.Instant

data class Message(
    val id: String,
    val content: String,
    val role: MessageRole,
    val timestamp: Instant,
    val status: MessageStatus = MessageStatus.SENT,
    val tokenUsage: TokenUsage? = null
)

enum class MessageRole {
    USER,
    ASSISTANT
}

enum class MessageStatus {
    SENDING,
    SENT,
    ERROR
}
