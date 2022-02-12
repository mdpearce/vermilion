package com.neaniesoft.vermilion.posts.data

import com.neaniesoft.vermilion.posts.domain.entities.Community
import com.neaniesoft.vermilion.posts.domain.entities.ListingKey
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.ResultSet

interface PostRepository {
    suspend fun postsForCommunity(
        community: Community,
        requestedCount: Int,
        previousCount: Int?,
        listingKey: ListingKey
    ): ResultSet<Post>
}
