package com.neaniesoft.vermilion.accounts.adapters.driven.auth

import com.neaniesoft.vermilion.accounts.domain.entities.AuthResponse
import com.neaniesoft.vermilion.accounts.domain.ports.AuthProcessor
import com.neaniesoft.vermilion.auth.AuthorizationStore
import com.neaniesoft.vermilion.utils.logger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppAuthProcessor @Inject constructor(
    private val authState: AuthState,
    private val authorizationStore: AuthorizationStore
) : AuthProcessor {
    private val logger by logger()

    override suspend fun updateAuthState(authResponse: AuthResponse<*, *>) {
        logger.debugIfEnabled { "Received auth response. Response: ${authResponse.response}, Exception: ${authResponse.exception}" }
        authState.update(
            authResponse.response as? AuthorizationResponse,
            authResponse.exception as? AuthorizationException
        )
        authorizationStore.saveAuthState(authState.jsonSerializeString())
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthProcessorModule {
    @Binds
    abstract fun provideAuthProcessor(processor: AppAuthProcessor): AuthProcessor
}
