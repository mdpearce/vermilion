package com.neaniesoft.vermilion.auth

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthStoreModule {
    @Binds
    abstract fun bindAuthStoreImpl(authorizationStoreImpl: AuthorizationStoreImpl): AuthorizationStore
}
