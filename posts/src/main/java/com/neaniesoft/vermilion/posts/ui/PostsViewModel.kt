package com.neaniesoft.vermilion.posts.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import androidx.paging.map
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.posts.PostDao
import com.neaniesoft.vermilion.dbentities.posts.PostRemoteKeyDao
import com.neaniesoft.vermilion.posts.data.PostRepository
import com.neaniesoft.vermilion.posts.data.toPost
import com.neaniesoft.vermilion.posts.domain.entities.FrontPage
import com.neaniesoft.vermilion.posts.domain.entities.Post
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import java.time.Clock
import javax.inject.Inject

@HiltViewModel
class PostsViewModel @Inject constructor(
    postRepository: PostRepository,
    postDao: PostDao,
    postRemoteKeyDao: PostRemoteKeyDao,
    database: VermilionDatabase,
    clock: Clock
) : ViewModel() {
    private val query = requireNotNull(FrontPage::class.simpleName)

    @ExperimentalPagingApi
    val pageFlow = Pager(
        PagingConfig(pageSize = 20),
        remoteMediator = PostsRemoteMediator(
            query,
            postDao,
            postRemoteKeyDao,
            postRepository,
            database,
            clock
        )
    ) {
        postDao.pagingSource(query)
    }.flow.map { pagingData ->
        pagingData.map {
            it.toPost()
        }
    }.cachedIn(viewModelScope)

    fun onPostClicked(post: Post) {


    }
}
