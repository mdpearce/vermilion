package com.neaniesoft.vermilion.accounts.adapters.driven.room

import com.neaniesoft.vermilion.accounts.domain.ports.UserAccountRecordRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class UserAccountRecordModule {
    @Binds
    abstract fun provideUserAccountRecordRepository(repo: UserAccountRoomRecordRepository): UserAccountRecordRepository
}
