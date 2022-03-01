package com.neaniesoft.vermilion.posts.data.database

import com.neaniesoft.vermilion.posts.data.PostHistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PostHistoryRoomRepositoryModule {
    @Binds
    abstract fun providePostHistoryRepository(impl: PostHistoryRoomRepository): PostHistoryRepository
}
