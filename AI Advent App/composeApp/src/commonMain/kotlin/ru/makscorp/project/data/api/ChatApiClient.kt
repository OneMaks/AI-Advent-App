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

            // Determine system prompt based on output format
            val systemPrompt = when (settings.outputFormat) {
                OutputFormat.JSON -> JSON_SYSTEM_PROMPT
                OutputFormat.NONE -> settings.systemPrompt
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
