package com.neaniesoft.vermilion.posts.data

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.github.michaelbull.result.get
import com.github.michaelbull.result.getError
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onSuccess
import com.neaniesoft.vermilion.posts.domain.entities.Community
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.utils.Logger
import com.neaniesoft.vermilion.utils.logger
import java.util.concurrent.atomic.AtomicInteger

class PostsPagingSource constructor(
    private val postRepository: PostRepository,
    private val community: Community
) : PagingSource<String, Post>() {
    private val logger: Logger by logger()

    private val returnedCount = AtomicInteger(0)

    override suspend fun load(params: LoadParams<String>): LoadResult<String, Post> {
        val previousCount = returnedCount.get().let {
            if (it == 0) {
                null
            } else {
                it
            }
        }
        val response = postRepository.postsForCommunity(
            community = community,
            requestedCount = params.loadSize,
            previousCount = previousCount,
            afterKey = params.key
        ).onSuccess { resultSet ->
            returnedCount.addAndGet(resultSet.results.size)
        }.map { response ->
            LoadResult.Page(
                data = response.results,
                prevKey = null,
                nextKey = response.afterKey?.value
            )
        }

        return response.get()
            ?: LoadResult.Error<String, Post>(
                response.getError()
                    ?: IllegalStateException("No error returned, but no value either")
            ).also {
                logger.errorIfEnabled(it.throwable) { "Error occurred during paging" }
            }
    }

    override fun getRefreshKey(state: PagingState<String, Post>): String? {
        return null
    }
}
