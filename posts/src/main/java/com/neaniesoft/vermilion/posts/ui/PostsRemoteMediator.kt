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
import com.neaniesoft.vermilion.coreentities.CommunityId
import com.neaniesoft.vermilion.coreentities.CommunityName
import com.neaniesoft.vermilion.coreentities.FrontPage
import com.neaniesoft.vermilion.coreentities.NamedCommunity
import com.neaniesoft.vermilion.db.PostQueries
import com.neaniesoft.vermilion.db.PostQuery
import com.neaniesoft.vermilion.db.PostRemoteKeyQueries
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.posts.PostDao
import com.neaniesoft.vermilion.dbentities.posts.PostRemoteKey
import com.neaniesoft.vermilion.dbentities.posts.PostRemoteKeyDao
import com.neaniesoft.vermilion.dbentities.posts.PostWithHistory
import com.neaniesoft.vermilion.posts.data.PostRepository
import com.neaniesoft.vermilion.posts.data.toPostRecord
import com.neaniesoft.vermilion.posts.data.toPostSqlRecord
import com.neaniesoft.vermilion.posts.domain.errors.PostsPersistenceError
import com.neaniesoft.vermilion.utils.logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.URI
import java.time.Clock
import java.util.concurrent.TimeUnit

@ExperimentalPagingApi
class PostsRemoteMediator(
    private val query: String,
    private val postQueries: PostQueries,
    private val postRemoteKeyQueries: PostRemoteKeyQueries,
    private val postRepository: PostRepository,
    private val database: VermilionDatabase,
    private val clock: Clock
) : RemoteMediator<Long, PostQuery>() {

    private val logger by logger()

    private val community = URI.create(query).path.let {
        if (it == FrontPage::class.simpleName) {
            FrontPage
        } else {
            NamedCommunity(CommunityName(it), CommunityId(""))
        }
    }

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Long, PostQuery>
    ): MediatorResult {
        val loadKey = when (loadType) {
            LoadType.REFRESH -> null // Send no `after` key on refresh
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> {
                // if lastItem is null here, it means the initial refresh returned no items.
                // val lastItem = state.lastItemOrNull()
                //     ?: return MediatorResult.Success(endOfPaginationReached = true)
                // lastItem.postId

                val remoteKey = postRemoteKeyQueries.remoteKeyByQuery(query).executeAsOneOrNull()

                // We need to explicitly check for null, as null is only valid for the initial page load, and it indicates that the end of pagination has been reached, i.e. there are no items to display.
                if (remoteKey?.next_key == null) {
                    return MediatorResult.Success(endOfPaginationReached = true)
                }

                remoteKey.next_key
            }
        }
        val previousCount = state.pages.fold(0) { acc, page -> acc + page.data.size }

        val result = withContext(Dispatchers.IO) {
            postRepository.postsForCommunity(
                community,
                state.config.pageSize,
                previousCount,
                loadKey
            )
                .andThen { response ->
                    runCatching {
                        database.withTransaction {
                            postRemoteKeyQueries.transaction {
                                if (loadType == LoadType.REFRESH) {
                                    postRemoteKeyQueries.deleteByQuery(query)
                                    postQueries.deleteByQuery(query)
                                }

                                // Update the remote key for this page
                                postRemoteKeyQueries.insertOrReplace(
                                    query,
                                    response.afterKey?.value
                                )

                                // Insert new posts into db, which invalidates current PagingData
//                            postDao.insertAll(
//                                response.results.map { post ->
//                                    post.toPostRecord(
//                                        query,
//                                        clock
//                                    )
//                                }
//                            )
                                response.results.map { post ->
                                    post.toPostSqlRecord(query, clock)
                                }.forEach { existingRecord ->
                                    postQueries.insert(
                                        id = null,
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
                                }
                            }
                        }

                        response
                    }.mapError { PostsPersistenceError(it) }
                }.map { response ->
                    MediatorResult.Success(endOfPaginationReached = response.afterKey == null)
                }.mapError {
                    MediatorResult.Error(it)
                }
        }

        return when (result) {
            is Ok<MediatorResult.Success> -> result.value
            is Err<MediatorResult.Error> -> result.error
            else -> throw IllegalStateException("Invalid result $result")
        }
    }

    override suspend fun initialize(): InitializeAction {
        if (postQueries.postCount(query).executeAsOne() > 0) {
            logger.debugIfEnabled { "Switching to tab with existing data, skipping refresh" }
            return InitializeAction.SKIP_INITIAL_REFRESH // We have an existing tab, lets not refresh and annoy the user
        } else {
            val cacheTimeout = TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES)
            logger.debugIfEnabled { "clock.millis(): ${clock.millis()}, cacheTimeout = $cacheTimeout" }
            return if (clock.millis() - (postQueries.lastUpdatedAt(query).executeAsOneOrNull()
                    ?: 0L) <= cacheTimeout
            ) {
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
}
