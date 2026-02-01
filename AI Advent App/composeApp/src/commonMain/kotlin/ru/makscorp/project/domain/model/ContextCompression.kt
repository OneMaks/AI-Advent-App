package ru.makscorp.project.domain.model

import kotlinx.serialization.Serializable

/**
 * Резюме сжатого блока сообщений
 */
@Serializable
data class ContextSummary(
    val id: String,
    val content: String,
    val originalMessageCount: Int,
    val estimatedTokens: Int
)

/**
 * Контекст разговора с историей сжатий
 */
@Serializable
data class ConversationContext(
    val summaries: List<ContextSummary> = emptyList()
)

/**
 * Статистика текущего контекста
 */
data class ContextStats(
    val recentMessagesCount: Int,
    val summaryBlocksCount: Int,
    val totalOriginalMessages: Int,
    val estimatedTokensSaved: Int,
    val currentContextTokens: Int
)
