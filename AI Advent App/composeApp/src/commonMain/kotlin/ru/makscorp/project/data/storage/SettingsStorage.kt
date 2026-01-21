package ru.makscorp.project.data.storage

import ru.makscorp.project.domain.model.ChatSettings
import ru.makscorp.project.domain.model.GigaChatModel

expect class SettingsStorage {
    fun saveSettings(settings: ChatSettings)
    fun loadSettings(): ChatSettings
    fun clear()
}
