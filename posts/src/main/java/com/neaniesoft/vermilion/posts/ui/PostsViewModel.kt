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
import com.neaniesoft.vermilion.coreentities.ScrollPosition
import com.neaniesoft.vermilion.db.VermilionDatabase
import com.neaniesoft.vermilion.dbentities.posts.PostDao
import com.neaniesoft.vermilion.dbentities.posts.PostRemoteKeyDao
import com.neaniesoft.vermilion.posts.data.PostRepository
import com.neaniesoft.vermilion.posts.data.toPost
import com.neaniesoft.vermilion.posts.domain.LinkRouter
import com.neaniesoft.vermilion.posts.domain.PostHistoryService
import com.neaniesoft.vermilion.posts.domain.PostVotingService
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.ui.videos.direct.VideoDescriptor
import com.neaniesoft.vermilion.uistate.TabType
import com.neaniesoft.vermilion.uistate.UiStateProvider
import com.neaniesoft.vermilion.utils.logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.commonmark.parser.Parser
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
    private val uiStateProvider: UiStateProvider,
    private val linkRouter: LinkRouter,
    private val postVotingService: PostVotingService,
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

    suspend fun getSavedScrollPosition(): ScrollPosition? {
        val scrollPosition = if (communityName == CommunityName("Home")) {
            uiStateProvider.scrollPositionTab(TabType.HOME, communityName.value)
        } else {
            uiStateProvider.scrollPositionTab(TabType.POSTS, communityName.value)
        }

        logger.debugIfEnabled { "Saved scroll position: $scrollPosition" }

        return scrollPosition
    }

    suspend fun onScrollStateUpdated(scrollPosition: ScrollPosition) {
        logger.debugIfEnabled { "onScrollStateUpdated: $scrollPosition" }
        if (communityName == CommunityName("Home")) {
            uiStateProvider.updateScrollPositionForTab(
                TabType.HOME,
                communityName.value,
                scrollPosition
            )
        } else {
            uiStateProvider.updateScrollPositionForTab(
                TabType.POSTS,
                communityName.value,
                scrollPosition
            )
        }
    }

    fun onOpenPostDetails(postId: PostId) {
        viewModelScope.launch {
            postHistoryService.markPostAsRead(postId)
            val route = "PostDetails/${postId.value}"
            _routeEvents.emit(route)
        }
    }

    fun onMediaClicked(post: Post) {
        viewModelScope.launch {
            postHistoryService.markPostAsRead(post.id)

            val route = if (post.attachedVideo != null) {
                logger.debugIfEnabled { "Found attached video, loading video directly" }
                buildVideoRoute(post.attachedVideo)
            } else if (post.type == Post.Type.IMAGE && post.animatedImagePreview != null) {
                logger.debugIfEnabled { "Found image with animated preview, loading video with animated preview" }
                buildVideoRoute(post.animatedImagePreview.uri)
            } else {
                logger.debugIfEnabled { "Falling back to link route" }
                buildLinkRoute(post.link)
            }

            _routeEvents.emit(route)
        }
    }

    private fun buildVideoRoute(video: VideoDescriptor): String {
        return "Video/" + Uri.encode(video.dash.toString())
    }

    private fun buildVideoRoute(uri: Uri): String {
        return "Video/" + Uri.encode(uri.toString())
    }

    private fun buildLinkRoute(uri: Uri): String {
        return linkRouter.routeForLink(uri)
    }

    fun onOpenCommunity(community: Community) {
        when (community) {
            is NamedCommunity -> {
                val route = "Posts/${community.name.value}"
                viewModelScope.launch { _routeEvents.emit(route) }
            }
            else -> { // do nothing
            }
        }
    }

    fun onUriClicked(uri: Uri) {
        viewModelScope.launch { _routeEvents.emit(buildLinkRoute(uri)) }
    }

    fun onUpVoteClicked(post: Post) {
        viewModelScope.launch { postVotingService.toggleUpVote(post) }
    }

    fun onDownVoteClicked(post: Post) {
        viewModelScope.launch { postVotingService.toggleDownVote(post) }
    }
}
