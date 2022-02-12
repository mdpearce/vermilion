package com.neaniesoft.vermilion.posts.adapters.driven

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.neaniesoft.vermilion.posts.domain.entities.Community
import com.neaniesoft.vermilion.posts.domain.entities.FirstSet
import com.neaniesoft.vermilion.posts.domain.entities.ListingKey
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.ports.PostRepository
import retrofit2.HttpException
import java.io.IOException

class PostsPagingSource constructor(
    private val postRepository: PostRepository,
    private val community: Community
) : PagingSource<PagingQuery, Post>() {
    override suspend fun load(params: LoadParams<PagingQuery>): LoadResult<PagingQuery, Post> {
        try {
            val query = params.key ?: PagingQuery(FirstSet, 0)
            val response = postRepository.postsForCommunity(
                community = community,
                requestedCount = params.loadSize,
                previousCount = query.previousCount,
                listingKey = query.listingKey
            )

            return LoadResult.Page(
                data = response.results,
                prevKey = null,
                nextKey = response.afterKey?.let { query.copy(listingKey = it) }
            )
        } catch (e: IOException) {
            return LoadResult.Error(e)
        } catch (e: HttpException) {
            return LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<PagingQuery, Post>): PagingQuery {
        return PagingQuery(FirstSet, 0)
    }
}

data class PagingQuery(
    val listingKey: ListingKey,
    val previousCount: Int
)
