package com.lmstudio.chat

/**
 * Entry point for the LM Studio Terminal Chat application.
 *
 * This application provides an interactive command-line interface for chatting
 * with local LLMs via LM Studio's OpenAI-compatible API.
 *
 * Features:
 * - Interactive chat with streaming responses
 * - Conversation history management (save/load)
 * - Configurable parameters (temperature, max_tokens, etc.)
 * - Multiple model support
 * - System prompt customization
 *
 * Usage:
 *   ./gradlew run
 *   java -jar kotlin-lmstudio-chat.jar
 *
 * Configuration:
 *   - config.properties file
 *   - Environment variables (LMSTUDIO_URL, LMSTUDIO_MODEL, etc.)
 *
 * Commands:
 *   /help     - Show available commands
 *   /clear    - Clear conversation history
 *   /save     - Save conversation to file
 *   /load     - Load conversation from file
 *   /model    - Switch model
 *   /models   - List available models
 *   /config   - Show current configuration
 *   /system   - Set system prompt
 *   /exit     - Exit the application
 */
fun main(args: Array<String>) {
    // Handle command-line arguments
    if (args.contains("--help") || args.contains("-h")) {
        printHelp()
        return
    }

    if (args.contains("--version") || args.contains("-v")) {
        println("LM Studio Terminal Chat v1.0.0")
        return
    }

    // Run the application
    val app = ChatApplication()
    app.run()
}

private fun printHelp() {
    println("""
        LM Studio Terminal Chat - Interactive CLI for LM Studio

        USAGE:
            kotlin-lmstudio-chat [OPTIONS]

        OPTIONS:
            -h, --help      Show this help message
            -v, --version   Show version information

        CONFIGURATION:
            The application can be configured through:
            1. config.properties file in the current directory
            2. Environment variables:
               - LMSTUDIO_URL       : LM Studio server URL (default: http://localhost:1234/v1)
               - LMSTUDIO_MODEL     : Default model to use
               - LMSTUDIO_TEMPERATURE: Temperature setting (0.0-2.0)
               - LMSTUDIO_MAX_TOKENS : Maximum tokens for response
               - LMSTUDIO_TOP_P     : Top-p sampling parameter
               - LMSTUDIO_STREAMING : Enable/disable streaming (true/false)
               - LMSTUDIO_COLORS    : Enable/disable colors (true/false)
               - LMSTUDIO_SYSTEM_PROMPT: Default system prompt

        COMMANDS:
            Once running, you can use these commands:
            /help              - Show available commands
            /clear             - Clear conversation history
            /save <filename>   - Save conversation to file
            /load <filename>   - Load conversation from file
            /model <name>      - Switch to a different model
            /models            - List available models
            /config            - Show current configuration
            /set <param> <val> - Set a parameter (temperature, max_tokens, top_p)
            /system <prompt>   - Set the system prompt
            /history           - Show conversation history
            /tokens            - Show token usage statistics
            /multiline         - Enter multi-line input mode
            /exit, /quit       - Exit the application

        EXAMPLES:
            Start chatting:
                You: Hello! Who are you?
                AI: Hi! I'm an AI assistant running locally...

            Set system prompt:
                /system You are a helpful coding assistant

            Save conversation:
                /save my-conversation

            Change temperature:
                /set temperature 0.8

        For more information, visit: https://github.com/your-repo/kotlin-lmstudio-chat
    """.trimIndent())
}
