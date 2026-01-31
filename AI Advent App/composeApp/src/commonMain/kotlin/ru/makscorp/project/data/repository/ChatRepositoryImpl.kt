package ru.makscorp.project.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import ru.makscorp.project.data.api.ChatApiClient
import ru.makscorp.project.data.api.dto.ChatMessageDto
import ru.makscorp.project.domain.model.Message
import ru.makscorp.project.domain.model.MessageRole
import ru.makscorp.project.domain.model.MessageStatus
import ru.makscorp.project.domain.model.TokenUsage
import ru.makscorp.project.domain.repository.ChatRepository
import ru.makscorp.project.util.TokenEstimator
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ChatRepositoryImpl(
    private val apiClient: ChatApiClient
) : ChatRepository {

    private val _messages = MutableStateFlow<List<Message>>(emptyList())
    private val conversationHistory = mutableListOf<ChatMessageDto>()

    override fun getMessages(): Flow<List<Message>> = _messages.asStateFlow()

    override suspend fun sendMessage(userMessage: String): Result<Message> {
        // Add user message to conversation history
        conversationHistory.add(
            ChatMessageDto(role = "user", content = userMessage)
        )

        // Call API with full conversation history
        val result = apiClient.sendMessage(conversationHistory)

        return result.fold(
            onSuccess = { response ->
                val assistantContent = response.choices.firstOrNull()?.message?.content
                    ?: "No response received"

                // Add assistant response to conversation history
                conversationHistory.add(
                    ChatMessageDto(role = "assistant", content = assistantContent)
                )

                // Calculate estimated tokens
                val estimatedPromptTokens = conversationHistory
                    .dropLast(1) // Exclude assistant response
                    .sumOf { TokenEstimator.estimateTokens(it.content) }
                val estimatedCompletionTokens = TokenEstimator.estimateTokens(assistantContent)

                // Get actual tokens from API response
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

                Result.success(assistantMessage)
            },
            onFailure = { error ->
                // Remove failed user message from history
                conversationHistory.removeLastOrNull()
                Result.failure(error)
            }
        )
    }

    override fun clearMessages() {
        conversationHistory.clear()
        _messages.value = emptyList()
    }
}
