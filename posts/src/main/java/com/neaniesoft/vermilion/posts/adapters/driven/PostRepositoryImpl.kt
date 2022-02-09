package com.neaniesoft.vermilion.posts.adapters.driven

import com.neaniesoft.vermilion.api.entities.Link
import com.neaniesoft.vermilion.api.entities.LinkThing
import com.neaniesoft.vermilion.posts.adapters.driven.http.PostsService
import com.neaniesoft.vermilion.posts.domain.entities.AuthorName
import com.neaniesoft.vermilion.posts.domain.entities.CommentCount
import com.neaniesoft.vermilion.posts.domain.entities.Community
import com.neaniesoft.vermilion.posts.domain.entities.CommunityName
import com.neaniesoft.vermilion.posts.domain.entities.FrontPage
import com.neaniesoft.vermilion.posts.domain.entities.NamedCommunity
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostTitle
import com.neaniesoft.vermilion.posts.domain.entities.PreviewText
import com.neaniesoft.vermilion.posts.domain.entities.ResultSet
import com.neaniesoft.vermilion.posts.domain.entities.Score
import com.neaniesoft.vermilion.posts.domain.entities.TextPostSummary
import com.neaniesoft.vermilion.posts.domain.ports.PostRepository
import com.neaniesoft.vermilion.utils.logger
import java.net.URL
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToLong

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postsService: PostsService
) : PostRepository {

    private val logger by logger()

    override suspend fun postsForCommunity(community: Community): ResultSet<Post> {
        logger.debugIfEnabled { "Loading posts for $community" }
        val response = when (community) {
            is FrontPage -> postsService.frontPageBest()
            is NamedCommunity -> TODO()
        }

        val posts = response.data.children.map { child ->
            if (child is LinkThing) {
                child.data.toPost()
            } else {
                throw IllegalArgumentException("Unknown thing type in posts response")
            }
        }

        return ResultSet(posts)
    }
}

internal fun Link.toPost(): Post {
    return Post(
        PostTitle(title),
        TextPostSummary(PreviewText(selfText)),
        CommunityName(subreddit),
        null,
        AuthorName(author),
        Instant.ofEpochMilli((created * 1000.0).roundToLong()),
        CommentCount(numComments),
        Score(score),
        URL(url)
    )
}
