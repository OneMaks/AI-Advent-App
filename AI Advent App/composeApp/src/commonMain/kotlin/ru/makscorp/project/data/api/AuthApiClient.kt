package ru.makscorp.project.data.api

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.forms.submitForm
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.parameters
import ru.makscorp.project.data.api.dto.AuthResponseDto
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class AuthApiClient(
    private val httpClient: HttpClient,
    private val authUrl: String,
    private val authorizationKey: String,
    private val scope: String = "GIGACHAT_API_PERS"
) {
    @OptIn(ExperimentalUuidApi::class)
    suspend fun getAccessToken(): Result<AuthResponseDto> {
        return try {
            val requestId = Uuid.random().toString()

            val response = httpClient.submitForm(
                url = "$authUrl/api/v2/oauth",
                formParameters = parameters {
                    append("scope", scope)
                }
            ) {
                header("Authorization", "Basic $authorizationKey")
                header("RqUID", requestId)
                header("Accept", "application/json")
            }

            if (response.status == HttpStatusCode.OK) {
                Result.success(response.body<AuthResponseDto>())
            } else {
                val errorBody = response.bodyAsText()
                Result.failure(AuthException("Auth failed: ${response.status} - $errorBody"))
            }
        } catch (e: Exception) {
            Result.failure(AuthException("Auth request failed: ${e.message}", e))
        }
    }
}

class AuthException(message: String, cause: Throwable? = null) : Exception(message, cause)
