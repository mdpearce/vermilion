package com.vermilion.auth

import java.time.Instant

interface AuthorizationStore {
    fun getToken(accessTokenService: AccessTokenService): AuthToken
}

sealed class AuthToken {
    abstract val token: Token
}

data class UserAuthToken(override val token: Token, val expiryTime: Instant, val refreshToken: Token? = null) :
    AuthToken()

data class DeviceAuthToken(override val token: Token, val expiryTime: Instant, val deviceId: DeviceId) : AuthToken()

@JvmInline
value class Token(val value: String)

@JvmInline
value class DeviceId(val value: String)