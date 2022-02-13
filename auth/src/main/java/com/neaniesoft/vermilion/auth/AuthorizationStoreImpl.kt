package com.neaniesoft.vermilion.auth

import android.content.SharedPreferences
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.runCatching
import com.github.michaelbull.result.toErrorIf
import com.neaniesoft.vermilion.auth.entities.AuthToken
import com.neaniesoft.vermilion.auth.entities.DeviceId
import com.neaniesoft.vermilion.auth.entities.Token
import com.neaniesoft.vermilion.auth.errors.AuthError
import com.neaniesoft.vermilion.auth.errors.AuthTokenServiceIoError
import com.neaniesoft.vermilion.auth.errors.EmptyBody
import com.neaniesoft.vermilion.auth.errors.InvalidDeviceId
import com.neaniesoft.vermilion.auth.errors.UnhandledError
import com.neaniesoft.vermilion.auth.errors.UnsuccessfulTokenRequest
import com.neaniesoft.vermilion.auth.http.AccessTokenService
import java.io.IOException
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
        private const val LOGGED_IN_USER_ID_KEY = "logged_in_user_id"
        private const val AUTH_STATE_KEY = "auth_state"
    }

    override fun getAuthState(): String {
        return prefs.getString(AUTH_STATE_KEY, "") ?: ""
    }

    override fun getDeviceToken(accessTokenService: AccessTokenService): Result<AuthToken, AuthError> {
        val tokenValue = prefs.getString(TOKEN_KEY, null)
        val expiryTime = Instant.ofEpochMilli(prefs.getLong(EXPIRY_TIME_KEY, 0L)) ?: Instant.EPOCH
        val deviceId = getOrCreateDeviceId()

        val currentToken = AuthToken(Token(tokenValue ?: ""), expiryTime, deviceId)
        return if (currentToken.token.value.isBlank() || isTokenExpired(currentToken.expiryTime)) {
            currentToken.fetchNewToken(accessTokenService)
                .onSuccess { token -> token.save() }
        } else {
            Ok(currentToken)
        }
    }

    override fun getCurrentLoggedInUserAccountId(): UUID? {
        return prefs.getString(LOGGED_IN_USER_ID_KEY, null)?.let { UUID.fromString(it) }
    }

    override fun setLoggedInUserId(id: UUID?) {
        prefs.edit().putString(LOGGED_IN_USER_ID_KEY, id?.toString()).apply()
    }

    private fun isTokenExpired(expiryTime: Instant): Boolean {
        return clock.millis() >= expiryTime.toEpochMilli()
    }

    override fun saveAuthState(stateAsString: String?) {
        prefs.edit().putString(AUTH_STATE_KEY, stateAsString).apply()
    }

    private fun AuthToken.fetchNewToken(accessTokenService: AccessTokenService): Result<AuthToken, AuthError> {
        return runCatching {
            accessTokenService.deviceAccessToken(
                "https://oauth.reddit.com/grants/installed_client",
                deviceId.value
            ).execute()
        }.mapError {
            when (it) {
                is IOException -> AuthTokenServiceIoError(it)
                else -> UnhandledError(it)
            }
        }.toErrorIf({ !it.isSuccessful }) {
            UnsuccessfulTokenRequest(it.code())
        }.andThen {
            runCatching { requireNotNull(it.body()) }
                .mapError { EmptyBody }
        }.toErrorIf({ it.deviceId != deviceId.value }) {
            InvalidDeviceId
        }.map {
            AuthToken(
                Token(it.accessToken),
                clock.instant().plusSeconds(it.expiresInSeconds),
                deviceId
            )
        }
    }

    private fun AuthToken.save(): AuthToken {
        saveToken(this.token, this.expiryTime)
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

    private fun saveToken(accessToken: Token, expiryTime: Instant) {
        prefs.edit().putString(TOKEN_KEY, accessToken.value).apply()
        prefs.edit().putLong(EXPIRY_TIME_KEY, expiryTime.toEpochMilli()).apply()
    }

    private fun saveDeviceId(deviceId: DeviceId) {
        prefs.edit().putString(DEVICE_ID_KEY, deviceId.value).apply()
    }
}
