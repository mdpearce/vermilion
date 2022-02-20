package com.neaniesoft.vermilion.accounts.adapters.driven.auth

import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.neaniesoft.vermilion.accounts.domain.entities.AccountError
import com.neaniesoft.vermilion.accounts.domain.entities.AuthResponse
import com.neaniesoft.vermilion.accounts.domain.entities.AuthorizationError
import com.neaniesoft.vermilion.accounts.domain.ports.AuthProcessor
import com.neaniesoft.vermilion.accounts.domain.ports.Updated
import com.neaniesoft.vermilion.auth.AuthStateProvider
import com.neaniesoft.vermilion.auth.AuthorizationStore
import com.neaniesoft.vermilion.utils.logger
import kotlinx.coroutines.CompletableDeferred
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

    override suspend fun updateAuthState(authResponse: AuthResponse<AuthorizationResponse, AuthorizationException>): Result<Updated, AccountError> {
        logger.debugIfEnabled { "Received auth response. Response: ${authResponse.response}, Exception: ${authResponse.exception}" }
        logger.debugIfEnabled { "Fetching token" }
        if (authResponse.exception != null) {
            logger.errorIfEnabled { "Exception while performing authorization" }
            return Err(AuthorizationError(authResponse.exception))
        }
        val authorizationResponse = authResponse.response
        val authorizationException = authResponse.exception

        authStateProvider.updateAuthState(authorizationResponse, authorizationException)
        val resp = authorizationResponse
            ?: return Err(AuthorizationError(IllegalStateException("No exception, but null response")))

        val updateResult = CompletableDeferred<Result<Updated, Throwable>>(null)
        authorizationService.performTokenRequest(
            resp.createTokenExchangeRequest(),
            ClientSecretBasic("")
        ) { tokenResponse, tokenException ->
            logger.debugIfEnabled { "Token response: $tokenResponse ; Token exception: $tokenException" }
            val authStateResult = com.github.michaelbull.result.runCatching {
                authStateProvider.updateAuthState(tokenResponse, tokenException)
                authorizationStore.saveAuthState(authStateProvider.serialize())
            }.map { Updated }
            updateResult.complete(authStateResult)
        }
        val completedResult = updateResult.await()
        logger.debugIfEnabled { "Auth update complete. $completedResult" }
        return completedResult.mapError { AuthorizationError(it) }
    }

    override fun invalidateAuthState() {
        authStateProvider.invalidateAuthState()
        authorizationStore.saveAuthState(null)
    }

    override fun isAuthorized(): Boolean {
        return authStateProvider.authState().isAuthorized
    }
}
