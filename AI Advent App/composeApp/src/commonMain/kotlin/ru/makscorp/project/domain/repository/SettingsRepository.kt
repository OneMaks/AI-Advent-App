package ru.makscorp.project.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.makscorp.project.domain.model.ChatSettings

interface SettingsRepository {
    val settings: Flow<ChatSettings>

    fun getSettings(): ChatSettings
    fun updateSettings(settings: ChatSettings)
    fun resetToDefaults()
}
