package com.neaniesoft.vermilion.postdetails.data

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class CommentRepositoryModule {
    @Binds
    abstract fun provideCommentRepository(impl: CommentRepositoryImpl): CommentRepository
}
