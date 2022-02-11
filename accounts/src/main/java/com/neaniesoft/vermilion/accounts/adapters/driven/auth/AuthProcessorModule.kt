package com.neaniesoft.vermilion.accounts.adapters.driven.auth

import com.neaniesoft.vermilion.accounts.domain.ports.AuthProcessor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthProcessorModule {
    @Binds
    abstract fun provideAuthProcessor(processor: AppAuthProcessor): AuthProcessor
}
