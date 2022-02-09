package com.neaniesoft.vermilion.accounts.domain.entities

import java.util.UUID

data class UserAccount(
    val id: UserAccountId,
    val authTokenId: AuthTokenId
)

@JvmInline
value class UserAccountId(val value: UUID)

@JvmInline
value class AuthTokenId(val value: String)