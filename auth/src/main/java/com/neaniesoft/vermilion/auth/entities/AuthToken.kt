package com.neaniesoft.vermilion.auth.entities

import java.time.Instant

data class AuthToken(
    val token: Token,
    val expiryTime: Instant,
    val deviceId: DeviceId
)
