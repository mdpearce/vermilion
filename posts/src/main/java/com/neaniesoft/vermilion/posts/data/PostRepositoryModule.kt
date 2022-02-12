package com.neaniesoft.vermilion.posts.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PostRepositoryModule {
    @Binds
    abstract fun postRepository(postRepository: PostRepositoryImpl): PostRepository
}
