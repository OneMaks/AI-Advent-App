package com.lmstudio.chat.storage

import java.io.File
import java.util.*

/**
 * Manages application configuration with support for file-based and runtime configuration.
 */
class ConfigurationManager(
    private val configFile: String = "config.properties"
) {
    private val properties = Properties()

    // Default configuration values
    var serverUrl: String = "http://localhost:1234/v1"
        private set

    var model: String? = null
        private set

    var temperature: Double = 0.7
        private set

    var maxTokens: Int = 2000
        private set

    var topP: Double = 1.0
        private set

    var frequencyPenalty: Double = 0.0
        private set

    var presencePenalty: Double = 0.0
        private set

    var streamingEnabled: Boolean = true
        private set

    var colorsEnabled: Boolean = true
        private set

    var defaultSystemPrompt: String? = null
        private set

    var conversationsDir: String = "conversations"
        private set

    var contextWindow: Int = 4096
        private set

    init {
        loadFromFile()
        loadFromEnvironment()
    }

    /**
     * Loads configuration from the properties file.
     */
    private fun loadFromFile() {
        val file = File(configFile)
        if (file.exists()) {
            try {
                file.inputStream().use { properties.load(it) }
                applyProperties()
            } catch (e: Exception) {
                // Use defaults if file can't be read
            }
        }

        // Also try to load from resources
        try {
            javaClass.classLoader.getResourceAsStream(configFile)?.use {
                properties.load(it)
                applyProperties()
            }
        } catch (e: Exception) {
            // Ignore
        }
    }

    /**
     * Loads configuration from environment variables.
     * Environment variables take precedence over file configuration.
     */
    private fun loadFromEnvironment() {
        System.getenv("LMSTUDIO_URL")?.let { serverUrl = it }
        System.getenv("LMSTUDIO_MODEL")?.let { model = it }
        System.getenv("LMSTUDIO_TEMPERATURE")?.toDoubleOrNull()?.let { temperature = it }
        System.getenv("LMSTUDIO_MAX_TOKENS")?.toIntOrNull()?.let { maxTokens = it }
        System.getenv("LMSTUDIO_TOP_P")?.toDoubleOrNull()?.let { topP = it }
        System.getenv("LMSTUDIO_STREAMING")?.toBooleanStrictOrNull()?.let { streamingEnabled = it }
        System.getenv("LMSTUDIO_COLORS")?.toBooleanStrictOrNull()?.let { colorsEnabled = it }
        System.getenv("LMSTUDIO_SYSTEM_PROMPT")?.let { defaultSystemPrompt = it }
    }

    private fun applyProperties() {
        properties.getProperty("server.url")?.let { serverUrl = it }
        properties.getProperty("model")?.let { model = it }
        properties.getProperty("temperature")?.toDoubleOrNull()?.let { temperature = it }
        properties.getProperty("max_tokens")?.toIntOrNull()?.let { maxTokens = it }
        properties.getProperty("top_p")?.toDoubleOrNull()?.let { topP = it }
        properties.getProperty("frequency_penalty")?.toDoubleOrNull()?.let { frequencyPenalty = it }
        properties.getProperty("presence_penalty")?.toDoubleOrNull()?.let { presencePenalty = it }
        properties.getProperty("streaming")?.toBooleanStrictOrNull()?.let { streamingEnabled = it }
        properties.getProperty("colors")?.toBooleanStrictOrNull()?.let { colorsEnabled = it }
        properties.getProperty("system_prompt")?.let { defaultSystemPrompt = it }
        properties.getProperty("conversations_dir")?.let { conversationsDir = it }
        properties.getProperty("context_window")?.toIntOrNull()?.let { contextWindow = it }
    }

    /**
     * Sets a parameter at runtime.
     */
    fun setParameter(name: String, value: Any): Boolean {
        return when (name.lowercase()) {
            "temperature" -> {
                val v = (value as? Double) ?: (value as? Number)?.toDouble()
                if (v != null && v in 0.0..2.0) {
                    temperature = v
                    true
                } else false
            }
            "max_tokens", "maxtokens" -> {
                val v = (value as? Int) ?: (value as? Number)?.toInt()
                if (v != null && v > 0) {
                    maxTokens = v
                    true
                } else false
            }
            "top_p", "topp" -> {
                val v = (value as? Double) ?: (value as? Number)?.toDouble()
                if (v != null && v in 0.0..1.0) {
                    topP = v
                    true
                } else false
            }
            "frequency_penalty" -> {
                val v = (value as? Double) ?: (value as? Number)?.toDouble()
                if (v != null && v in -2.0..2.0) {
                    frequencyPenalty = v
                    true
                } else false
            }
            "presence_penalty" -> {
                val v = (value as? Double) ?: (value as? Number)?.toDouble()
                if (v != null && v in -2.0..2.0) {
                    presencePenalty = v
                    true
                } else false
            }
            "streaming" -> {
                streamingEnabled = value as? Boolean ?: return false
                true
            }
            else -> false
        }
    }

    /**
     * Sets the current model.
     */
    fun setModel(modelName: String) {
        model = modelName
    }

    /**
     * Gets all configuration as a map for display.
     */
    fun toMap(): Map<String, Any?> = mapOf(
        "server_url" to serverUrl,
        "model" to (model ?: "(auto-detect)"),
        "temperature" to temperature,
        "max_tokens" to maxTokens,
        "top_p" to topP,
        "frequency_penalty" to frequencyPenalty,
        "presence_penalty" to presencePenalty,
        "streaming" to streamingEnabled,
        "colors" to colorsEnabled,
        "context_window" to contextWindow,
        "conversations_dir" to conversationsDir
    )

    /**
     * Saves current configuration to the properties file.
     */
    fun save(): Result<Unit> {
        return try {
            val props = Properties()
            props.setProperty("server.url", serverUrl)
            model?.let { props.setProperty("model", it) }
            props.setProperty("temperature", temperature.toString())
            props.setProperty("max_tokens", maxTokens.toString())
            props.setProperty("top_p", topP.toString())
            props.setProperty("frequency_penalty", frequencyPenalty.toString())
            props.setProperty("presence_penalty", presencePenalty.toString())
            props.setProperty("streaming", streamingEnabled.toString())
            props.setProperty("colors", colorsEnabled.toString())
            props.setProperty("context_window", contextWindow.toString())
            props.setProperty("conversations_dir", conversationsDir)
            defaultSystemPrompt?.let { props.setProperty("system_prompt", it) }

            File(configFile).outputStream().use {
                props.store(it, "LM Studio Chat Configuration")
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
