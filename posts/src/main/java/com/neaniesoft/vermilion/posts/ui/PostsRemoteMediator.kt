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
import com.neaniesoft.vermilion.dbentities.posts.PostRemoteKey
import com.neaniesoft.vermilion.dbentities.posts.PostRemoteKeyDao
import com.neaniesoft.vermilion.posts.data.PostRepository
import com.neaniesoft.vermilion.posts.data.toPostRecord
import com.neaniesoft.vermilion.posts.domain.entities.CommunityName
import com.neaniesoft.vermilion.posts.domain.entities.FrontPage
import com.neaniesoft.vermilion.posts.domain.entities.NamedCommunity
import com.neaniesoft.vermilion.posts.domain.errors.PostsPersistenceError
import com.neaniesoft.vermilion.utils.logger
import java.net.URI
import java.time.Clock
import java.util.concurrent.TimeUnit

@ExperimentalPagingApi
class PostsRemoteMediator(
    private val query: String,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val postRepository: PostRepository,
    private val database: VermilionDatabase,
    private val clock: Clock
) : RemoteMediator<Int, PostRecord>() {

    private val logger by logger()

    private val community = URI.create(query).path.let {
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
                // val lastItem = state.lastItemOrNull()
                //     ?: return MediatorResult.Success(endOfPaginationReached = true)
                // lastItem.postId

                val remoteKey =
                    database.withTransaction { postRemoteKeyDao.remoteKeyByQuery(query) }

                // We need to explicitly check for null, as null is only valid for the initial page load, and it indicates that the end of pagination has been reached, i.e. there are no items to display.
                if (remoteKey.nextKey == null) {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }

                remoteKey.nextKey
            }
        }
        val previousCount = state.pages.fold(0) { acc, page -> acc + page.data.size }

        val result =
            postRepository.postsForCommunity(
                community,
                state.config.pageSize,
                previousCount,
                loadKey
            )
                .andThen { response ->
                    runCatching {
                        database.withTransaction {
                            if (loadType == LoadType.REFRESH) {
                                postRemoteKeyDao.deleteByQuery(query)
                                postDao.deleteByQuery(query)
                            }

                            // Update the remote key for this page
                            postRemoteKeyDao.insertOrReplace(
                                PostRemoteKey(
                                    query,
                                    response.afterKey?.value
                                )
                            )

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
