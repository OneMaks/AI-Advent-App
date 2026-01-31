package com.lmstudio.chat.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Request body for the chat completions API.
 */
@Serializable
data class ChatRequest(
    val model: String,
    val messages: List<Message>,
    val temperature: Double = 0.7,
    @SerialName("max_tokens")
    val maxTokens: Int = 2000,
    @SerialName("top_p")
    val topP: Double = 1.0,
    @SerialName("frequency_penalty")
    val frequencyPenalty: Double = 0.0,
    @SerialName("presence_penalty")
    val presencePenalty: Double = 0.0,
    val stream: Boolean = false
)
