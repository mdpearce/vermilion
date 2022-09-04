package com.neaniesoft.vermilion.posts.data.http

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching
import com.neaniesoft.vermilion.api.entities.LinkThing
import com.neaniesoft.vermilion.coreentities.Community
import com.neaniesoft.vermilion.coreentities.FrontPage
import com.neaniesoft.vermilion.coreentities.NamedCommunity
import com.neaniesoft.vermilion.db.PostQueries
import com.neaniesoft.vermilion.posts.data.PostRepository
import com.neaniesoft.vermilion.posts.data.toPost
import com.neaniesoft.vermilion.posts.domain.entities.AfterKey
import com.neaniesoft.vermilion.posts.domain.entities.BeforeKey
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.domain.entities.ResultSet
import com.neaniesoft.vermilion.posts.domain.errors.PostError
import com.neaniesoft.vermilion.posts.domain.errors.PostsApiError
import com.neaniesoft.vermilion.utils.logger
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToOne
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
    private val postQueries: PostQueries,
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
                response.data.after?.let { AfterKey(it) }
            )
        }
    }

    override fun postFlow(postId: PostId): Flow<Post> {
        return postQueries.postWithId(postId.value).asFlow().mapToOne().map {
            it.toPost(markdownParser)
        }
    }

    override suspend fun update(postId: PostId, post: Post) {
        postQueries.transaction {
            val existingRecord = postQueries.postWithId(postId.value).executeAsOneOrNull()
            if (existingRecord != null) {
                logger.debugIfEnabled { "Updating post record ${postId.value}" }
                postQueries.update(
                    id = existingRecord.id,
                    post_id = existingRecord.post_id,
                    query = existingRecord.query,
                    inserted_at = existingRecord.inserted_at,
                    title = existingRecord.title,
                    post_type = existingRecord.post_type,
                    link_host = existingRecord.link_host,
                    thumbnail_uri = existingRecord.thumbnail_uri,
                    preview_uri = existingRecord.preview_uri,
                    preview_width = existingRecord.preview_width,
                    preview_height = existingRecord.preview_height,
                    preview_video_width = existingRecord.preview_video_width,
                    preview_video_height = existingRecord.preview_video_height,
                    preview_video_dash = existingRecord.preview_video_dash,
                    preview_video_hls = existingRecord.preview_video_hls,
                    preview_video_fallback = existingRecord.preview_video_fallback,
                    animated_preview_width = existingRecord.animated_preview_width,
                    animated_preview_height = existingRecord.animated_preview_height,
                    animated_preview_uri = existingRecord.animated_preview_uri,
                    video_width = existingRecord.video_width,
                    video_height = existingRecord.video_height,
                    video_dash = existingRecord.video_dash,
                    video_hls = existingRecord.video_hls,
                    video_fallback = existingRecord.video_fallback,
                    link_uri = existingRecord.link_uri,
                    preview_text = existingRecord.preview_text,
                    community_name = existingRecord.community_name,
                    community_id = existingRecord.community_id,
                    author_name = existingRecord.author_name,
                    posted_at = existingRecord.posted_at,
                    comment_count = existingRecord.comment_count,
                    score = existingRecord.score,
                    flags = existingRecord.flags,
                    flair_text = existingRecord.flair_text,
                    flair_background_color = existingRecord.flair_background_color,
                    flair_text_color = existingRecord.flair_text_color,
                    gallery_item_uris = existingRecord.gallery_item_uris,
                    gallery_item_widths = existingRecord.gallery_item_widths,
                    gallery_item_heights = existingRecord.gallery_item_heights
                )
            } else {
                logger.warnIfEnabled { "Existing record not found for ${postId.value}" }
            }
        }

    }

    override suspend fun deleteByQuery(query: String) {
        postQueries.deleteByQuery(query)
    }
}