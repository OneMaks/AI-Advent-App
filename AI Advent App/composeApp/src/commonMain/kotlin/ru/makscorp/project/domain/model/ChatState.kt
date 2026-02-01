package ru.makscorp.project.domain.model

data class ChatState(
    val messages: List<Message> = emptyList(),
    val summaries: List<ContextSummary> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val inputText: String = ""
)
