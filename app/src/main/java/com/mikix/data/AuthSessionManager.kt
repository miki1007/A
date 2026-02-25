package com.mikix.data

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthSessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val authApi: AuthApi
) {
    private val accessTokenKey = stringPreferencesKey("access_token")
    private val refreshTokenKey = stringPreferencesKey("refresh_token")
    private val accessExpiryKey = longPreferencesKey("access_expiry_epoch_ms")

    suspend fun currentAccessToken(): String? = dataStore.data.first()[accessTokenKey]

    suspend fun saveTokens(auth: AuthResponse) {
        val expiry = System.currentTimeMillis() + (auth.expiresInSec * 1000)
        dataStore.edit {
            it[accessTokenKey] = auth.accessToken
            it[refreshTokenKey] = auth.refreshToken
            it[accessExpiryKey] = expiry
        }
    }

    suspend fun ensureValidAccessToken(): String? {
        val prefs = dataStore.data.first()
        val token = prefs[accessTokenKey]
        val expiry = prefs[accessExpiryKey] ?: 0L
        if (!token.isNullOrBlank() && expiry > System.currentTimeMillis() + 30_000L) return token

        val refresh = prefs[refreshTokenKey] ?: return token
        val newAuth = authApi.refresh(RefreshTokenRequest(refresh))
        saveTokens(newAuth)
        return newAuth.accessToken
    }
}
