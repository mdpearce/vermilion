package com.neaniesoft.vermilion.auth

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import com.neaniesoft.vermilion.utils.logger
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.TokenResponse
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AppAuthModule {
    companion object {
        private const val AUTHORIZATION_URI = "authorization_uri"
        private const val TOKEN_URI = "token_uri"
    }

    private val logger by logger()

    @Provides
    fun provideAuthorizationService(@ApplicationContext context: Context): AuthorizationService =
        AuthorizationService(context)

    @Provides
    fun configuration(
        @Named(AUTHORIZATION_URI) authorizationUri: Uri,
        @Named(TOKEN_URI) tokenUri: Uri
    ) = AuthorizationServiceConfiguration(
        authorizationUri,
        tokenUri
    )

    @Named(AUTHORIZATION_URI)
    @Provides
    fun provideAuthorizationUri(): Uri =
        "https://www.reddit.com/api/v1/authorize.compact".toUri()

    @Named(TOKEN_URI)
    @Provides
    fun provideTokenUri(): Uri =
        "https://www.reddit.com/api/v1/access_token".toUri()

    @Provides
    @Singleton
    fun provideAuthState(
        configuration: AuthorizationServiceConfiguration,
        authorizationStore: AuthorizationStore
    ): AuthState {
        val serializedState = authorizationStore.getAuthState()
        return if (serializedState.isEmpty()) {
            logger.debugIfEnabled { "Empty auth state, creating unauthorized state" }
            AuthState(configuration)
        } else {
            logger.debugIfEnabled { "Got state, deserializing" }
            AuthState.jsonDeserialize(serializedState)
        }
    }
}

@Singleton
class AuthStateProvider @Inject constructor(
    private val configuration: AuthorizationServiceConfiguration,
    private val authorizationStore: AuthorizationStore
) {
    private var authState: AuthState = AuthState()

    fun authState(): AuthState {
        return authState
    }

    fun updateAuthState(response: AuthorizationResponse?, exception: AuthorizationException?) {
        authState.update(response, exception)
    }

    fun updateAuthState(response: TokenResponse?, exception: AuthorizationException?) {
        authState.update(response, exception)
    }

    fun deserializeFrom(serializedState: String) {
        authState = AuthState.jsonDeserialize(serializedState)
    }

    fun serialize(): String {
        return authState.jsonSerializeString()
    }

    fun invalidateAuthState() {
        authState = AuthState()
    }
}
