package com.lmstudio.chat.terminal

import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Handles terminal UI rendering and user input.
 */
class TerminalUI(
    private val enableColors: Boolean = true
) {
    private val reader = BufferedReader(InputStreamReader(System.`in`))

    /**
     * Displays the welcome banner.
     */
    fun showWelcome(serverUrl: String, model: String?) {
        println()
        printColored("╔══════════════════════════════════════════════════════════════╗", Colors.CYAN)
        printColored("║               LM Studio Terminal Chat                        ║", Colors.CYAN)
        printColored("╚══════════════════════════════════════════════════════════════╝", Colors.CYAN)
        println()
        printSuccess("Connected to LM Studio at $serverUrl")
        if (model != null) {
            printInfo("Current model: $model")
        }
        printInfo("Type /help for commands or start chatting!")
        println()
    }

    /**
     * Displays the help menu.
     */
    fun showHelp() {
        println()
        printColored("Available Commands:", Colors.BOLD_CYAN)
        println()
        showCommand("/help", "Show this help message")
        showCommand("/clear", "Clear conversation history")
        showCommand("/save <filename>", "Save conversation to file")
        showCommand("/load <filename>", "Load conversation from file")
        showCommand("/model <name>", "Switch to a different model")
        showCommand("/models", "List available models")
        showCommand("/config", "Show current configuration")
        showCommand("/set <param> <value>", "Set a parameter (temperature, max_tokens, top_p)")
        showCommand("/system <prompt>", "Set the system prompt")
        showCommand("/history", "Show conversation history")
        showCommand("/tokens", "Show token usage statistics")
        showCommand("/exit, /quit", "Exit the application")
        println()
    }

    private fun showCommand(command: String, description: String) {
        val cmd = if (enableColors) "${Colors.GREEN}$command${Colors.RESET}" else command
        val desc = if (enableColors) "${Colors.DIM}$description${Colors.RESET}" else description
        println("  $cmd - $desc")
    }

    /**
     * Reads a line of user input.
     */
    fun readInput(): String? {
        printColored("You: ", Colors.BOLD_GREEN, newLine = false)
        return try {
            reader.readLine()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Reads multi-line input until an empty line is entered.
     */
    fun readMultiLineInput(): String {
        val lines = mutableListOf<String>()
        printColored("You (enter empty line to send): ", Colors.BOLD_GREEN)

        while (true) {
            val line = reader.readLine() ?: break
            if (line.isEmpty()) break
            lines.add(line)
            printColored("... ", Colors.DIM, newLine = false)
        }

        return lines.joinToString("\n")
    }

    /**
     * Shows the AI response label.
     */
    fun showAssistantLabel() {
        printColored("AI: ", Colors.BOLD_BLUE, newLine = false)
    }

    /**
     * Prints streaming content without a newline.
     */
    fun printStreamContent(content: String) {
        print(content)
        System.out.flush()
    }

    /**
     * Ends the streaming response with a newline.
     */
    fun endStreamResponse() {
        println()
        println()
    }

    /**
     * Shows a complete AI response.
     */
    fun showAssistantResponse(response: String) {
        showAssistantLabel()
        println(response)
        println()
    }

    /**
     * Shows a thinking/loading indicator.
     */
    fun showThinking() {
        printColored("Thinking...", Colors.DIM)
    }

    /**
     * Clears the current line (for removing thinking indicator).
     */
    fun clearLine() {
        print("\r\u001B[K")
        System.out.flush()
    }

    /**
     * Shows an error message.
     */
    fun showError(message: String) {
        printColored("Error: $message", Colors.RED)
    }

    /**
     * Shows a success message.
     */
    fun showSuccess(message: String) {
        printColored(message, Colors.GREEN)
    }

    /**
     * Shows an info message.
     */
    fun showInfo(message: String) {
        printColored(message, Colors.CYAN)
    }

    /**
     * Shows a warning message.
     */
    fun showWarning(message: String) {
        printColored(message, Colors.YELLOW)
    }

    /**
     * Shows configuration details.
     */
    fun showConfig(config: Map<String, Any?>) {
        println()
        printColored("Current Configuration:", Colors.BOLD_CYAN)
        println()
        config.forEach { (key, value) ->
            val formattedKey = if (enableColors) "${Colors.YELLOW}$key${Colors.RESET}" else key
            val formattedValue = if (enableColors) "${Colors.WHITE}$value${Colors.RESET}" else value.toString()
            println("  $formattedKey: $formattedValue")
        }
        println()
    }

    /**
     * Shows the list of available models.
     */
    fun showModels(models: List<String>, currentModel: String?) {
        println()
        printColored("Available Models:", Colors.BOLD_CYAN)
        println()
        models.forEachIndexed { index, model ->
            val marker = if (model == currentModel) " (current)" else ""
            val modelName = if (enableColors) "${Colors.GREEN}$model${Colors.RESET}" else model
            val markerText = if (enableColors && marker.isNotEmpty()) "${Colors.YELLOW}$marker${Colors.RESET}" else marker
            println("  ${index + 1}. $modelName$markerText")
        }
        println()
    }

    /**
     * Shows conversation history.
     */
    fun showHistory(messages: List<Pair<String, String>>) {
        println()
        printColored("Conversation History:", Colors.BOLD_CYAN)
        println()

        if (messages.isEmpty()) {
            printColored("  (empty)", Colors.DIM)
        } else {
            messages.forEach { (role, content) ->
                val roleColor = when (role) {
                    "system" -> Colors.PURPLE
                    "user" -> Colors.GREEN
                    "assistant" -> Colors.BLUE
                    else -> Colors.WHITE
                }
                val roleLabel = if (enableColors) "$roleColor${role.uppercase()}${Colors.RESET}" else role.uppercase()
                println("[$roleLabel]")
                println("  ${content.take(200)}${if (content.length > 200) "..." else ""}")
                println()
            }
        }
    }

    /**
     * Shows token usage statistics.
     */
    fun showTokens(promptTokens: Int, completionTokens: Int, totalTokens: Int) {
        println()
        printColored("Token Usage:", Colors.BOLD_CYAN)
        println()
        println("  Prompt tokens:     $promptTokens")
        println("  Completion tokens: $completionTokens")
        println("  Total tokens:      $totalTokens")
        println()
    }

    /**
     * Shows goodbye message.
     */
    fun showGoodbye() {
        println()
        printColored("Goodbye!", Colors.CYAN)
        println()
    }

    private fun printColored(text: String, color: String, newLine: Boolean = true) {
        val output = if (enableColors) "$color$text${Colors.RESET}" else text
        if (newLine) {
            println(output)
        } else {
            print(output)
            System.out.flush()
        }
    }

    private fun printSuccess(message: String) = printColored(message, Colors.GREEN)
    private fun printInfo(message: String) = printColored(message, Colors.CYAN)
}
