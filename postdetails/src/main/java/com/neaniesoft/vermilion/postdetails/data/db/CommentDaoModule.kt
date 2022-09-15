package com.neaniesoft.vermilion.postdetails.data.db

import com.neaniesoft.vermilion.db.Database
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
class CommentDaoModule {
    @Provides
    fun provideCommentQueries(db: Database) = db.commentQueries
}
