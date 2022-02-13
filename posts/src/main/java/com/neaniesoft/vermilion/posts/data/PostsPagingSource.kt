package com.neaniesoft.vermilion.posts.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import com.github.michaelbull.result.map
import com.neaniesoft.vermilion.posts.data.entities.PagingQuery
import com.neaniesoft.vermilion.posts.domain.entities.Community
import com.neaniesoft.vermilion.posts.domain.entities.FirstSet
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.utils.Logger
import com.neaniesoft.vermilion.utils.logger

class PostsPagingSource constructor(
    private val postRepository: PostRepository,
    private val community: Community
) : PagingSource<PagingQuery, Post>() {
    private val logger: Logger by logger()

    override suspend fun load(params: LoadParams<PagingQuery>): LoadResult<PagingQuery, Post> {
        val query = params.key ?: PagingQuery(FirstSet, 0)
        val response = postRepository.postsForCommunity(
            community = community,
            requestedCount = params.loadSize,
            previousCount = query.previousCount,
            listingKey = query.listingKey
        ).map { response ->
            LoadResult.Page(
                data = response.results,
                prevKey = null,
                nextKey = response.afterKey?.let {
                    query.copy(
                        listingKey = it,
                        previousCount = query.previousCount + response.results.size
                    )
                }
            )
        }

        return response.get()
            ?: LoadResult.Error<PagingQuery, Post>(
                response.getError()
                    ?: IllegalStateException("No error returned, but no value either")
            ).also {
                logger.errorIfEnabled(it.throwable) { "Error occurred during paging" }
            }
    }

    override fun getRefreshKey(state: PagingState<PagingQuery, Post>): PagingQuery {
        return PagingQuery(FirstSet, 0)
    }
}
