package com.lmstudio.chat.api.models

import kotlinx.serialization.Serializable

/**
 * Represents a message in the chat conversation.
 * Compatible with OpenAI's message format used by LM Studio.
 */
@Serializable
data class Message(
    val role: String,
    val content: String
) {
    companion object {
        const val ROLE_SYSTEM = "system"
        const val ROLE_USER = "user"
        const val ROLE_ASSISTANT = "assistant"

        fun system(content: String) = Message(ROLE_SYSTEM, content)
        fun user(content: String) = Message(ROLE_USER, content)
        fun assistant(content: String) = Message(ROLE_ASSISTANT, content)
    }
}
