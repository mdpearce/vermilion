package com.vermilion.auth

import android.content.SharedPreferences
import java.time.Clock
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AuthorizationStoreImpl @Inject constructor(
    @Named(AuthModule.AUTH_PREFS) private val prefs: SharedPreferences,
    private val clock: Clock
) : AuthorizationStore {

    companion object {
        private const val TOKEN_KEY = "token"
        private const val EXPIRY_TIME_KEY = "expiry_time"
    }

    override fun getToken(): TokenResponse {
        val tokenValue = prefs.getString(TOKEN_KEY, null)
        return when {
            tokenValue.isNullOrBlank() -> {
                TokenMissing
            }
            clock.millis() >= prefs.getLong(EXPIRY_TIME_KEY, 0L) -> {
                TokenExpired
            }
            else -> {
                AuthorizationToken(Token(tokenValue))
            }
        }
    }
}
