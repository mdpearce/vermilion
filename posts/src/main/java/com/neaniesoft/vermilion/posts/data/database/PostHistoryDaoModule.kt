package com.neaniesoft.vermilion.posts.data.database

import com.neaniesoft.vermilion.db.Database
import com.neaniesoft.vermilion.db.PostHistoryQueries
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PostHistoryDaoModule {

    @Provides
    @Singleton
    fun providePostHistoryQueries(database: Database): PostHistoryQueries =
        database.postHistoryQueries
}
