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
import com.neaniesoft.vermilion.utils.logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.commonmark.parser.Parser
import java.time.Clock
import javax.inject.Inject

@FlowPreview
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
            ?: "Home"
    )
    private val logger by logger()

    private val tabType = if (communityName == CommunityName("Home")) {
        TabType.HOME
    } else {
        TabType.POSTS
    }

    val restoredScrollPosition = flow {
        val position = tabSupervisor.scrollPositionForTab(ParentId(communityName.value), tabType)
        emit(position)
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

    suspend fun onScrollStateUpdated(scrollPosition: ScrollPosition) {
        tabSupervisor.updateScrollState(
            parentId = ParentId(communityName.value),
            type = tabType,
            scrollPosition = scrollPosition
        )
    }

    // private val scrollStateUpdates: MutableSharedFlow<ScrollPosition> = MutableSharedFlow()

    // init {
    //     viewModelScope.launch {
    //         scrollStateUpdates.asSharedFlow().debounce(128).collect {
    //             tabSupervisor.updateScrollState(
    //                 parentId = ParentId(communityName.value),
    //                 type = tabType,
    //                 scrollPosition = it
    //             )
    //         }
    //     }
    // }
}
