package com.neaniesoft.vermilion.posts.data.http

import androidx.room.withTransaction
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching
import com.neaniesoft.vermilion.api.entities.LinkThing
import com.neaniesoft.vermilion.coreentities.Community
import com.neaniesoft.vermilion.coreentities.FrontPage
import com.neaniesoft.vermilion.coreentities.NamedCommunity
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.posts.PostDao
import com.neaniesoft.vermilion.posts.data.PostRepository
import com.neaniesoft.vermilion.posts.data.toPost
import com.neaniesoft.vermilion.posts.data.toPostRecord
import com.neaniesoft.vermilion.posts.domain.entities.AfterKey
import com.neaniesoft.vermilion.posts.domain.entities.BeforeKey
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.domain.entities.ResultSet
import com.neaniesoft.vermilion.posts.domain.errors.PostError
import com.neaniesoft.vermilion.posts.domain.errors.PostsApiError
import com.neaniesoft.vermilion.utils.logger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.commonmark.parser.Parser
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postsService: PostsService,
    private val markdownParser: Parser,
    private val database: VermilionDatabase,
    private val postDao: PostDao,
    private val clock: Clock
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
                is NamedCommunity ->
                    postsService.subredditBest(
                        community.name.value,
                        requestedCount,
                        null,
                        afterKey,
                        previousCount
                    )
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

    override fun postFlow(postId: PostId): Flow<Post> {
        return postDao.postWithIdFlow(postId.value).map { it.toPost(markdownParser) }
    }

    override suspend fun update(postId: PostId, post: Post) {
        database.withTransaction {
            val existingRecord = postDao.postWithId(postId.value)
            if (existingRecord != null) {
                logger.debugIfEnabled { "Updating post record ${postId.value}" }
                val newRecord = post.toPostRecord(query = existingRecord.query, clock)
                    .copy(id = existingRecord.id)
                postDao.update(newRecord)
            } else {
                logger.warnIfEnabled { "Existing record not found for ${postId.value}" }
            }
        }
    }
}
