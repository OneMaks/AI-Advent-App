package ru.makscorp.project.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.makscorp.project.domain.model.AuthState

interface AuthRepository {
    val authState: Flow<AuthState>

    suspend fun authenticate(): Result<String>
    suspend fun getValidToken(): Result<String>
    suspend fun refreshToken(): Result<String>
    fun logout()
}
