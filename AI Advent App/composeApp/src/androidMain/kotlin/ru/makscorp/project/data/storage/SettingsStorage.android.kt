package ru.makscorp.project.data.storage

import android.content.Context
import android.content.SharedPreferences
import ru.makscorp.project.domain.model.ChatSettings
import ru.makscorp.project.domain.model.GigaChatModel
import ru.makscorp.project.domain.model.OutputFormat

actual class SettingsStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "chat_settings",
        Context.MODE_PRIVATE
    )

    actual fun saveSettings(settings: ChatSettings) {
        prefs.edit()
            .putString(KEY_MODEL, settings.model.name)
            .putFloat(KEY_TEMPERATURE, settings.temperature)
            .putInt(KEY_MAX_TOKENS, settings.maxTokens)
            .putString(KEY_SYSTEM_PROMPT, settings.systemPrompt)
            .putString(KEY_OUTPUT_FORMAT, settings.outputFormat.name)
            .putBoolean(KEY_THINKING_MODE, settings.thinkingMode)
            .putBoolean(KEY_CONTEXT_COMPRESSION_ENABLED, settings.contextCompressionEnabled)
            .putInt(KEY_COMPRESSION_THRESHOLD, settings.compressionThreshold)
            .putInt(KEY_RECENT_MESSAGES_COUNT, settings.recentMessagesCount)
            .apply()
    }

    actual fun loadSettings(): ChatSettings {
        val modelName = prefs.getString(KEY_MODEL, GigaChatModel.GIGACHAT.name)
        val model = try {
            GigaChatModel.valueOf(modelName ?: GigaChatModel.GIGACHAT.name)
        } catch (e: Exception) {
            GigaChatModel.GIGACHAT
        }

        val outputFormatName = prefs.getString(KEY_OUTPUT_FORMAT, OutputFormat.NONE.name)
        val outputFormat = try {
            OutputFormat.valueOf(outputFormatName ?: OutputFormat.NONE.name)
        } catch (e: Exception) {
            OutputFormat.NONE
        }

        return ChatSettings(
            model = model,
            temperature = prefs.getFloat(KEY_TEMPERATURE, 0.7f),
            maxTokens = prefs.getInt(KEY_MAX_TOKENS, 2048),
            systemPrompt = prefs.getString(KEY_SYSTEM_PROMPT, "") ?: "",
            outputFormat = outputFormat,
            thinkingMode = prefs.getBoolean(KEY_THINKING_MODE, false),
            contextCompressionEnabled = prefs.getBoolean(KEY_CONTEXT_COMPRESSION_ENABLED, false),
            compressionThreshold = prefs.getInt(KEY_COMPRESSION_THRESHOLD, 20),
            recentMessagesCount = prefs.getInt(KEY_RECENT_MESSAGES_COUNT, 10)
        )
    }

    actual fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_MODEL = "model"
        const val KEY_TEMPERATURE = "temperature"
        const val KEY_MAX_TOKENS = "max_tokens"
        const val KEY_SYSTEM_PROMPT = "system_prompt"
        const val KEY_OUTPUT_FORMAT = "output_format"
        const val KEY_THINKING_MODE = "thinking_mode"
        const val KEY_CONTEXT_COMPRESSION_ENABLED = "context_compression_enabled"
        const val KEY_COMPRESSION_THRESHOLD = "compression_threshold"
        const val KEY_RECENT_MESSAGES_COUNT = "recent_messages_count"
    }
}
