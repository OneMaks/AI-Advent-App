package com.lmstudio.chat

import com.lmstudio.chat.api.LMStudioClient
import com.lmstudio.chat.api.StreamResult
import com.lmstudio.chat.api.models.ChatRequest
import com.lmstudio.chat.storage.ConfigurationManager
import com.lmstudio.chat.storage.ConversationManager
import com.lmstudio.chat.terminal.Command
import com.lmstudio.chat.terminal.CommandProcessor
import com.lmstudio.chat.terminal.TerminalUI
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.runBlocking

/**
 * Main chat application that orchestrates all components.
 */
class ChatApplication(
    private val config: ConfigurationManager = ConfigurationManager(),
    private val conversationManager: ConversationManager = ConversationManager(config.conversationsDir),
    private val ui: TerminalUI = TerminalUI(config.colorsEnabled),
    private val commandProcessor: CommandProcessor = CommandProcessor()
) {
    private lateinit var client: LMStudioClient
    private var currentModel: String? = null
    private var availableModels: List<String> = emptyList()
    private var isRunning = true

    /**
     * Starts the chat application.
     */
    fun run() = runBlocking {
        // Initialize client
        client = LMStudioClient(baseUrl = config.serverUrl)

        // Check server availability
        if (!client.isServerAvailable()) {
            ui.showError("Cannot connect to LM Studio at ${config.serverUrl}")
            ui.showInfo("Make sure LM Studio is running and the server is started.")
            ui.showInfo("You can configure the server URL in config.properties or via LMSTUDIO_URL environment variable.")
            return@runBlocking
        }

        // Fetch available models
        fetchModels()

        // Set initial model
        currentModel = config.model ?: availableModels.firstOrNull()

        // Set default system prompt if configured
        config.defaultSystemPrompt?.let {
            conversationManager.setSystemPrompt(it)
        }

        // Show welcome
        ui.showWelcome(config.serverUrl, currentModel)

        // Main loop
        while (isRunning) {
            val input = ui.readInput()

            if (input == null) {
                // EOF reached
                break
            }

            if (input.isBlank()) {
                continue
            }

            val command = commandProcessor.parse(input)
            handleCommand(command)
        }

        // Cleanup
        client.close()
        ui.showGoodbye()
    }

    private suspend fun handleCommand(command: Command) {
        when (command) {
            is Command.Chat -> handleChat(command.message)
            is Command.Help -> ui.showHelp()
            is Command.Clear -> handleClear()
            is Command.Exit -> isRunning = false
            is Command.Save -> handleSave(command.filename)
            is Command.Load -> handleLoad(command.filename)
            is Command.SwitchModel -> handleSwitchModel(command.modelName)
            is Command.ListModels -> handleListModels()
            is Command.ShowConfig -> ui.showConfig(config.toMap())
            is Command.SetParameter -> handleSetParameter(command.name, command.value)
            is Command.SetSystemPrompt -> handleSetSystemPrompt(command.prompt)
            is Command.ShowHistory -> ui.showHistory(conversationManager.getHistory())
            is Command.ShowTokens -> handleShowTokens()
            is Command.MultiLineInput -> handleMultiLineInput()
            is Command.Error -> ui.showError(command.message)
            is Command.Unknown -> ui.showError("Unknown command: ${command.command}. Type /help for available commands.")
        }
    }

    private suspend fun handleChat(message: String) {
        if (currentModel == null) {
            ui.showError("No model selected. Use /model <name> or /models to see available models.")
            return
        }

        // Add user message to conversation
        conversationManager.addUserMessage(message)

        // Trim conversation to fit context window
        conversationManager.trimToTokenLimit(config.contextWindow)

        // Build request
        val request = ChatRequest(
            model = currentModel!!,
            messages = conversationManager.getMessages(),
            temperature = config.temperature,
            maxTokens = config.maxTokens,
            topP = config.topP,
            frequencyPenalty = config.frequencyPenalty,
            presencePenalty = config.presencePenalty,
            stream = config.streamingEnabled
        )

        if (config.streamingEnabled) {
            handleStreamingChat(request)
        } else {
            handleNonStreamingChat(request)
        }
    }

    private suspend fun handleStreamingChat(request: ChatRequest) {
        ui.showAssistantLabel()

        val responseBuilder = StringBuilder()
        var hasError = false

        client.chatStream(request)
            .onEach { result ->
                when (result) {
                    is StreamResult.Content -> {
                        responseBuilder.append(result.text)
                        ui.printStreamContent(result.text)
                    }
                    is StreamResult.Error -> {
                        hasError = true
                        ui.endStreamResponse()
                        ui.showError(result.exception.message ?: "Unknown error")
                    }
                    is StreamResult.Finished, StreamResult.Done -> {
                        // Stream completed
                    }
                }
            }
            .collect()

        if (!hasError) {
            ui.endStreamResponse()
            if (responseBuilder.isNotEmpty()) {
                conversationManager.addAssistantMessage(responseBuilder.toString())
            }
        }
    }

    private suspend fun handleNonStreamingChat(request: ChatRequest) {
        ui.showThinking()

        val result = client.chat(request)

        ui.clearLine()

        result.fold(
            onSuccess = { response ->
                val content = response.choices.firstOrNull()?.message?.content ?: ""
                ui.showAssistantResponse(content)
                conversationManager.addAssistantMessage(content)

                // Update token usage
                response.usage?.let {
                    conversationManager.updateTokenUsage(it.promptTokens, it.completionTokens)
                }
            },
            onFailure = { error ->
                ui.showError(error.message ?: "Unknown error occurred")
                // Remove the user message since request failed
                // Note: Simplified - in production you'd want proper rollback
            }
        )
    }

    private fun handleClear() {
        conversationManager.clear()
        ui.showSuccess("Conversation history cleared.")
    }

    private fun handleSave(filename: String) {
        conversationManager.save(filename).fold(
            onSuccess = { path ->
                ui.showSuccess("Conversation saved to $path")
            },
            onFailure = { error ->
                ui.showError("Failed to save: ${error.message}")
            }
        )
    }

    private fun handleLoad(filename: String) {
        conversationManager.load(filename).fold(
            onSuccess = {
                ui.showSuccess("Conversation loaded from $filename")
                ui.showInfo("${conversationManager.messageCount()} messages loaded.")
            },
            onFailure = { error ->
                ui.showError("Failed to load: ${error.message}")
            }
        )
    }

    private suspend fun handleSwitchModel(modelName: String) {
        // Check if model exists in available models
        val matchingModel = availableModels.find {
            it.equals(modelName, ignoreCase = true) || it.contains(modelName, ignoreCase = true)
        }

        if (matchingModel != null) {
            currentModel = matchingModel
            config.setModel(matchingModel)
            ui.showSuccess("Switched to model: $matchingModel")
        } else {
            // Try to use the model name directly
            currentModel = modelName
            config.setModel(modelName)
            ui.showWarning("Model '$modelName' not in known models list, but will try to use it.")
        }
    }

    private suspend fun handleListModels() {
        // Refresh models list
        fetchModels()
        ui.showModels(availableModels, currentModel)
    }

    private fun handleSetParameter(name: String, value: Any) {
        if (config.setParameter(name, value)) {
            ui.showSuccess("Set $name = $value")
        } else {
            ui.showError("Failed to set $name")
        }
    }

    private fun handleSetSystemPrompt(prompt: String) {
        conversationManager.setSystemPrompt(prompt)
        ui.showSuccess("System prompt updated.")
    }

    private fun handleShowTokens() {
        val (prompt, completion, total) = conversationManager.getTokenUsage()
        ui.showTokens(prompt, completion, total)
    }

    private suspend fun handleMultiLineInput() {
        val message = ui.readMultiLineInput()
        if (message.isNotBlank()) {
            handleChat(message)
        }
    }

    private suspend fun fetchModels() {
        client.getModels().fold(
            onSuccess = { response ->
                availableModels = response.data.map { it.id }
            },
            onFailure = {
                availableModels = emptyList()
            }
        )
    }
}
