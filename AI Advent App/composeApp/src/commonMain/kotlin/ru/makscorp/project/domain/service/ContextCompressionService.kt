package ru.makscorp.project.domain.service

import ru.makscorp.project.data.api.dto.ChatMessageDto
import ru.makscorp.project.domain.model.ChatSettings
import ru.makscorp.project.domain.model.ContextStats
import ru.makscorp.project.domain.model.ContextSummary

interface ContextCompressionService {
    /**
     * Проверяет, нужно ли сжатие на основе текущих настроек
     */
    fun shouldCompress(
        messageCount: Int,
        settings: ChatSettings
    ): Boolean

    /**
     * Создает резюме из блока сообщений через API
     */
    suspend fun summarizeMessages(
        messages: List<ChatMessageDto>
    ): Result<ContextSummary>

    /**
     * Подготавливает контекст для отправки на API
     * Объединяет резюме + полные последние сообщения
     */
    fun prepareContextForApi(
        summaries: List<ContextSummary>,
        recentMessages: List<ChatMessageDto>
    ): List<ChatMessageDto>

    /**
     * Получает статистику текущего контекста
     */
    fun getStats(
        summaries: List<ContextSummary>,
        recentMessages: List<ChatMessageDto>
    ): ContextStats
}
