package ru.makscorp.project.data.storage

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

actual class TokenStorage(context: Context) {

    private val sharedPreferences: SharedPreferences = try {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        EncryptedSharedPreferences.create(
            context,
            "auth_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    } catch (e: Exception) {
        Log.w("TokenStorage", "EncryptedSharedPreferences failed, using regular prefs", e)
        context.getSharedPreferences("auth_prefs_fallback", Context.MODE_PRIVATE)
    }

    actual fun saveToken(token: StoredToken) {
        sharedPreferences.edit()
            .putString(KEY_ACCESS_TOKEN, token.accessToken)
            .putLong(KEY_EXPIRES_AT, token.expiresAt)
            .apply()
    }

    actual fun getToken(): StoredToken? {
        val accessToken = sharedPreferences.getString(KEY_ACCESS_TOKEN, null) ?: return null
        val expiresAt = sharedPreferences.getLong(KEY_EXPIRES_AT, 0L)
        return StoredToken(accessToken, expiresAt)
    }

    actual fun clearToken() {
        sharedPreferences.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_EXPIRES_AT)
            .apply()
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_EXPIRES_AT = "expires_at"
    }
}
