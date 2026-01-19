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
import ru.makscorp.project.domain.repository.AuthRepository

class ChatApiClient(
    private val httpClient: HttpClient,
    private val authRepository: AuthRepository,
    private val model: String
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun sendMessage(messages: List<ChatMessageDto>): Result<ChatResponseDto> {
        return try {
            val request = ChatRequestDto(
                model = model,
                messages = messages,
                maxTokens = 2048,
                temperature = 0.7
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
