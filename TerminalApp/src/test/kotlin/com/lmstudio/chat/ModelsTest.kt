package com.lmstudio.chat

import com.lmstudio.chat.api.models.*
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Unit tests for API model serialization/deserialization.
 */
class ModelsTest {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    @Test
    fun `test Message serialization`() {
        val message = Message.user("Hello, world!")
        val serialized = json.encodeToString(Message.serializer(), message)
        val deserialized = json.decodeFromString<Message>(serialized)

        assertEquals(message.role, deserialized.role)
        assertEquals(message.content, deserialized.content)
    }

    @Test
    fun `test Message factory methods`() {
        val system = Message.system("You are helpful")
        val user = Message.user("Hello")
        val assistant = Message.assistant("Hi there!")

        assertEquals("system", system.role)
        assertEquals("user", user.role)
        assertEquals("assistant", assistant.role)
    }

    @Test
    fun `test ChatRequest serialization`() {
        val request = ChatRequest(
            model = "test-model",
            messages = listOf(
                Message.system("You are helpful"),
                Message.user("Hello")
            ),
            temperature = 0.7,
            maxTokens = 100,
            stream = false
        )

        val serialized = json.encodeToString(ChatRequest.serializer(), request)
        val deserialized = json.decodeFromString<ChatRequest>(serialized)

        assertEquals(request.model, deserialized.model)
        assertEquals(request.messages.size, deserialized.messages.size)
        assertEquals(request.temperature, deserialized.temperature)
        assertEquals(request.maxTokens, deserialized.maxTokens)
    }

    @Test
    fun `test ChatResponse deserialization`() {
        val responseJson = """
        {
            "id": "chatcmpl-123",
            "object": "chat.completion",
            "created": 1234567890,
            "model": "test-model",
            "choices": [
                {
                    "index": 0,
                    "message": {
                        "role": "assistant",
                        "content": "Hello! How can I help?"
                    },
                    "finish_reason": "stop"
                }
            ],
            "usage": {
                "prompt_tokens": 10,
                "completion_tokens": 20,
                "total_tokens": 30
            }
        }
        """.trimIndent()

        val response = json.decodeFromString<ChatResponse>(responseJson)

        assertEquals("chatcmpl-123", response.id)
        assertEquals("chat.completion", response.`object`)
        assertEquals(1234567890L, response.created)
        assertEquals("test-model", response.model)
        assertEquals(1, response.choices.size)

        val choice = response.choices.first()
        assertEquals(0, choice.index)
        assertEquals("stop", choice.finishReason)
        assertNotNull(choice.message)
        assertEquals("assistant", choice.message!!.role)
        assertEquals("Hello! How can I help?", choice.message!!.content)

        assertNotNull(response.usage)
        assertEquals(10, response.usage!!.promptTokens)
        assertEquals(20, response.usage!!.completionTokens)
        assertEquals(30, response.usage!!.totalTokens)
    }

    @Test
    fun `test StreamChunk deserialization`() {
        val chunkJson = """
        {
            "id": "chatcmpl-123",
            "object": "chat.completion.chunk",
            "created": 1234567890,
            "model": "test-model",
            "choices": [
                {
                    "index": 0,
                    "delta": {
                        "content": "Hello"
                    },
                    "finish_reason": null
                }
            ]
        }
        """.trimIndent()

        val chunk = json.decodeFromString<StreamChunk>(chunkJson)

        assertEquals("chatcmpl-123", chunk.id)
        assertEquals("chat.completion.chunk", chunk.`object`)
        assertEquals(1, chunk.choices.size)

        val choice = chunk.choices.first()
        assertEquals("Hello", choice.delta.content)
    }

    @Test
    fun `test ModelsResponse deserialization`() {
        val modelsJson = """
        {
            "object": "list",
            "data": [
                {
                    "id": "llama-3.2-1b",
                    "object": "model",
                    "created": 1234567890,
                    "owned_by": "local"
                },
                {
                    "id": "mistral-7b",
                    "object": "model",
                    "owned_by": "local"
                }
            ]
        }
        """.trimIndent()

        val response = json.decodeFromString<ModelsResponse>(modelsJson)

        assertEquals("list", response.`object`)
        assertEquals(2, response.data.size)
        assertEquals("llama-3.2-1b", response.data[0].id)
        assertEquals("mistral-7b", response.data[1].id)
    }
}
