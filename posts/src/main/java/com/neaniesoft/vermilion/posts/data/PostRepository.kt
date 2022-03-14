package com.neaniesoft.vermilion.posts.data

import com.github.michaelbull.result.Result
import com.neaniesoft.vermilion.coreentities.Community
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.domain.entities.ResultSet
import com.neaniesoft.vermilion.posts.domain.errors.PostError
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    suspend fun postsForCommunity(
        community: Community,
        requestedCount: Int,
        previousCount: Int?,
        afterKey: String?
    ): Result<ResultSet<Post>, PostError>

    fun postFlow(postId: PostId): Flow<Post>
    suspend fun update(postId: PostId, post: Post)
}
