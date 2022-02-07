package com.vermilion.auth

import com.vermilion.auth.entities.AuthToken
import com.vermilion.auth.http.AccessTokenService

interface AuthorizationStore {
    fun getToken(accessTokenService: AccessTokenService): AuthToken
}
