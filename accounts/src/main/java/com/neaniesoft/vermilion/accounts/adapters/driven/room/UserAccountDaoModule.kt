package com.neaniesoft.vermilion.accounts.adapters.driven.room

import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.useraccount.UserAccountDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UserAccountDaoModule {

    @Provides
    @Singleton
    fun provideUserAccountDao(db: VermilionDatabase): UserAccountDao =
        db.userAccountDao()
}
