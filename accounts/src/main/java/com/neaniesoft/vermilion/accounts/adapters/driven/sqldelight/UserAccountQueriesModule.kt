package com.neaniesoft.vermilion.accounts.adapters.driven.sqldelight

import com.neaniesoft.vermilion.db.Database
import com.neaniesoft.vermilion.db.UserAccountQueries
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class UserAccountQueriesModule {

    @Provides
    @Singleton
    fun provideUserAccountQueries(db: Database): UserAccountQueries =
        db.userAccountQueries
}
