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
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
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
        "https://www.reddit.com/api/v1/authorize".toUri()

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
            logger.debugIfEnabled { "Empty auth state, creating unauthorized state"}
            AuthState(configuration)
        } else {
            logger.debugIfEnabled { "Got state, deserializing" }
            AuthState.jsonDeserialize(serializedState)
        }
    }
}
