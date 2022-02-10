package com.neaniesoft.vermilion.accounts.domain.entities

import java.util.UUID

data class UserAccount(
    val id: UserAccountId,
    val username: UserName
)

@JvmInline
value class UserAccountId(val value: UUID)

@JvmInline
value class AuthTokenId(val value: String)

data class AuthResponse<T>(val response: T)

@JvmInline
value class UserName(val value: String)
