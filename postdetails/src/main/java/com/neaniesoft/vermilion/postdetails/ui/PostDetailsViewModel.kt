package com.neaniesoft.vermilion.postdetails.ui

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neaniesoft.vermilion.coreentities.ScrollPosition
import com.neaniesoft.vermilion.postdetails.domain.entities.ThreadStub
import com.neaniesoft.vermilion.posts.domain.LinkRouter
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.uistate.TabType
import com.neaniesoft.vermilion.uistate.UiStateProvider
import com.neaniesoft.vermilion.utils.logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailsViewModel @Inject constructor(
    private val uiStateProvider: UiStateProvider,
    private val linkRouter: LinkRouter,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val logger by logger()

    private val _routeEvents = MutableSharedFlow<String>()
    val routeEvents = _routeEvents.asSharedFlow()

    private val postId = PostId(
        savedStateHandle.get<String>("postId")
            ?: throw IllegalStateException("Could not obtain post ID from saved state")
    )

    suspend fun getSavedScrollPosition(): ScrollPosition? {
        return uiStateProvider.scrollPositionTab(TabType.POST_DETAILS, postId.value)
    }

    suspend fun onScrollStateUpdated(scrollPosition: ScrollPosition) {
        uiStateProvider.updateScrollPositionForTab(
            TabType.POST_DETAILS,
            postId.value,
            scrollPosition
        )
    }

    fun onOpenUri(uri: Uri) {
        viewModelScope.launch { _routeEvents.emit(linkRouter.routeForLink(uri)) }
    }

    fun onThreadClicked(stub: ThreadStub) {
        val route = "CommentThread/${stub.postId.value}/${stub.parentId.value}"
        logger.debugIfEnabled { "Thread clicked, routing to $route" }
        viewModelScope.launch { _routeEvents.emit(route) }
    }
}
