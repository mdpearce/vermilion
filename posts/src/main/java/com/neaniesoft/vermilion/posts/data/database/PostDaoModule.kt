package com.neaniesoft.vermilion.posts.data.database

import com.neaniesoft.vermilion.db.VermilionDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class PostDaoModule {
    @Provides
    fun providePostDao(db: VermilionDatabase) = db.postDao()

    @Provides
    fun providePostRemoteKeyDao(db: VermilionDatabase) = db.postRemoteKeysDao()
}
