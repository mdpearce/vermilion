package com.neaniesoft.vermilion.auth

import com.neaniesoft.vermilion.auth.entities.AuthToken
import com.neaniesoft.vermilion.auth.http.AccessTokenService

interface AuthorizationStore {
    fun getToken(accessTokenService: AccessTokenService): AuthToken
}
