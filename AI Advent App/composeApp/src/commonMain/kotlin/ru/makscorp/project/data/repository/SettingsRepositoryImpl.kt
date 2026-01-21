package ru.makscorp.project.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.makscorp.project.data.storage.SettingsStorage
import ru.makscorp.project.domain.model.ChatSettings
import ru.makscorp.project.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val settingsStorage: SettingsStorage
) : SettingsRepository {

    private val _settings = MutableStateFlow(settingsStorage.loadSettings())
    override val settings: Flow<ChatSettings> = _settings.asStateFlow()

    override fun getSettings(): ChatSettings = _settings.value

    override fun updateSettings(settings: ChatSettings) {
        settingsStorage.saveSettings(settings)
        _settings.value = settings
    }

    override fun resetToDefaults() {
        val defaults = ChatSettings()
        settingsStorage.saveSettings(defaults)
        _settings.value = defaults
    }
}
