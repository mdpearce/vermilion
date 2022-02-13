package com.neaniesoft.vermilion.posts.ui

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
import com.neaniesoft.vermilion.posts.domain.entities.CommunityName
import com.neaniesoft.vermilion.posts.domain.entities.FrontPage
import com.neaniesoft.vermilion.posts.domain.entities.ImagePostSummary
import com.neaniesoft.vermilion.posts.domain.entities.LinkPostSummary
import com.neaniesoft.vermilion.posts.domain.entities.NamedCommunity
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.TextPostSummary
import com.neaniesoft.vermilion.posts.domain.entities.VideoPostSummary
import com.neaniesoft.vermilion.posts.domain.errors.PostsPersistenceError
import java.net.URI
import java.time.Clock
import java.util.UUID
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalPagingApi::class)
class PostsRemoteMediator(
    private val query: String,
    private val postDao: PostDao,
    private val postRepository: PostRepository,
    private val database: VermilionDatabase,
    private val clock: Clock
) : RemoteMediator<String, Post>() {

    val community = URI.create(query).path.let {
        if (it == FrontPage::class.simpleName) {
            FrontPage
        } else {
            NamedCommunity(CommunityName(it))
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<String, Post>
    ): MediatorResult {

        val loadKey = when (loadType) {
            LoadType.REFRESH -> null // Send no `after` key on refresh
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                // if lastItem is null here, it means the initial refresh returned no items.
                val lastItem = state.lastItemOrNull()
                    ?: return MediatorResult.Success(endOfPaginationReached = true)
                lastItem.id
            }
        }

        val result =
            postRepository.postsForCommunity(community, state.config.pageSize, null, loadKey?.value)
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

        return if (clock.millis() - postDao.lastUpdatedAt(query) >= cacheTimeout) {
            // Cache is up to date, skip refresh
            InitializeAction.SKIP_INITIAL_REFRESH
        } else {
            // Cache is invalid, perform a refresh
            InitializeAction.LAUNCH_INITIAL_REFRESH
        }
    }
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
    }
)
