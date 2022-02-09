package com.neaniesoft.vermilion.auth

import android.content.SharedPreferences
import com.neaniesoft.vermilion.auth.entities.AuthToken
import com.neaniesoft.vermilion.auth.entities.DeviceAuthToken
import com.neaniesoft.vermilion.auth.entities.DeviceId
import com.neaniesoft.vermilion.auth.entities.Token
import com.neaniesoft.vermilion.auth.entities.UserAuthToken
import com.neaniesoft.vermilion.auth.errors.InvalidDeviceIdError
import com.neaniesoft.vermilion.auth.errors.UnsuccessfulTokenRequestError
import com.neaniesoft.vermilion.auth.http.AccessTokenService
import java.time.Clock
import java.time.Instant
import java.util.UUID
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
        private const val DEVICE_ID_KEY = "device_id"
        private const val TOKEN_TYPE_KEY = "token_type"
        private const val REFRESH_TOKEN_KEY = "refresh_token"
        private const val TOKEN_TYPE_DEVICE = "device"
        private const val TOKEN_TYPE_USER = "user"
        private const val LOGGED_IN_USER_ID_KEY = "logged_in_user_id"
        private const val LOGGED_IN_USER_AUTH_TOKEN_ID_KEY = "logged_in_user_auth_token_id"
    }

    private enum class TokenType(val value: String) {
        DEVICE("device"),
        USER("user");

        fun from(value: String): TokenType {
            return when (value) {
                DEVICE.value -> {
                    DEVICE
                }
                USER.value -> {
                    USER
                }
                else -> {
                    throw IllegalArgumentException("Could not determine token type from string '$value'")
                }
            }
        }
    }

    override fun getToken(
        accessTokenService: AccessTokenService,
        accessTokenType: AccessTokenType
    ): AuthToken {
        val tokenValue = prefs.getString(TOKEN_KEY, null)
        val expiryTime = Instant.ofEpochMilli(prefs.getLong(EXPIRY_TIME_KEY, 0L)) ?: Instant.EPOCH
        val currentToken =
            when (val tokenType = prefs.getString(TOKEN_TYPE_KEY, TOKEN_TYPE_DEVICE)) {
                TOKEN_TYPE_DEVICE -> {
                    val deviceId = getOrCreateDeviceId()
                    DeviceAuthToken(Token(tokenValue ?: ""), expiryTime, deviceId)
                }
                TOKEN_TYPE_USER -> {
                    val refreshToken = prefs.getString(REFRESH_TOKEN_KEY, null)
                    UserAuthToken(
                        Token(tokenValue ?: ""),
                        expiryTime,
                        refreshToken = refreshToken?.let { Token(it) }
                    )
                }
                else -> throw IllegalStateException("Invalid token type: $tokenType")
            }
        return when (currentToken) {
            is DeviceAuthToken -> {
                if (currentToken.token.value.isBlank() || isTokenExpired(currentToken.expiryTime)) {
                    val token = currentToken.fetchNewToken(accessTokenService)
                    token.save()
                } else {
                    currentToken
                }
            }
            is UserAuthToken -> TODO()
        }
    }

    override fun getCurrentLoggedInUserAccountId(): UUID? {
        return prefs.getString(LOGGED_IN_USER_ID_KEY, null)?.let { UUID.fromString(it) }
    }

    override fun getCurrentLoggedInUserAuthTokenId(): String? {
        return prefs.getString(LOGGED_IN_USER_AUTH_TOKEN_ID_KEY, null)
    }

    private fun isTokenExpired(expiryTime: Instant): Boolean {
        return clock.millis() >= expiryTime.toEpochMilli()
    }

    private fun DeviceAuthToken.fetchNewToken(accessTokenService: AccessTokenService): DeviceAuthToken {
        val response =
            accessTokenService.deviceAccessToken(
                "https://oauth.reddit.com/grants/installed_client",
                deviceId.value
            )
                .execute()
        val body = response.body()
        if (response.isSuccessful && body != null) {
            if (body.deviceId == deviceId.value) {
                return DeviceAuthToken(
                    Token(body.accessToken),
                    clock.instant().plusSeconds(body.expiresInSeconds),
                    deviceId
                )
            } else {
                throw InvalidDeviceIdError("DeviceId ${body.deviceId} does not match stored ID ${deviceId.value}")
            }
        } else {
            throw UnsuccessfulTokenRequestError("Unsuccessful request for token: ${response.code()}")
        }
    }

    private fun DeviceAuthToken.save(): DeviceAuthToken {
        saveToken(this.token, this.expiryTime, TokenType.DEVICE)
        return this
    }

    private object DeviceIdLock

    private fun getOrCreateDeviceId(): DeviceId {
        return synchronized(DeviceIdLock) {
            val deviceId = prefs.getString(DEVICE_ID_KEY, null)
            if (deviceId == null) {
                val newDeviceId = DeviceId(UUID.randomUUID().toString())
                saveDeviceId(newDeviceId)
                newDeviceId
            } else {
                DeviceId(deviceId)
            }
        }
    }

    private fun saveToken(accessToken: Token, expiryTime: Instant, tokenType: TokenType) {
        prefs.edit().putString(TOKEN_KEY, accessToken.value).apply()
        prefs.edit().putLong(EXPIRY_TIME_KEY, expiryTime.toEpochMilli()).apply()
        prefs.edit().putString(TOKEN_TYPE_KEY, tokenType.value).apply()
    }

    private fun saveDeviceId(deviceId: DeviceId) {
        prefs.edit().putString(DEVICE_ID_KEY, deviceId.value).apply()
    }
}
