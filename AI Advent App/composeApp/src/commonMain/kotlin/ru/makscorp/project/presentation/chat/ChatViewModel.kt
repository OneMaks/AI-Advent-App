package ru.makscorp.project.presentation.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import ru.makscorp.project.domain.model.ChatSettings
import ru.makscorp.project.domain.model.ChatState
import ru.makscorp.project.domain.model.Message
import ru.makscorp.project.domain.model.MessageRole
import ru.makscorp.project.domain.model.MessageStatus
import ru.makscorp.project.domain.repository.ChatRepository
import ru.makscorp.project.domain.repository.SettingsRepository
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    val settings: StateFlow<ChatSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, settingsRepository.getSettings())

    init {
        // Подписка на изменения summaries
        viewModelScope.launch {
            chatRepository.getSummaries().collect { summaries ->
                _state.update { it.copy(summaries = summaries) }
            }
        }
    }

    fun onInputChange(text: String) {
        _state.update { it.copy(inputText = text) }
    }

    fun onSendMessage() {
        val currentInput = _state.value.inputText.trim()
        if (currentInput.isBlank()) return

        val userMessage = Message(
            id = Uuid.random().toString(),
            content = currentInput,
            role = MessageRole.USER,
            timestamp = Clock.System.now(),
            status = MessageStatus.SENT
        )

        _state.update { state ->
            state.copy(
                messages = state.messages + userMessage,
                inputText = "",
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            chatRepository.sendMessage(currentInput)
                .onSuccess { result ->
                    _state.update { state ->
                        // Если были сжатые сообщения, удаляем их из UI
                        val updatedMessages = if (result.compressedMessagesCount > 0) {
                            state.messages.drop(result.compressedMessagesCount)
                        } else {
                            state.messages
                        }

                        state.copy(
                            messages = updatedMessages + result.message,
                            isLoading = false
                        )
                    }
                }
                .onFailure { error ->
                    _state.update { state ->
                        state.copy(
                            isLoading = false,
                            error = error.message ?: "Failed to get response"
                        )
                    }
                }
        }
    }

    fun onRetry(messageId: String) {
        // Find the failed message and retry
        val failedMessage = _state.value.messages.find { it.id == messageId }
        if (failedMessage != null && failedMessage.role == MessageRole.USER) {
            _state.update { state ->
                state.copy(
                    messages = state.messages.filter { it.id != messageId },
                    inputText = failedMessage.content
                )
            }
            onSendMessage()
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    fun updateSettings(newSettings: ChatSettings) {
        settingsRepository.updateSettings(newSettings)
    }

    fun clearChat() {
        chatRepository.clearMessages()
        _state.update { it.copy(messages = emptyList()) }
    }
}
