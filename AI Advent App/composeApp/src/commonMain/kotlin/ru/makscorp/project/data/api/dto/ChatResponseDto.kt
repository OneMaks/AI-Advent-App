package ru.makscorp.project.data.api.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ChatResponseDto(
    val id: String? = null,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<ChoiceDto>,
    val usage: UsageDto? = null
)

@Serializable
data class ChoiceDto(
    val index: Int,
    val message: ChatMessageDto,
    @SerialName("finish_reason")
    val finishReason: String? = null
)

@Serializable
data class UsageDto(
    @SerialName("prompt_tokens")
    val promptTokens: Int,
    @SerialName("completion_tokens")
    val completionTokens: Int,
    @SerialName("total_tokens")
    val totalTokens: Int
)

@Serializable
data class ApiErrorDto(
    val error: ErrorDetailDto
)

@Serializable
data class ErrorDetailDto(
    val message: String,
    val type: String? = null,
    val code: String? = null
)
