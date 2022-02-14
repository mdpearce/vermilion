package com.neaniesoft.vermilion.posts.ui

import android.util.Log
import androidx.core.net.toUri
import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import androidx.room.withTransaction
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.andThen
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.posts.PostDao
import com.neaniesoft.vermilion.dbentities.posts.PostRecord
import com.neaniesoft.vermilion.dbentities.posts.PostType
import com.neaniesoft.vermilion.posts.data.PostRepository
import com.neaniesoft.vermilion.posts.domain.entities.AuthorName
import com.neaniesoft.vermilion.posts.domain.entities.CommentCount
import com.neaniesoft.vermilion.posts.domain.entities.CommunityName
import com.neaniesoft.vermilion.posts.domain.entities.FrontPage
import com.neaniesoft.vermilion.posts.domain.entities.ImagePostSummary
import com.neaniesoft.vermilion.posts.domain.entities.LinkHost
import com.neaniesoft.vermilion.posts.domain.entities.LinkPostSummary
import com.neaniesoft.vermilion.posts.domain.entities.NamedCommunity
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostFlags
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.domain.entities.PostTitle
import com.neaniesoft.vermilion.posts.domain.entities.PreviewText
import com.neaniesoft.vermilion.posts.domain.entities.Score
import com.neaniesoft.vermilion.posts.domain.entities.TextPostSummary
import com.neaniesoft.vermilion.posts.domain.entities.UriImage
import com.neaniesoft.vermilion.posts.domain.entities.VideoPostSummary
import com.neaniesoft.vermilion.posts.domain.errors.PostsPersistenceError
import com.neaniesoft.vermilion.utils.logger
import java.net.URI
import java.net.URL
import java.time.Clock
import java.time.Instant
import java.util.UUID
import java.util.concurrent.TimeUnit

@ExperimentalPagingApi
class PostsRemoteMediator(
    private val query: String,
    private val postDao: PostDao,
    private val postRepository: PostRepository,
    private val database: VermilionDatabase,
    private val clock: Clock
) : RemoteMediator<Int, PostRecord>() {

    private val logger by logger()

    val community = URI.create(query).path.let {
        if (it == FrontPage::class.simpleName) {
            FrontPage
        } else {
            NamedCommunity(CommunityName(it))
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostRecord>
    ): MediatorResult {

        val loadKey = when (loadType) {
            LoadType.REFRESH -> null // Send no `after` key on refresh
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                // if lastItem is null here, it means the initial refresh returned no items.
                val lastItem = state.lastItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                lastItem.postId
            }
        }

        val result =
            postRepository.postsForCommunity(community, state.config.pageSize, null, loadKey)
                .andThen { response ->
                    runCatching {
                        database.withTransaction {
                            if (loadType == LoadType.REFRESH) {
                                postDao.deleteByQuery(query)
                            }

                            // Insert new posts into db, which invalidates current PagingData
                            postDao.insertAll(response.results.map {
                                it.toPostRecord(
                                    query,
                                    clock
                                )
                            })
                            response
                        }
                    }.mapError { PostsPersistenceError(it) }
                }.map { response ->
                    MediatorResult.Success(endOfPaginationReached = response.afterKey == null)
                }.mapError {
                    MediatorResult.Error(it)
                }

        return when (result) {
            is Ok<MediatorResult.Success> -> result.value
            is Err<MediatorResult.Error> -> result.error
            else -> throw IllegalStateException("Invalid result $result")
        }
    }

    override suspend fun initialize(): InitializeAction {
        val cacheTimeout = TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES)
        logger.debugIfEnabled { "clock.millis(): ${clock.millis()}, cacheTimeout = $cacheTimeout" }
        return if (clock.millis() - (postDao.lastUpdatedAt(query) ?: 0L) <= cacheTimeout) {
            logger.debugIfEnabled { "Cache is up to date, skipping refresh" }
            // Cache is up to date, skip refresh
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            logger.debugIfEnabled { "Cache is invalid, performing refresh" }
            // Cache is invalid, perform a refresh
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }
}

fun PostRecord.toPost(): Post {

    Log.d("toPost", "flags: $flags")

    return Post(
        id = PostId(postId),
        title = PostTitle(title),
        summary = when (postType) {
            PostType.IMAGE -> ImagePostSummary(
                LinkHost(linkHost),
                thumbnailUri = thumbnailUri?.toUri()
                    ?: throw IllegalStateException("Image post with no thumbnail returned from db"),
                previews = listOf(
                    UriImage(
                        previewUri?.toUri()
                            ?: throw IllegalStateException("Image post with no preview returned from db"),
                        previewWidth ?: 0,
                        previewHeight ?: 0
                    )
                ),
                fullSizeUri = linkUri.toUri()
            )
            PostType.LINK -> TODO()
            PostType.TEXT -> TextPostSummary(
                previewText = PreviewText(previewText ?: "")
            )
            PostType.VIDEO -> VideoPostSummary(
                LinkHost(linkHost),
                thumbnailUri = thumbnailUri?.toUri()
                    ?: throw IllegalStateException("Video post with no thumbnail returned from db"),
                previews = listOf(
                    UriImage(
                        previewUri?.toUri()
                            ?: throw IllegalStateException("Video post with no preview returned from db"),
                        previewWidth ?: 0,
                        previewHeight ?: 0
                    )
                ),
                linkUri = linkUri.toUri()
            )
        },
        community = if (communityName == FrontPage.routeName) {
            FrontPage
        } else {
            NamedCommunity(CommunityName(communityName))
        },
        authorName = AuthorName(authorName),
        postedAt = Instant.ofEpochMilli(postedAt),
        awardCounts = emptyMap(), // TODO implement caching of awards
        commentCount = CommentCount(commentCount),
        score = Score(score),
        flags = flags.split(",").filter { it.isNotEmpty() }.map { PostFlags.valueOf(it) }.toSet(),
        link = URL(linkUri)
    )
}

fun Post.toPostRecord(query: String, clock: Clock): PostRecord = PostRecord(
    id = UUID.randomUUID().toString(),
    postId = id.value,
    query = query,
    insertedAt = clock.millis(),
    title = title.value,
    postType = when (summary) {
        is TextPostSummary -> PostType.TEXT
        is ImagePostSummary -> PostType.IMAGE
        is LinkPostSummary -> PostType.LINK
        is VideoPostSummary -> PostType.VIDEO
    },
    linkHost = link.host,
    thumbnailUri = when (summary) {
        is ImagePostSummary -> summary.thumbnailUri.toString()
        is VideoPostSummary -> summary.thumbnailUri.toString()
        else -> null
    },
    previewUri = when (summary) {
        is ImagePostSummary -> summary.previews.last().uri.toString()
        is VideoPostSummary -> summary.previews.last().uri.toString()
        else -> null
    },
    previewWidth = when (summary) {
        is ImagePostSummary -> summary.previews.last().width
        is VideoPostSummary -> summary.previews.last().width
        else -> null
    },
    previewHeight = when (summary) {
        is ImagePostSummary -> summary.previews.last().height
        is VideoPostSummary -> summary.previews.last().height
        else -> null
    },
    linkUri = link.toString(),
    previewText = when (summary) {
        is TextPostSummary -> summary.previewText.value
        else -> null
    },
    communityName = community.routeName,
    authorName = authorName.value,
    postedAt = postedAt.toEpochMilli(),
    commentCount = commentCount.value,
    score = score.value,
    flags = flags.joinToString(",") { it.name }
)
