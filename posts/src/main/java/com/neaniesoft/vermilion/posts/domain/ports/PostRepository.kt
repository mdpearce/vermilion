package com.neaniesoft.vermilion.posts.domain.ports

import com.neaniesoft.vermilion.posts.domain.entities.CommunityName
import com.neaniesoft.vermilion.posts.domain.entities.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    suspend fun postsForCommunity(community: Community): ResultSet<Post>
}

sealed class Community
data class NamedCommunity(
    val communityName: CommunityName
) : Community()

object FrontPage : Community()

data class ResultSet<out T>(
    val results: List<T>,
)