package com.lmstudio.chat.storage

import com.lmstudio.chat.api.models.Message
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant

/**
 * Manages conversation history, including save/load functionality.
 */
class ConversationManager(
    private val conversationsDir: String = "conversations"
) {
    private val messages = mutableListOf<Message>()
    private var systemPrompt: String? = null

    // Token usage tracking
    private var totalPromptTokens: Int = 0
    private var totalCompletionTokens: Int = 0

    private val json = Json {
        prettyPrint = true
        encodeDefaults = true
    }

    init {
        File(conversationsDir).mkdirs()
    }

    /**
     * Sets the system prompt for the conversation.
     */
    fun setSystemPrompt(prompt: String?) {
        systemPrompt = prompt
    }

    /**
     * Gets the current system prompt.
     */
    fun getSystemPrompt(): String? = systemPrompt

    /**
     * Adds a user message to the conversation.
     */
    fun addUserMessage(content: String) {
        messages.add(Message.user(content))
    }

    /**
     * Adds an assistant message to the conversation.
     */
    fun addAssistantMessage(content: String) {
        messages.add(Message.assistant(content))
    }

    /**
     * Gets all messages including the system prompt.
     */
    fun getMessages(): List<Message> {
        val result = mutableListOf<Message>()
        systemPrompt?.let { result.add(Message.system(it)) }
        result.addAll(messages)
        return result
    }

    /**
     * Gets the message history as role-content pairs.
     */
    fun getHistory(): List<Pair<String, String>> {
        val result = mutableListOf<Pair<String, String>>()
        systemPrompt?.let { result.add("system" to it) }
        messages.forEach { result.add(it.role to it.content) }
        return result
    }

    /**
     * Clears the conversation history (but keeps system prompt).
     */
    fun clear() {
        messages.clear()
        totalPromptTokens = 0
        totalCompletionTokens = 0
    }

    /**
     * Clears everything including system prompt.
     */
    fun clearAll() {
        clear()
        systemPrompt = null
    }

    /**
     * Returns the number of messages in the conversation.
     */
    fun messageCount(): Int = messages.size

    /**
     * Updates token usage statistics.
     */
    fun updateTokenUsage(promptTokens: Int, completionTokens: Int) {
        totalPromptTokens += promptTokens
        totalCompletionTokens += completionTokens
    }

    /**
     * Gets total token usage.
     */
    fun getTokenUsage(): Triple<Int, Int, Int> =
        Triple(totalPromptTokens, totalCompletionTokens, totalPromptTokens + totalCompletionTokens)

    /**
     * Saves the conversation to a JSON file.
     */
    fun save(filename: String): Result<String> {
        return try {
            val file = getConversationFile(filename)
            val conversation = SavedConversation(
                systemPrompt = systemPrompt,
                messages = messages.map { SavedMessage(it.role, it.content) },
                savedAt = Instant.now().toString(),
                tokenUsage = TokenUsage(totalPromptTokens, totalCompletionTokens)
            )
            file.writeText(json.encodeToString(SavedConversation.serializer(), conversation))
            Result.success(file.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Loads a conversation from a JSON file.
     */
    fun load(filename: String): Result<Unit> {
        return try {
            val file = getConversationFile(filename)
            if (!file.exists()) {
                return Result.failure(Exception("File not found: ${file.absolutePath}"))
            }

            val content = file.readText()
            val conversation = json.decodeFromString<SavedConversation>(content)

            // Restore conversation
            messages.clear()
            systemPrompt = conversation.systemPrompt
            conversation.messages.forEach {
                messages.add(Message(it.role, it.content))
            }
            conversation.tokenUsage?.let {
                totalPromptTokens = it.promptTokens
                totalCompletionTokens = it.completionTokens
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Lists all saved conversations.
     */
    fun listSavedConversations(): List<String> {
        return File(conversationsDir)
            .listFiles { file -> file.extension == "json" }
            ?.map { it.nameWithoutExtension }
            ?.sorted()
            ?: emptyList()
    }

    private fun getConversationFile(filename: String): File {
        val name = if (filename.endsWith(".json")) filename else "$filename.json"
        return File(conversationsDir, name)
    }

    /**
     * Trims old messages to stay within a token limit estimate.
     * Uses a rough estimate of 4 characters per token.
     */
    fun trimToTokenLimit(maxTokens: Int) {
        val charsPerToken = 4
        val maxChars = maxTokens * charsPerToken

        // Keep system prompt
        var totalChars = (systemPrompt?.length ?: 0)

        // Count from newest to oldest, find cutoff
        val reversedMessages = messages.reversed()
        var keepCount = 0

        for (message in reversedMessages) {
            val messageChars = message.content.length + message.role.length
            if (totalChars + messageChars > maxChars) {
                break
            }
            totalChars += messageChars
            keepCount++
        }

        // Keep the most recent messages
        if (keepCount < messages.size) {
            val toRemove = messages.size - keepCount
            repeat(toRemove) { messages.removeAt(0) }
        }
    }
}

@Serializable
private data class SavedConversation(
    val systemPrompt: String? = null,
    val messages: List<SavedMessage>,
    val savedAt: String,
    val tokenUsage: TokenUsage? = null
)

@Serializable
private data class SavedMessage(
    val role: String,
    val content: String
)

@Serializable
private data class TokenUsage(
    val promptTokens: Int,
    val completionTokens: Int
)
