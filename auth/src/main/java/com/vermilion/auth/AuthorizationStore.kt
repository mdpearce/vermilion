package com.vermilion.auth

import java.time.Instant

interface AuthorizationStore {
    fun getToken(accessTokenService: AccessTokenService): TokenResponse
}

sealed class TokenResponse
data class UserAuthorizationToken(val token: Token, val expiryTime: Instant, val refreshToken: Token? = null) : TokenResponse()
data class DeviceAuthorizationToken(val token: Token, val expiryTime: Instant, val deviceId: DeviceId) : TokenResponse()

@JvmInline
value class Token(val value: String)

@JvmInline
value class DeviceId(val value: String)