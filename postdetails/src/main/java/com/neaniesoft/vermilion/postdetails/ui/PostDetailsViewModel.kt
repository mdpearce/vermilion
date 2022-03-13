package com.neaniesoft.vermilion.postdetails.ui

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neaniesoft.vermilion.posts.domain.LinkRouter
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.tabs.domain.TabSupervisor
import com.neaniesoft.vermilion.tabs.domain.entities.ParentId
import com.neaniesoft.vermilion.tabs.domain.entities.ScrollPosition
import com.neaniesoft.vermilion.tabs.domain.entities.TabType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PostDetailsViewModel @Inject constructor(
    private val tabSupervisor: TabSupervisor,
    private val linkRouter: LinkRouter,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _routeEvents = MutableSharedFlow<String>()
    val routeEvents = _routeEvents.asSharedFlow()

    private val postId = PostId(
        savedStateHandle.get<String>("id")
            ?: throw IllegalStateException("Could not obtain post ID from saved state")
    )

    val restoredScrollPosition = flow {
        val position =
            tabSupervisor.scrollPositionForTab(ParentId(postId.value), TabType.POST_DETAILS)
        emit(position)
    }

    suspend fun onScrollStateUpdated(scrollPosition: ScrollPosition) {
        tabSupervisor.updateScrollState(
            ParentId(postId.value),
            TabType.POST_DETAILS,
            scrollPosition
        )
    }

    fun onOpenUri(uri: Uri) {
        viewModelScope.launch { _routeEvents.emit(linkRouter.routeForLink(uri)) }
    }
}
