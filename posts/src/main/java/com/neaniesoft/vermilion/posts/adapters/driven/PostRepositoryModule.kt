package com.neaniesoft.vermilion.posts.adapters.driven

import com.neaniesoft.vermilion.posts.domain.ports.PostRepository
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
