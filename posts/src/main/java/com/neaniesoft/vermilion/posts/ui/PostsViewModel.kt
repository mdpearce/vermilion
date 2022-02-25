package com.neaniesoft.vermilion.posts.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.posts.PostDao
import com.neaniesoft.vermilion.dbentities.posts.PostRemoteKeyDao
import com.neaniesoft.vermilion.posts.data.PostRepository
import com.neaniesoft.vermilion.posts.data.toPost
import com.neaniesoft.vermilion.posts.domain.entities.CommunityName
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.tabs.domain.TabSupervisor
import com.neaniesoft.vermilion.tabs.domain.entities.ParentId
import com.neaniesoft.vermilion.tabs.domain.entities.ScrollPosition
import com.neaniesoft.vermilion.tabs.domain.entities.TabType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.commonmark.parser.Parser
import java.time.Clock
import javax.inject.Inject

@HiltViewModel
class PostsViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val database: VermilionDatabase,
    private val clock: Clock,
    private val markdownParser: Parser,
    private val tabSupervisor: TabSupervisor,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val pagingDataMap: MutableMap<String, Flow<PagingData<Post>>> = mutableMapOf()
    private val communityName = CommunityName(
        savedStateHandle.get<String>("communityName")
            ?: ""
    )

    private val initialScrollPosition = ScrollPosition(
        savedStateHandle.get<Int>("initialScrollIndex") ?: 0,
        savedStateHandle.get<Int>("initialScrollOffset") ?: 0
    )

    private val _scrollPosition = MutableStateFlow(initialScrollPosition)
    val initialScrollPositionState: StateFlow<ScrollPosition> = _scrollPosition.asStateFlow()

    private val _scrollUpdates: MutableStateFlow<ScrollPosition> =
        MutableStateFlow(ScrollPosition(0, 0))
    private val scrollUpdates = _scrollUpdates.asStateFlow()

    init {
        setUpScrollListener()
    }

    @ExperimentalPagingApi
    fun pagingData(query: String): Flow<PagingData<Post>> {
        return pagingDataMap.computeIfAbsent(query) { key ->
            Pager(
                PagingConfig(pageSize = 20),
                remoteMediator = PostsRemoteMediator(
                    key,
                    postDao,
                    postRemoteKeyDao,
                    postRepository,
                    database,
                    clock
                )
            ) {
                postDao.pagingSource(key)
            }.flow.map { pagingData ->
                pagingData.map {
                    it.toPost(markdownParser)
                }
            }.cachedIn(viewModelScope)
        }
    }

    private fun setUpScrollListener() {
        viewModelScope.launch {
            scrollUpdates.collect {
                if (communityName.value.isNotEmpty())
                    tabSupervisor.updateScrollState(
                        parentId = ParentId(communityName.value),
                        type = TabType.POSTS,
                        scrollPosition = it
                    )
            }
        }
    }

    fun onScrollStateUpdated(firstVisibleItemIndex: Int, firstVisibleItemOffset: Int) {
        viewModelScope.launch {
            _scrollUpdates.emit(
                ScrollPosition(
                    firstVisibleItemIndex,
                    firstVisibleItemOffset
                )
            )
        }
    }
}
