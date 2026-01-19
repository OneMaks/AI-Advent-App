package ru.makscorp.project.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatRequestDto(
    val model: String,
    val messages: List<ChatMessageDto>,
    @SerialName("max_tokens")
    val maxTokens: Int? = null,
    val temperature: Double? = null
)

@Serializable
data class ChatMessageDto(
    val role: String,
    val content: String
)
