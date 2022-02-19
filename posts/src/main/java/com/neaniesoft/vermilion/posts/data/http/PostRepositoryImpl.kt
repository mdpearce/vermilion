package com.neaniesoft.vermilion.posts.data.http

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching
import com.neaniesoft.vermilion.api.entities.LinkThing
import com.neaniesoft.vermilion.posts.data.PostRepository
import com.neaniesoft.vermilion.posts.data.toPost
import com.neaniesoft.vermilion.posts.domain.entities.AfterKey
import com.neaniesoft.vermilion.posts.domain.entities.BeforeKey
import com.neaniesoft.vermilion.posts.domain.entities.Community
import com.neaniesoft.vermilion.posts.domain.entities.FrontPage
import com.neaniesoft.vermilion.posts.domain.entities.NamedCommunity
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.ResultSet
import com.neaniesoft.vermilion.posts.domain.errors.PostError
import com.neaniesoft.vermilion.posts.domain.errors.PostsApiError
import com.neaniesoft.vermilion.utils.logger
import org.commonmark.parser.Parser
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postsService: PostsService,
    private val markdownParser: Parser
) : PostRepository {

    private val logger by logger()

    override suspend fun postsForCommunity(
        community: Community,
        requestedCount: Int,
        previousCount: Int?,
        afterKey: String?
    ): Result<ResultSet<Post>, PostError> {
        logger.debugIfEnabled { "Loading posts for $community" }
        return runCatching {
            when (community) {
                is FrontPage -> {
                    postsService.frontPageBest(
                        requestedCount,
                        null,
                        afterKey,
                        previousCount
                    )
                }
                is NamedCommunity -> TODO()
            }
        }.mapError {
            PostsApiError(it)
        }.map { response ->
            val posts = response.data.children.mapNotNull { child ->
                if (child is LinkThing) {
                    child.data.toPost(markdownParser)
                } else {
                    logger.warnIfEnabled { "Unknown thing type in posts response" }
                    null
                }
            }

            ResultSet(
                posts,
                response.data.before?.let { BeforeKey(it) },
                response.data.after?.let { AfterKey(it) },
            )
        }
    }
}
