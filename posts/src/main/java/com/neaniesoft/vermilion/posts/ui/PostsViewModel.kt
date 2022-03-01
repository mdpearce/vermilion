package com.neaniesoft.vermilion.posts.ui

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.ExperimentalPagingApi
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.map
import com.neaniesoft.vermilion.coreentities.Community
import com.neaniesoft.vermilion.coreentities.CommunityName
import com.neaniesoft.vermilion.coreentities.NamedCommunity
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.posts.PostDao
import com.neaniesoft.vermilion.dbentities.posts.PostRemoteKeyDao
import com.neaniesoft.vermilion.posts.data.PostRepository
import com.neaniesoft.vermilion.posts.data.toPost
import com.neaniesoft.vermilion.posts.domain.PostHistoryService
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.tabs.domain.TabSupervisor
import com.neaniesoft.vermilion.tabs.domain.entities.ParentId
import com.neaniesoft.vermilion.tabs.domain.entities.ScrollPosition
import com.neaniesoft.vermilion.tabs.domain.entities.TabType
import com.neaniesoft.vermilion.utils.logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.commonmark.parser.Parser
import java.net.URLEncoder
import java.time.Clock
import javax.inject.Inject

@FlowPreview
@HiltViewModel
class PostsViewModel @Inject constructor(
    private val postRepository: PostRepository,
    private val postDao: PostDao,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val postHistoryService: PostHistoryService,
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

    private val _routeEvents = MutableSharedFlow<String>()
    val routeEvents = _routeEvents.asSharedFlow()

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

    fun onOpenPostDetails(postId: PostId) {
        viewModelScope.launch {
            postHistoryService.markPostAsRead(postId)
            val route = "PostDetails/${postId.value}"
            _routeEvents.emit(route)
        }
    }

    fun onOpenUri(post: Post, uri: Uri) {
        viewModelScope.launch {
            postHistoryService.markPostAsRead(post.id)
            _routeEvents.emit(customTabRoute(uri))
        }
    }

    fun onOpenCommunity(community: Community) {
        when (community) {
            is NamedCommunity -> {
                val route = "Posts/${community.name.value}"
                viewModelScope.launch { _routeEvents.emit(route) }
            }
            else -> {// do nothing
            }
        }
    }

    private fun customTabRoute(uri: Uri): String =
        "CustomTab/" + URLEncoder.encode(uri.toString(), "utf-8")
}
