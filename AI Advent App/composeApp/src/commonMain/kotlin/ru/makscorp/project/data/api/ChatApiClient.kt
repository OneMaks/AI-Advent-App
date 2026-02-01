package ru.makscorp.project.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import kotlinx.serialization.json.Json
import ru.makscorp.project.data.api.dto.ApiErrorDto
import ru.makscorp.project.data.api.dto.ChatMessageDto
import ru.makscorp.project.data.api.dto.ChatRequestDto
import ru.makscorp.project.data.api.dto.ChatResponseDto
import ru.makscorp.project.domain.model.OutputFormat
import ru.makscorp.project.domain.repository.AuthRepository
import ru.makscorp.project.domain.repository.SettingsRepository

class ChatApiClient(
    private val httpClient: HttpClient,
    private val authRepository: AuthRepository,
    private val settingsRepository: SettingsRepository
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun sendMessage(messages: List<ChatMessageDto>): Result<ChatResponseDto> {
        return try {
            val settings = settingsRepository.getSettings()

            // Determine system prompt based on output format and thinking mode
            val systemPrompt = when {
                settings.outputFormat == OutputFormat.JSON -> JSON_SYSTEM_PROMPT
                settings.thinkingMode -> THINKING_MODE_SYSTEM_PROMPT +
                    if (settings.systemPrompt.isNotBlank()) "\n\nДополнительные инструкции: ${settings.systemPrompt}" else ""
                else -> settings.systemPrompt
            }

            // Add system prompt if configured
            val messagesWithSystem = if (systemPrompt.isNotBlank()) {
                listOf(ChatMessageDto(role = "system", content = systemPrompt)) + messages
            } else {
                messages
            }

            val request = ChatRequestDto(
                model = settings.model.apiName,
                messages = messagesWithSystem,
                maxTokens = settings.maxTokens,
                temperature = settings.temperature.toDouble()
            )

            // First attempt with current token
            var accessToken = authRepository.getValidToken().getOrElse { error ->
                return Result.failure(
                    ApiException(
                        message = "Authentication failed: ${error.message}",
                        cause = error
                    )
                )
            }

            var response = executeRequest(request, accessToken)

            // If 401, refresh token and retry once
            if (response.status == HttpStatusCode.Unauthorized) {
                accessToken = authRepository.refreshToken().getOrElse { error ->
                    return Result.failure(
                        ApiException(
                            message = "Token refresh failed: ${error.message}",
                            cause = error
                        )
                    )
                }
                response = executeRequest(request, accessToken)
            }

            if (response.status.isSuccess()) {
                Result.success(response.body<ChatResponseDto>())
            } else {
                val errorBody = response.bodyAsText()
                val errorMessage = try {
                    val apiError = json.decodeFromString<ApiErrorDto>(errorBody)
                    apiError.error.message
                } catch (e: Exception) {
                    "API error: ${response.status.value} - $errorBody"
                }
                Result.failure(ApiException(errorMessage, response.status.value))
            }
        } catch (e: Exception) {
            Result.failure(
                ApiException(
                    message = e.message ?: "Unknown network error",
                    cause = e
                )
            )
        }
    }

    private suspend fun executeRequest(request: ChatRequestDto, accessToken: String): HttpResponse {
        return httpClient.post("api/v1/chat/completions") {
            header(HttpHeaders.Authorization, "Bearer $accessToken")
            setBody(request)
        }
    }

    /**
     * Отправляет запрос для суммаризации сообщений с оптимизированными параметрами
     */
    suspend fun sendMessageForSummary(messages: List<ChatMessageDto>): Result<ChatResponseDto> {
        return try {
            val settings = settingsRepository.getSettings()

            val request = ChatRequestDto(
                model = settings.model.apiName,
                messages = messages,
                maxTokens = 200,  // Короткое резюме
                temperature = 0.3 // Более детерминированный результат
            )

            var accessToken = authRepository.getValidToken().getOrElse { error ->
                return Result.failure(
                    ApiException(
                        message = "Authentication failed: ${error.message}",
                        cause = error
                    )
                )
            }

            var response = executeRequest(request, accessToken)

            if (response.status == HttpStatusCode.Unauthorized) {
                accessToken = authRepository.refreshToken().getOrElse { error ->
                    return Result.failure(
                        ApiException(
                            message = "Token refresh failed: ${error.message}",
                            cause = error
                        )
                    )
                }
                response = executeRequest(request, accessToken)
            }

            if (response.status.isSuccess()) {
                Result.success(response.body<ChatResponseDto>())
            } else {
                val errorBody = response.bodyAsText()
                val errorMessage = try {
                    val apiError = json.decodeFromString<ApiErrorDto>(errorBody)
                    apiError.error.message
                } catch (e: Exception) {
                    "API error: ${response.status.value} - $errorBody"
                }
                Result.failure(ApiException(errorMessage, response.status.value))
            }
        } catch (e: Exception) {
            Result.failure(
                ApiException(
                    message = e.message ?: "Unknown network error",
                    cause = e
                )
            )
        }
    }
}

class ApiException(
    message: String,
    val statusCode: Int? = null,
    cause: Throwable? = null
) : Exception(message, cause)

private const val JSON_SYSTEM_PROMPT = """You are a JSON-only response assistant. You MUST respond ONLY with valid JSON in the exact format specified below. No additional text, explanations, or markdown formatting outside the JSON structure.

RESPONSE FORMAT (strict):
{
  "timestamp": "HH.mm.ss dd.MM.yy",
  "question": "<exact user question>",
  "answer": "<your detailed answer as a single string>",
  "tags": ["tag1", "tag2", "tag3", "tag4", "tag5"]
}

RULES:
1. ALWAYS output valid JSON that can be parsed by standard JSON parsers
2. The "timestamp" field must use the current date/time in format "HH.mm.ss dd.MM.yy" (24-hour format)
3. The "question" field must contain the user's original question exactly as asked
4. The "answer" field must be a single string. Escape special characters properly:
   - Use \" for quotes inside the answer
   - Use \n for newlines
   - Use \\ for backslashes
5. The "tags" field must ALWAYS contain exactly 5 relevant tags as an array of strings
6. Tags should be lowercase, single words or short phrases relevant to the question topic
7. Do NOT include markdown code blocks, only raw JSON
8. Do NOT include any text before or after the JSON object
9. Ensure all string values are properly escaped for JSON validity"""

private const val THINKING_MODE_SYSTEM_PROMPT = """Ты вдумчивый ИИ-ассистент, работающий в режиме последовательного сбора информации. Твоя задача - провести структурированный диалог с пользователем, задавая вопросы ПО ОДНОМУ, как это делает врач на приёме или аналитик при сборе требований.

ВАЖНЕЙШИЕ ПРАВИЛА:
1. ВСЕГДА задавай только ОДИН вопрос за раз
2. Жди ответа пользователя перед следующим вопросом
3. Каждый новый вопрос должен учитывать предыдущие ответы
4. Веди себя как внимательный специалист, а не как анкета

АЛГОРИТМ РАБОТЫ:

ШАГ 1 - Первый контакт:
Когда пользователь обращается с запросом, кратко подтверди понимание темы и задай ПЕРВЫЙ уточняющий вопрос.
Формат: "Понял, [краткое описание темы]. [Один конкретный вопрос]?"

ШАГ 2 - Сбор информации:
После каждого ответа пользователя:
- Кратко подтверди получение информации ("Хорошо", "Понятно", "Отлично")
- Задай СЛЕДУЮЩИЙ логичный вопрос, вытекающий из контекста
- Вопросы должны углубляться в тему, а не быть случайными

ШАГ 3 - Завершение сбора:
Когда собрано достаточно информации (обычно 3-5 вопросов), сообщи:
"Спасибо, у меня достаточно информации. Подготовить итоговый ответ?"

ШАГ 4 - Итоговый ответ:
После подтверждения пользователя дай структурированный ответ:
"📋 ИТОГОВЫЙ ОТВЕТ:
[Детальный ответ на основе собранной информации]"

ПРИМЕР ДИАЛОГА:

Пользователь: "Помоги написать функцию"
Ты: "Понял, нужна помощь с написанием функции. На каком языке программирования вы работаете?"

Пользователь: "Python"
Ты: "Хорошо, Python. Какую задачу должна решать эта функция?"

Пользователь: "Сортировка списка"
Ты: "Понятно. Какие данные будут в списке - числа, строки, или объекты?"

И так далее...

ИСКЛЮЧЕНИЯ:
- Если пользователь пишет "без вопросов", "сразу ответь" или подобное - переходи к ответу
- Если запрос очень простой и конкретный - можно ответить сразу"""
