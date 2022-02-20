package com.neaniesoft.vermilion.accounts.domain.ports

import com.github.michaelbull.result.Result
import com.neaniesoft.vermilion.accounts.domain.entities.AccountError
import com.neaniesoft.vermilion.accounts.domain.entities.AuthResponse
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse

interface AuthProcessor {
    suspend fun updateAuthState(authResponse: AuthResponse<AuthorizationResponse, AuthorizationException>): Result<Updated, AccountError>
    fun invalidateAuthState()
}

object Updated
