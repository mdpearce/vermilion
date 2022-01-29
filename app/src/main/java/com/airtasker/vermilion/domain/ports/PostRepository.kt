package com.airtasker.vermilion.domain.ports

import com.airtasker.vermilion.domain.entities.CommunityName
import com.airtasker.vermilion.domain.entities.Post
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    suspend fun postsForCommunity(community: Community): Flow<ResultSet<Post>>
}

sealed class Community
data class NamedCommunity(
    val communityName: CommunityName
) : Community()

object FrontPage : Community()

data class ResultSet<out T>(
    val results: List<T>,
    val nextResultSet: () -> Unit
)