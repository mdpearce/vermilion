package com.neaniesoft.vermilion.accounts.adapters.driving.ui

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.android.scopes.ActivityRetainedScoped

@Module
@InstallIn(ActivityRetainedComponent::class)
abstract class AuthUiProviderModule {
    @Binds
    @ActivityRetainedScoped
    abstract fun provideAuthUiProvider(provider: AppAuthUiProvider): AuthUiProvider
}
