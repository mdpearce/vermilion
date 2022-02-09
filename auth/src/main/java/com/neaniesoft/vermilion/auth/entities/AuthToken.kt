package com.neaniesoft.vermilion.auth.entities

import java.time.Instant

sealed class AuthToken {
    abstract val token: Token
}

data class UserAuthToken(override val token: Token, val expiryTime: Instant, val refreshToken: Token? = null) :
    AuthToken()

data class DeviceAuthToken(override val token: Token, val expiryTime: Instant, val deviceId: DeviceId) : AuthToken()
