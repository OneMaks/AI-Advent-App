package ru.makscorp.project.domain.model

data class ChatSettings(
    val model: GigaChatModel = GigaChatModel.GIGACHAT,
    val temperature: Float = 0.7f,
    val maxTokens: Int = 2048,
    val systemPrompt: String = "",
    val outputFormat: OutputFormat = OutputFormat.NONE
)

enum class GigaChatModel(val apiName: String, val displayName: String) {
    GIGACHAT("GigaChat", "GigaChat"),
    GIGACHAT_PRO("GigaChat-Pro", "GigaChat Pro"),
    GIGACHAT_MAX("GigaChat-Max", "GigaChat Max")
}

enum class OutputFormat(val displayName: String) {
    NONE("Нет"),
    JSON("JSON")
}
