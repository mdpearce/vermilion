package com.neaniesoft.vermilion.posts.data.database

import com.neaniesoft.vermilion.db.Database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class PostDaoModule {

    @Provides
    @Singleton
    fun providePostQueries(db: Database) = db.postQueries

    @Provides
    @Singleton
    fun providePostRemoteKeyQueries(db: Database) = db.postRemoteKeyQueries
}
