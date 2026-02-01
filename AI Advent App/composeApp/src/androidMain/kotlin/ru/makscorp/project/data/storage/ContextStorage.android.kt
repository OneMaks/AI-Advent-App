package ru.makscorp.project.data.storage

import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.makscorp.project.domain.model.ConversationContext

actual class ContextStorage(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(
        "context_storage",
        Context.MODE_PRIVATE
    )

    private val json = Json { ignoreUnknownKeys = true }

    actual fun saveContext(context: ConversationContext) {
        val jsonString = json.encodeToString(context)
        prefs.edit()
            .putString(KEY_CONTEXT, jsonString)
            .apply()
    }

    actual fun loadContext(): ConversationContext? {
        val jsonString = prefs.getString(KEY_CONTEXT, null) ?: return null
        return try {
            json.decodeFromString<ConversationContext>(jsonString)
        } catch (e: Exception) {
            null
        }
    }

    actual fun clearContext() {
        prefs.edit()
            .remove(KEY_CONTEXT)
            .apply()
    }

    private companion object {
        const val KEY_CONTEXT = "conversation_context"
    }
}
