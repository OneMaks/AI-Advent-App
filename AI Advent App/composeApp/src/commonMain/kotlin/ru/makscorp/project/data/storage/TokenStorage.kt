package ru.makscorp.project.data.storage

data class StoredToken(
    val accessToken: String,
    val expiresAt: Long
)

expect class TokenStorage {
    fun saveToken(token: StoredToken)
    fun getToken(): StoredToken?
    fun clearToken()
}
