package ru.makscorp.project.domain.model

sealed class AuthState {
    data object Unauthorized : AuthState()
    data object Loading : AuthState()
    data class Authorized(val accessToken: String, val expiresAt: Long) : AuthState()
    data class Error(val message: String) : AuthState()
}
