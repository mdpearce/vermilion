package com.neaniesoft.vermilion.posts.domain.ports

import com.neaniesoft.vermilion.posts.domain.entities.Community
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.ResultSet

interface PostRepository {
    suspend fun postsForCommunity(community: Community): ResultSet<Post>
}

