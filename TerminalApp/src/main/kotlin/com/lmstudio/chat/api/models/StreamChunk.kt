package com.lmstudio.chat.api.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a streaming chunk from the chat completions API.
 */
@Serializable
data class StreamChunk(
    val id: String,
    val `object`: String,
    val created: Long,
    val model: String,
    val choices: List<StreamChoice>
)

@Serializable
data class StreamChoice(
    val index: Int,
    val delta: Delta,
    @SerialName("finish_reason")
    val finishReason: String? = null
)
