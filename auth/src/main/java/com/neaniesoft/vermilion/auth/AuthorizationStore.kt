package com.neaniesoft.vermilion.auth

import com.github.michaelbull.result.Result
import com.neaniesoft.vermilion.auth.entities.AuthToken
import com.neaniesoft.vermilion.auth.errors.AuthError
import com.neaniesoft.vermilion.auth.http.AccessTokenService
import java.util.UUID

interface AuthorizationStore {
    fun getDeviceToken(
        accessTokenService: AccessTokenService
    ): Result<AuthToken, AuthError>

    fun getCurrentLoggedInUserAccountId(): UUID?
    fun setLoggedInUserId(id: UUID?)
    fun saveAuthState(stateAsString: String?)
    fun getAuthState(): String
}
