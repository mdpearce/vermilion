package com.neaniesoft.vermilion.postdetails.data

import com.neaniesoft.vermilion.dbentities.comments.CommentDao
import com.neaniesoft.vermilion.postdetails.CommentNode
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.utils.logger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

interface CommentRepository {
    suspend fun getCommentTreeForPost(postId: PostId): List<CommentNode>
}

@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val apiService: CommentApiService,
    private val dao: CommentDao,
    private val clock: Clock
) {
    private val logger by logger()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CommentRepositoryModule {
    @Binds
    abstract fun provideCommentRepository(impl: CommentRepositoryImpl): CommentRepository
}
