package com.neaniesoft.vermilion.posts.data

import com.github.michaelbull.result.Result
import com.neaniesoft.vermilion.coreentities.Community
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.ResultSet
import com.neaniesoft.vermilion.posts.domain.errors.PostError

interface PostRepository {
    suspend fun postsForCommunity(
        community: Community,
        requestedCount: Int,
        previousCount: Int?,
        afterKey: String?
    ): Result<ResultSet<Post>, PostError>
}
