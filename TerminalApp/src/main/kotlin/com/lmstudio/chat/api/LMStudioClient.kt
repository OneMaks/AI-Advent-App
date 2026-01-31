package com.lmstudio.chat.api

import com.lmstudio.chat.api.models.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.serialization.json.Json
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Client for communicating with LM Studio's OpenAI-compatible API.
 */
class LMStudioClient(
    private val baseUrl: String = "http://localhost:1234/v1",
    private val connectTimeoutSeconds: Long = 30,
    private val readTimeoutSeconds: Long = 300,
    private val writeTimeoutSeconds: Long = 60
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(connectTimeoutSeconds, TimeUnit.SECONDS)
        .readTimeout(readTimeoutSeconds, TimeUnit.SECONDS)
        .writeTimeout(writeTimeoutSeconds, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    /**
     * Checks if the LM Studio server is available.
     */
    suspend fun isServerAvailable(): Boolean {
        return try {
            val response = executeRequest(
                Request.Builder()
                    .url("$baseUrl/models")
                    .get()
                    .build()
            )
            response.isSuccessful.also { response.close() }
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Gets the list of available models from LM Studio.
     */
    suspend fun getModels(): Result<ModelsResponse> {
        return try {
            val request = Request.Builder()
                .url("$baseUrl/models")
                .addHeader("Authorization", "Bearer lm-studio")
                .get()
                .build()

            val response = executeRequest(request)
            val body = response.body?.string()
                ?: return Result.failure(IOException("Empty response body"))

            if (!response.isSuccessful) {
                return Result.failure(IOException("Request failed: ${response.code} - $body"))
            }

            val modelsResponse = json.decodeFromString<ModelsResponse>(body)
            Result.success(modelsResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sends a chat request and returns the complete response.
     */
    suspend fun chat(chatRequest: ChatRequest): Result<ChatResponse> {
        return try {
            val requestBody = json.encodeToString(ChatRequest.serializer(), chatRequest.copy(stream = false))

            val request = Request.Builder()
                .url("$baseUrl/chat/completions")
                .addHeader("Authorization", "Bearer lm-studio")
                .addHeader("Content-Type", "application/json")
                .post(requestBody.toRequestBody(jsonMediaType))
                .build()

            val response = executeRequest(request)
            val body = response.body?.string()
                ?: return Result.failure(IOException("Empty response body"))

            if (!response.isSuccessful) {
                return Result.failure(IOException("Request failed: ${response.code} - $body"))
            }

            val chatResponse = json.decodeFromString<ChatResponse>(body)
            Result.success(chatResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Sends a chat request with streaming enabled and returns a Flow of content chunks.
     */
    fun chatStream(chatRequest: ChatRequest): Flow<StreamResult> = flow {
        val requestBody = json.encodeToString(ChatRequest.serializer(), chatRequest.copy(stream = true))

        val request = Request.Builder()
            .url("$baseUrl/chat/completions")
            .addHeader("Authorization", "Bearer lm-studio")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "text/event-stream")
            .post(requestBody.toRequestBody(jsonMediaType))
            .build()

        val response = try {
            executeRequest(request)
        } catch (e: Exception) {
            emit(StreamResult.Error(e))
            return@flow
        }

        if (!response.isSuccessful) {
            val errorBody = response.body?.string() ?: "Unknown error"
            emit(StreamResult.Error(IOException("Request failed: ${response.code} - $errorBody")))
            response.close()
            return@flow
        }

        val inputStream = response.body?.byteStream()
        if (inputStream == null) {
            emit(StreamResult.Error(IOException("No response body")))
            response.close()
            return@flow
        }

        val reader = BufferedReader(InputStreamReader(inputStream))
        try {
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                val currentLine = line ?: continue

                if (currentLine.isEmpty()) continue

                if (currentLine.startsWith("data: ")) {
                    val data = currentLine.removePrefix("data: ").trim()

                    if (data == "[DONE]") {
                        emit(StreamResult.Done)
                        break
                    }

                    try {
                        val chunk = json.decodeFromString<StreamChunk>(data)
                        val content = chunk.choices.firstOrNull()?.delta?.content
                        if (content != null) {
                            emit(StreamResult.Content(content))
                        }

                        val finishReason = chunk.choices.firstOrNull()?.finishReason
                        if (finishReason != null) {
                            emit(StreamResult.Finished(finishReason))
                        }
                    } catch (e: Exception) {
                        // Skip malformed chunks
                    }
                }
            }
        } catch (e: Exception) {
            emit(StreamResult.Error(e))
        } finally {
            reader.close()
            response.close()
        }
    }.flowOn(Dispatchers.IO)

    private suspend fun executeRequest(request: Request): Response =
        suspendCancellableCoroutine { continuation ->
            val call = client.newCall(request)

            continuation.invokeOnCancellation {
                call.cancel()
            }

            call.enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    if (continuation.isActive) {
                        continuation.resumeWithException(e)
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    if (continuation.isActive) {
                        continuation.resume(response)
                    }
                }
            })
        }

    fun close() {
        client.dispatcher.executorService.shutdown()
        client.connectionPool.evictAll()
    }
}

/**
 * Represents the result of a streaming response.
 */
sealed class StreamResult {
    data class Content(val text: String) : StreamResult()
    data class Finished(val reason: String) : StreamResult()
    data class Error(val exception: Exception) : StreamResult()
    data object Done : StreamResult()
}
