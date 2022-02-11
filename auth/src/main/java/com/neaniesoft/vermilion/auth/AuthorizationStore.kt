package com.neaniesoft.vermilion.auth

import com.neaniesoft.vermilion.auth.entities.AuthToken
import com.neaniesoft.vermilion.auth.http.AccessTokenService
import java.util.UUID

interface AuthorizationStore {
    fun getDeviceToken(
        accessTokenService: AccessTokenService
    ): AuthToken

    fun getCurrentLoggedInUserAccountId(): UUID?
    fun setLoggedInUserId(id: UUID?)
    fun saveAuthState(stateAsString: String?)
    fun getAuthState(): String
}
