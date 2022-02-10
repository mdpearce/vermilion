package com.neaniesoft.vermilion.auth

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.net.toUri
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import com.neaniesoft.vermilion.utils.logger
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import net.openid.appauth.AuthState
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import java.time.Clock
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class AuthModule {
    companion object {
        const val AUTH_PREFS_FILENAME = "auth_prefs_filename"
        const val AUTH_PREFS_FILENAME_VALUE = "auth"
        const val AUTH_PREFS = "auth_prefs"
    }

    @Provides
    @Named(AUTH_PREFS_FILENAME)
    fun provideAuthPrefsFilename(): String = AUTH_PREFS_FILENAME_VALUE

    @Provides
    @Named(AUTH_PREFS)
    fun provideEncryptedSharedPrefs(
        @Named(AUTH_PREFS_FILENAME) authPrefsFilename: String,
        @ApplicationContext context: Context
    ): SharedPreferences {
        val masterKey = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)

        return EncryptedSharedPreferences.create(
            authPrefsFilename,
            masterKey,
            context,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    @Provides
    fun provideSystemClock(): Clock {
        return Clock.systemUTC()
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthStoreModule {
    @Binds
    abstract fun bindAuthStoreImpl(authorizationStoreImpl: AuthorizationStoreImpl): AuthorizationStore
}

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
