package ru.makscorp.project.data.repository

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ru.makscorp.project.data.api.ChatApiClient
import ru.makscorp.project.data.api.dto.ChatMessageDto
import ru.makscorp.project.data.storage.ChatDatabase
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
    private val chatDatabase: ChatDatabase
) : ChatRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    private val _summaries = MutableStateFlow<List<ContextSummary>>(emptyList())

    // Кэш несжатых сообщений для API контекста
    private val recentMessages = mutableListOf<ChatMessageDto>()

    init {
        // Загружаем данные из БД
        scope.launch {
            // Загружаем несжатые сообщения для отображения
            chatDatabase.getUncompressedMessages().collect { messages ->
                _messages.value = messages
                // Синхронизируем recentMessages с БД
                recentMessages.clear()
                recentMessages.addAll(messages.map { msg ->
                    ChatMessageDto(
                        role = if (msg.role == MessageRole.USER) "user" else "assistant",
                        content = msg.content
                    )
                })
            }
        }
        scope.launch {
            // Загружаем резюме
            chatDatabase.getSummaries().collect { summaries ->
                _summaries.value = summaries
            }
        }
    }

    override fun getMessages(): Flow<List<Message>> = _messages.asStateFlow()

    override fun getSummaries(): Flow<List<ContextSummary>> = _summaries.asStateFlow()

    override suspend fun sendMessage(userMessage: String): Result<SendMessageResult> {
        // Создаем сообщение пользователя
        val userMessageObj = Message(
            id = Uuid.random().toString(),
            content = userMessage,
            role = MessageRole.USER,
            timestamp = Clock.System.now(),
            status = MessageStatus.SENT
        )

        // Сохраняем в БД
        chatDatabase.insertMessage(userMessageObj)

        // Добавляем в кэш для API
        recentMessages.add(
            ChatMessageDto(role = "user", content = userMessage)
        )

        // Обновляем UI
        _messages.value = _messages.value + userMessageObj

        val settings = settingsRepository.getSettings()

        // Проверяем необходимость сжатия
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

                // Добавляем ответ ассистента в кэш
                recentMessages.add(
                    ChatMessageDto(role = "assistant", content = assistantContent)
                )

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

                // Сохраняем в БД
                chatDatabase.insertMessage(assistantMessage)

                // Обновляем UI
                _messages.value = _messages.value + assistantMessage

                Result.success(SendMessageResult(
                    message = assistantMessage,
                    compressedMessagesCount = compressedCount
                ))
            },
            onFailure = { error ->
                // Удаляем неудачное сообщение из кэша
                recentMessages.removeLastOrNull()
                // Помечаем сообщение пользователя как ошибку в UI
                _messages.value = _messages.value.dropLast(1) + userMessageObj.copy(status = MessageStatus.ERROR)
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

        // Получаем ID сообщений для пометки как сжатые
        val currentMessages = _messages.value
        val messageIdsToCompress = currentMessages.take(messagesToCompress).map { it.id }

        var compressedCount = 0
        compressionService.summarizeMessages(oldMessages)
            .onSuccess { summary ->
                // Сохраняем резюме в БД
                chatDatabase.insertSummary(summary)

                // Помечаем сообщения как сжатые в БД
                chatDatabase.markMessagesAsCompressed(messageIdsToCompress, summary.id)

                // Обновляем UI
                _summaries.value = _summaries.value + summary

                // Удаляем сжатые сообщения из кэша и UI
                repeat(messagesToCompress) {
                    recentMessages.removeFirstOrNull()
                }
                _messages.value = currentMessages.drop(messagesToCompress)

                compressedCount = messagesToCompress
            }
            .onFailure {
                // При ошибке продолжаем без сжатия
            }

        return compressedCount
    }

    override fun clearMessages() {
        recentMessages.clear()
        _summaries.value = emptyList()
        _messages.value = emptyList()

        // Очищаем БД
        scope.launch {
            chatDatabase.clearAll()
        }
    }
}
