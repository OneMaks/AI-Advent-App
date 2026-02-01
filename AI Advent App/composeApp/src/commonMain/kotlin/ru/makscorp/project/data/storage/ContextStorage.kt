package ru.makscorp.project.data.storage

import ru.makscorp.project.domain.model.ConversationContext

expect class ContextStorage {
    fun saveContext(context: ConversationContext)
    fun loadContext(): ConversationContext?
    fun clearContext()
}
