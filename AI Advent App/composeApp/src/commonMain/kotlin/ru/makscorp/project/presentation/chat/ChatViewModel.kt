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
import ru.makscorp.project.domain.model.ChatSettings
import ru.makscorp.project.domain.model.ChatState
import ru.makscorp.project.domain.model.Message
import ru.makscorp.project.domain.model.MessageRole
import ru.makscorp.project.domain.repository.ChatRepository
import ru.makscorp.project.domain.repository.SettingsRepository

class ChatViewModel(
    private val chatRepository: ChatRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(ChatState())
    val state: StateFlow<ChatState> = _state.asStateFlow()

    val settings: StateFlow<ChatSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, settingsRepository.getSettings())

    init {
        // Подписка на изменения сообщений из БД
        viewModelScope.launch {
            chatRepository.getMessages().collect { messages ->
                _state.update { it.copy(messages = messages) }
            }
        }
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

        _state.update { state ->
            state.copy(
                inputText = "",
                isLoading = true,
                error = null
            )
        }

        viewModelScope.launch {
            chatRepository.sendMessage(currentInput)
                .onSuccess {
                    _state.update { state ->
                        state.copy(isLoading = false)
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
