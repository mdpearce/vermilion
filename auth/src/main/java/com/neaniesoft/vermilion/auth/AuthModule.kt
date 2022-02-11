package com.neaniesoft.vermilion.auth

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Named

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
