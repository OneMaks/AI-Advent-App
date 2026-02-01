package ru.makscorp.project.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import ru.makscorp.project.data.api.ChatApiClient
import ru.makscorp.project.data.api.dto.ChatMessageDto
import ru.makscorp.project.data.storage.ContextStorage
import ru.makscorp.project.domain.model.ConversationContext
import ru.makscorp.project.domain.model.ContextSummary
import ru.makscorp.project.domain.model.Message
import ru.makscorp.project.domain.model.MessageRole
import ru.makscorp.project.domain.model.MessageStatus
import ru.makscorp.project.domain.model.SendMessageResult
import ru.makscorp.project.domain.model.TokenUsage
import ru.makscorp.project.domain.repository.ChatRepository
import ru.makscorp.project.domain.repository.SettingsRepository
import ru.makscorp.project.domain.service.ContextCompressionService
import ru.makscorp.project.util.TokenEstimator
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ChatRepositoryImpl(
    private val apiClient: ChatApiClient,
    private val compressionService: ContextCompressionService,
    private val settingsRepository: SettingsRepository,
    private val contextStorage: ContextStorage
) : ChatRepository {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    private val _summaries = MutableStateFlow<List<ContextSummary>>(emptyList())

    // Последние сообщения (полный контекст)
    private val recentMessages = mutableListOf<ChatMessageDto>()

    init {
        // Восстанавливаем контекст из хранилища
        contextStorage.loadContext()?.let { savedContext ->
            _summaries.value = savedContext.summaries
        }
    }

    override fun getMessages(): Flow<List<Message>> = _messages.asStateFlow()

    override fun getSummaries(): Flow<List<ContextSummary>> = _summaries.asStateFlow()

    override suspend fun sendMessage(userMessage: String): Result<SendMessageResult> {
        // Добавляем сообщение пользователя
        recentMessages.add(
            ChatMessageDto(role = "user", content = userMessage)
        )

        val settings = settingsRepository.getSettings()

        // Проверяем необходимость сжатия и получаем количество сжатых сообщений
        var compressedCount = 0
        if (compressionService.shouldCompress(recentMessages.size, settings)) {
            compressedCount = performCompression(settings.recentMessagesCount)
        }

        // Подготавливаем контекст для API
        val apiMessages = compressionService.prepareContextForApi(_summaries.value, recentMessages)

        // Отправляем запрос
        val result = apiClient.sendMessage(apiMessages)

        return result.fold(
            onSuccess = { response ->
                val assistantContent = response.choices.firstOrNull()?.message?.content
                    ?: "No response received"

                // Добавляем ответ ассистента в историю
                recentMessages.add(
                    ChatMessageDto(role = "assistant", content = assistantContent)
                )

                // Сохраняем контекст
                saveContext()

                // Вычисляем токены
                val estimatedPromptTokens = recentMessages
                    .dropLast(1)
                    .sumOf { TokenEstimator.estimateTokens(it.content) } +
                    _summaries.value.sumOf { it.estimatedTokens }
                val estimatedCompletionTokens = TokenEstimator.estimateTokens(assistantContent)

                val tokenUsage = TokenUsage(
                    estimatedPromptTokens = estimatedPromptTokens,
                    estimatedCompletionTokens = estimatedCompletionTokens,
                    actualPromptTokens = response.usage?.promptTokens,
                    actualCompletionTokens = response.usage?.completionTokens,
                    actualTotalTokens = response.usage?.totalTokens
                )

                val assistantMessage = Message(
                    id = Uuid.random().toString(),
                    content = assistantContent,
                    role = MessageRole.ASSISTANT,
                    timestamp = Clock.System.now(),
                    status = MessageStatus.SENT,
                    tokenUsage = tokenUsage
                )

                Result.success(SendMessageResult(
                    message = assistantMessage,
                    compressedMessagesCount = compressedCount
                ))
            },
            onFailure = { error ->
                // Удаляем неудачное сообщение из истории
                recentMessages.removeLastOrNull()
                Result.failure(error)
            }
        )
    }

    /**
     * Выполняет сжатие старых сообщений
     * @return количество сжатых сообщений (0 если сжатие не произошло)
     */
    private suspend fun performCompression(recentMessagesCount: Int): Int {
        val messagesToCompress = recentMessages.size - recentMessagesCount
        if (messagesToCompress <= 0) return 0

        val oldMessages = recentMessages.take(messagesToCompress)

        var compressedCount = 0
        compressionService.summarizeMessages(oldMessages)
            .onSuccess { summary ->
                _summaries.value = _summaries.value + summary
                // Удаляем сжатые сообщения из recent
                repeat(messagesToCompress) {
                    recentMessages.removeFirstOrNull()
                }
                compressedCount = messagesToCompress
                saveContext()
            }
            .onFailure {
                // При ошибке продолжаем без сжатия
            }

        return compressedCount
    }

    /**
     * Сохраняет контекст в хранилище
     */
    private fun saveContext() {
        val context = ConversationContext(summaries = _summaries.value)
        contextStorage.saveContext(context)
    }

    override fun clearMessages() {
        recentMessages.clear()
        _summaries.value = emptyList()
        contextStorage.clearContext()
        _messages.value = emptyList()
    }
}
