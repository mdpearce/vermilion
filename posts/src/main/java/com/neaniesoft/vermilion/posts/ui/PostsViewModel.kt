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
import com.neaniesoft.vermilion.posts.domain.LinkRouter
import com.neaniesoft.vermilion.posts.domain.PostHistoryService
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.tabs.domain.TabSupervisor
import com.neaniesoft.vermilion.tabs.domain.entities.ParentId
import com.neaniesoft.vermilion.tabs.domain.entities.ScrollPosition
import com.neaniesoft.vermilion.tabs.domain.entities.TabType
import com.neaniesoft.vermilion.ui.videos.VideoDescriptor
import com.neaniesoft.vermilion.utils.logger
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.commonmark.parser.Parser
import java.time.Clock
import javax.inject.Inject
import javax.inject.Singleton

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
    private val linkRouter: LinkRouter,
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

    fun onMediaClicked(post: Post) {
        viewModelScope.launch {
            postHistoryService.markPostAsRead(post.id)

            val route = if (post.attachedVideo != null) {
                buildVideoRoute(post.attachedVideo)
            } else {
                buildLinkRoute(post.link)
            }

            _routeEvents.emit(route)
        }
    }

    private fun buildVideoRoute(video: VideoDescriptor): String {
        return "Video/" + Uri.encode(Json.encodeToString(video))
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
}

@Singleton
class CustomVideoRouter @Inject constructor(
    private val matchers: Set<@JvmSuppressWildcards CustomVideoRouteMatcher>
) {
    private val logger by logger()

    fun routeForVideoUri(uri: Uri): String? {
        matchers.forEach { matcher ->
            val result = matcher.match(uri)
            if (result is CustomVideoMatchResult.RouteMatch) {
                logger.debugIfEnabled { "Matched a direct video route: ${result.route}" }
                return result.route
            }
        }
        // No match
        return null
    }
}

interface CustomVideoRouteMatcher {
    fun match(linkUri: Uri): CustomVideoMatchResult
}

@Singleton
class YoutubeCustomVideoRouteMatcher @Inject constructor() : CustomVideoRouteMatcher {
    override fun match(linkUri: Uri): CustomVideoMatchResult {
        return when (linkUri.host) {
            "youtu.be", "youtube.com" -> {
                val videoId = linkUri.pathSegments.lastOrNull()
                if (videoId.isNullOrEmpty()) {
                    CustomVideoMatchResult.NoMatch
                } else {
                    CustomVideoMatchResult.RouteMatch("YouTube/$videoId")
                }
            }
            else -> CustomVideoMatchResult.NoMatch
        }
    }
}

@Module
@InstallIn(SingletonComponent::class)
abstract class CustomVideoRouterModule {
    @Binds
    @IntoSet
    abstract fun bindYoutubeCustomVideoRouteMatcher(impl: YoutubeCustomVideoRouteMatcher): CustomVideoRouteMatcher
}

sealed class CustomVideoMatchResult {
    object NoMatch : CustomVideoMatchResult()
    data class RouteMatch(val route: String) : CustomVideoMatchResult()
}
