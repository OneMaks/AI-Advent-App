package ru.makscorp.project.data.service

import ru.makscorp.project.data.api.ChatApiClient
import ru.makscorp.project.data.api.dto.ChatMessageDto
import ru.makscorp.project.domain.model.ChatSettings
import ru.makscorp.project.domain.model.ContextStats
import ru.makscorp.project.domain.model.ContextSummary
import ru.makscorp.project.domain.service.ContextCompressionService
import ru.makscorp.project.util.TokenEstimator
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class ContextCompressionServiceImpl(
    private val apiClient: ChatApiClient
) : ContextCompressionService {

    override fun shouldCompress(
        messageCount: Int,
        settings: ChatSettings
    ): Boolean {
        return settings.contextCompressionEnabled &&
                messageCount > settings.compressionThreshold
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun summarizeMessages(
        messages: List<ChatMessageDto>
    ): Result<ContextSummary> {
        if (messages.isEmpty()) {
            return Result.failure(IllegalArgumentException("No messages to summarize"))
        }

        // Формируем текст диалога для суммаризации
        val conversationText = messages.joinToString("\n") { msg ->
            val role = when (msg.role) {
                "user" -> "Пользователь"
                "assistant" -> "Ассистент"
                else -> msg.role
            }
            "$role: ${msg.content}"
        }

        val summarizationRequest = listOf(
            ChatMessageDto(
                role = "system",
                content = SUMMARIZATION_SYSTEM_PROMPT
            ),
            ChatMessageDto(
                role = "user",
                content = "Диалог для суммаризации:\n\n$conversationText"
            )
        )

        return apiClient.sendMessageForSummary(summarizationRequest).map { response ->
            val summaryContent = response.choices.firstOrNull()?.message?.content
                ?: "Резюме недоступно"

            ContextSummary(
                id = Uuid.random().toString(),
                content = summaryContent,
                originalMessageCount = messages.size,
                estimatedTokens = TokenEstimator.estimateTokens(summaryContent)
            )
        }
    }

    override fun prepareContextForApi(
        summaries: List<ContextSummary>,
        recentMessages: List<ChatMessageDto>
    ): List<ChatMessageDto> {
        val contextMessages = mutableListOf<ChatMessageDto>()

        // Добавляем резюме как системное сообщение с контекстом
        if (summaries.isNotEmpty()) {
            val combinedSummary = summaries.joinToString("\n\n") { summary ->
                "[Резюме предыдущего диалога (${summary.originalMessageCount} сообщений)]:\n${summary.content}"
            }

            contextMessages.add(
                ChatMessageDto(
                    role = "system",
                    content = "Контекст предыдущего разговора:\n$combinedSummary"
                )
            )
        }

        // Добавляем полные последние сообщения
        contextMessages.addAll(recentMessages)

        return contextMessages
    }

    override fun getStats(
        summaries: List<ContextSummary>,
        recentMessages: List<ChatMessageDto>
    ): ContextStats {
        val totalOriginalMessages = summaries.sumOf { it.originalMessageCount } +
                recentMessages.size

        val summaryTokens = summaries.sumOf { it.estimatedTokens }
        val recentTokens = recentMessages.sumOf {
            TokenEstimator.estimateTokens(it.content)
        }

        // Оценка токенов, которые были бы без сжатия
        val estimatedOriginalTokens = summaries.sumOf { summary ->
            summary.originalMessageCount * 150 // Средняя оценка токенов на сообщение
        } + recentTokens

        return ContextStats(
            recentMessagesCount = recentMessages.size,
            summaryBlocksCount = summaries.size,
            totalOriginalMessages = totalOriginalMessages,
            estimatedTokensSaved = maxOf(0, estimatedOriginalTokens - summaryTokens - recentTokens),
            currentContextTokens = summaryTokens + recentTokens
        )
    }

    companion object {
        private const val SUMMARIZATION_SYSTEM_PROMPT = """Ты - ассистент для создания кратких резюме диалогов.

ЗАДАЧА: Создай краткое резюме предоставленного диалога.

ТРЕБОВАНИЯ:
1. Сохрани ключевые факты, решения и важный контекст
2. Используй 2-4 предложения (максимум 100 слов)
3. Пиши от третьего лица ("Пользователь спросил...", "Обсуждались...")
4. Сохрани техническую терминологию если она есть
5. Не добавляй информацию, которой не было в диалоге

ФОРМАТ: Только текст резюме, без заголовков и форматирования."""
    }
}
