package com.neaniesoft.vermilion.posts.data.database

import com.neaniesoft.vermilion.db.Database
import com.neaniesoft.vermilion.db.PostHistoryQueries
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.posts.PostHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PostHistoryDaoModule {
    @Provides
    fun providePostHistoryDao(database: VermilionDatabase): PostHistoryDao =
        database.postHistoryDao()

    @Provides
    @Singleton
    fun providePostHistoryQueries(database: Database): PostHistoryQueries =
        database.postHistoryQueries
}
