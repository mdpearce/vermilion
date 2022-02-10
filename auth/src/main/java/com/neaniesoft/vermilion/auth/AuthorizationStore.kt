package com.neaniesoft.vermilion.auth

import com.neaniesoft.vermilion.auth.entities.AuthToken
import com.neaniesoft.vermilion.auth.http.AccessTokenService
import java.util.UUID

interface AuthorizationStore {
    fun getToken(
        accessTokenService: AccessTokenService,
        accessTokenType: AccessTokenType = AccessTokenType.Device
    ): AuthToken

    fun getCurrentLoggedInUserAccountId(): UUID?

    fun getCurrentLoggedInUserAuthTokenId(): String?

    fun setLoggedInUserId(id: UUID)
    fun saveAuthState(stateAsString: String)
}

sealed class AccessTokenType {
    object Device : AccessTokenType()
    data class User(val accountId: UUID) : AccessTokenType()
}
