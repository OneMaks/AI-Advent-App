package ru.makscorp.project.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import ru.makscorp.project.data.api.AuthApiClient
import ru.makscorp.project.data.storage.StoredToken
import ru.makscorp.project.data.storage.TokenStorage
import ru.makscorp.project.domain.model.AuthState
import ru.makscorp.project.domain.repository.AuthRepository

class AuthRepositoryImpl(
    private val authApiClient: AuthApiClient,
    private val tokenStorage: TokenStorage
) : AuthRepository {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthorized)
    override val authState: Flow<AuthState> = _authState.asStateFlow()

    private val mutex = Mutex()

    private companion object {
        const val TOKEN_REFRESH_MARGIN_MS = 60_000L // Refresh 1 minute before expiration
    }

    init {
        loadStoredToken()
    }

    private fun loadStoredToken() {
        val storedToken = tokenStorage.getToken()
        if (storedToken != null && !isTokenExpired(storedToken.expiresAt)) {
            _authState.value = AuthState.Authorized(
                accessToken = storedToken.accessToken,
                expiresAt = storedToken.expiresAt
            )
        }
    }

    override suspend fun authenticate(): Result<String> = mutex.withLock {
        _authState.value = AuthState.Loading

        return authApiClient.getAccessToken()
            .map { response ->
                val token = StoredToken(
                    accessToken = response.accessToken,
                    expiresAt = response.expiresAt
                )
                tokenStorage.saveToken(token)
                _authState.value = AuthState.Authorized(
                    accessToken = response.accessToken,
                    expiresAt = response.expiresAt
                )
                response.accessToken
            }
            .onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "Authentication failed")
            }
    }

    override suspend fun getValidToken(): Result<String> = mutex.withLock {
        val currentState = _authState.value

        if (currentState is AuthState.Authorized && !shouldRefreshToken(currentState.expiresAt)) {
            return Result.success(currentState.accessToken)
        }

        // Token expired or about to expire, refresh it
        return refreshTokenInternal()
    }

    private suspend fun refreshTokenInternal(): Result<String> {
        _authState.value = AuthState.Loading

        return authApiClient.getAccessToken()
            .map { response ->
                val token = StoredToken(
                    accessToken = response.accessToken,
                    expiresAt = response.expiresAt
                )
                tokenStorage.saveToken(token)
                _authState.value = AuthState.Authorized(
                    accessToken = response.accessToken,
                    expiresAt = response.expiresAt
                )
                response.accessToken
            }
            .onFailure { error ->
                _authState.value = AuthState.Error(error.message ?: "Token refresh failed")
            }
    }

    override suspend fun refreshToken(): Result<String> = mutex.withLock {
        return refreshTokenInternal()
    }

    override fun logout() {
        tokenStorage.clearToken()
        _authState.value = AuthState.Unauthorized
    }

    private fun currentTimeMs(): Long = Clock.System.now().toEpochMilliseconds()

    private fun isTokenExpired(expiresAt: Long): Boolean {
        return currentTimeMs() >= expiresAt * 1000
    }

    private fun shouldRefreshToken(expiresAt: Long): Boolean {
        return currentTimeMs() >= (expiresAt * 1000 - TOKEN_REFRESH_MARGIN_MS)
    }
}
