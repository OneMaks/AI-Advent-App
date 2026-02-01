package ru.makscorp.project.domain.model

/**
 * Результат отправки сообщения с информацией о сжатии контекста
 */
data class SendMessageResult(
    val message: Message,
    val compressedMessagesCount: Int = 0
)
