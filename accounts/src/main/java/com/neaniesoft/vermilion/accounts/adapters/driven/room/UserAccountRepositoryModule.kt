package com.neaniesoft.vermilion.accounts.adapters.driven.room

import com.neaniesoft.vermilion.accounts.adapters.driven.sqldelight.UserAccountSqlDelightRepository
import com.neaniesoft.vermilion.accounts.domain.ports.UserAccountRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UserAccountRepositoryModule {
//    @Binds
//    abstract fun provideUserAccountRecordRepository(repo: UserAccountRoomRepository): UserAccountRepository

    @Binds
    abstract fun provideUserAccountRepository(repo: UserAccountSqlDelightRepository): UserAccountRepository
}
