package com.lmstudio.chat.terminal

/**
 * Processes terminal commands and returns the appropriate action.
 */
class CommandProcessor {

    /**
     * Parses user input and determines if it's a command.
     */
    fun parse(input: String): Command {
        val trimmed = input.trim()

        if (!trimmed.startsWith("/")) {
            return Command.Chat(trimmed)
        }

        val parts = trimmed.split(" ", limit = 2)
        val commandName = parts[0].lowercase()
        val args = parts.getOrNull(1)?.trim() ?: ""

        return when (commandName) {
            "/help" -> Command.Help
            "/clear" -> Command.Clear
            "/exit", "/quit" -> Command.Exit
            "/save" -> {
                if (args.isEmpty()) {
                    Command.Error("Usage: /save <filename>")
                } else {
                    Command.Save(args)
                }
            }
            "/load" -> {
                if (args.isEmpty()) {
                    Command.Error("Usage: /load <filename>")
                } else {
                    Command.Load(args)
                }
            }
            "/model" -> {
                if (args.isEmpty()) {
                    Command.Error("Usage: /model <name>")
                } else {
                    Command.SwitchModel(args)
                }
            }
            "/models" -> Command.ListModels
            "/config" -> Command.ShowConfig
            "/set" -> {
                parseSetCommand(args)
            }
            "/system" -> {
                if (args.isEmpty()) {
                    Command.Error("Usage: /system <prompt>")
                } else {
                    Command.SetSystemPrompt(args)
                }
            }
            "/history" -> Command.ShowHistory
            "/tokens" -> Command.ShowTokens
            "/multiline" -> Command.MultiLineInput
            else -> Command.Unknown(commandName)
        }
    }

    private fun parseSetCommand(args: String): Command {
        if (args.isEmpty()) {
            return Command.Error("Usage: /set <parameter> <value>\nParameters: temperature, max_tokens, top_p")
        }

        val setParts = args.split(" ", limit = 2)
        if (setParts.size < 2) {
            return Command.Error("Usage: /set <parameter> <value>")
        }

        val parameter = setParts[0].lowercase()
        val valueStr = setParts[1]

        return when (parameter) {
            "temperature" -> {
                val value = valueStr.toDoubleOrNull()
                if (value == null || value < 0 || value > 2) {
                    Command.Error("Temperature must be a number between 0 and 2")
                } else {
                    Command.SetParameter("temperature", value)
                }
            }
            "max_tokens", "maxtokens" -> {
                val value = valueStr.toIntOrNull()
                if (value == null || value < 1) {
                    Command.Error("max_tokens must be a positive integer")
                } else {
                    Command.SetParameter("max_tokens", value)
                }
            }
            "top_p", "topp" -> {
                val value = valueStr.toDoubleOrNull()
                if (value == null || value < 0 || value > 1) {
                    Command.Error("top_p must be a number between 0 and 1")
                } else {
                    Command.SetParameter("top_p", value)
                }
            }
            else -> Command.Error("Unknown parameter: $parameter\nValid parameters: temperature, max_tokens, top_p")
        }
    }
}

/**
 * Represents a parsed command or user input.
 */
sealed class Command {
    data class Chat(val message: String) : Command()
    data class Save(val filename: String) : Command()
    data class Load(val filename: String) : Command()
    data class SwitchModel(val modelName: String) : Command()
    data class SetSystemPrompt(val prompt: String) : Command()
    data class SetParameter(val name: String, val value: Any) : Command()
    data class Error(val message: String) : Command()
    data class Unknown(val command: String) : Command()

    data object Help : Command()
    data object Clear : Command()
    data object Exit : Command()
    data object ListModels : Command()
    data object ShowConfig : Command()
    data object ShowHistory : Command()
    data object ShowTokens : Command()
    data object MultiLineInput : Command()
}
