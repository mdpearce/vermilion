package com.neaniesoft.vermilion.accounts.adapters.driven.auth

import com.neaniesoft.vermilion.accounts.domain.entities.AuthResponse
import com.neaniesoft.vermilion.accounts.domain.ports.AuthProcessor
import com.neaniesoft.vermilion.auth.AuthStateProvider
import com.neaniesoft.vermilion.auth.AuthorizationStore
import com.neaniesoft.vermilion.utils.logger
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.ClientSecretBasic
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuthProcessor @Inject constructor(
    private val authStateProvider: AuthStateProvider,
    private val authorizationStore: AuthorizationStore,
    private val authorizationService: AuthorizationService
) : AuthProcessor {
    private val logger by logger()

    override suspend fun updateAuthState(authResponse: AuthResponse<*, *>) {
        logger.debugIfEnabled { "Received auth response. Response: ${authResponse.response}, Exception: ${authResponse.exception}" }
        logger.debugIfEnabled { "Fetching token" }
        if (authResponse.exception != null) {
            logger.errorIfEnabled { "Exception while performing authorization" }
            return
        }
        val authorizationResponse = authResponse.response as? AuthorizationResponse
        val authorizationException = authResponse.exception as? AuthorizationException

        authStateProvider.updateAuthState(authorizationResponse, authorizationException)
        val resp =
            requireNotNull(authorizationResponse) { "No exception, but null response" }

        authorizationService.performTokenRequest(
            resp.createTokenExchangeRequest(),
            ClientSecretBasic("")
        ) { tokenResponse, tokenException ->
            logger.debugIfEnabled { "Token response: $tokenResponse ; Token exception: $tokenException" }
            authStateProvider.updateAuthState(tokenResponse, tokenException)
            authorizationStore.saveAuthState(authStateProvider.serialize())
        }
    }

    override fun invalidateAuthState() {
        authStateProvider.invalidateAuthState()
        authorizationStore.saveAuthState(null)
    }
}
