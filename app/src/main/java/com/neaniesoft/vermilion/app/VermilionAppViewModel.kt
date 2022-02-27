package com.neaniesoft.vermilion.app

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination
import com.neaniesoft.vermilion.communities.data.database.CommunityRepository
import com.neaniesoft.vermilion.coreentities.Community
import com.neaniesoft.vermilion.tabs.domain.TabSupervisor
import com.neaniesoft.vermilion.tabs.domain.entities.ActiveTab
import com.neaniesoft.vermilion.tabs.domain.entities.ParentId
import com.neaniesoft.vermilion.tabs.domain.entities.TabState
import com.neaniesoft.vermilion.tabs.domain.entities.TabType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VermilionAppViewModel @Inject constructor(
    private val tabSupervisor: TabSupervisor,
    private val communityRepository: CommunityRepository
) : ViewModel() {
    val tabs = tabSupervisor.currentTabs

    private val _routeEvents = MutableSharedFlow<String>()
    val routeEvents: SharedFlow<String> = _routeEvents.asSharedFlow()

    private val _clearTabFromBackstackEvents = MutableSharedFlow<TabState>()
    val clearTabFromBackstackEvents = _clearTabFromBackstackEvents.asSharedFlow()

    private val _activeTab = MutableStateFlow<ActiveTab>(ActiveTab.None)
    val activeTab = _activeTab.asStateFlow()

    fun onNavigationEvent(destination: NavDestination, args: Bundle?) {
        val route = destination.route
        if (route != null) {
            when {
                route.startsWith(VermilionScreen.PostDetails.name) -> {
                    val id =
                        requireNotNull(args?.getString("id")) { "Received a post details route with no id" }
                    viewModelScope.launch(Dispatchers.IO) {
                        val tab =
                            tabSupervisor.addNewTabIfNotExists(ParentId(id), TabType.POST_DETAILS)
                        _activeTab.emit(ActiveTab.Tab(tab.id))
                    }
                }
                route.startsWith(VermilionScreen.Posts.name) -> {
                    val communityName =
                        requireNotNull(args?.getString("communityName")) { "Received a posts route with no name " }
                    viewModelScope.launch(Dispatchers.IO) {
                        val tab =
                            tabSupervisor.addNewTabIfNotExists(
                                ParentId(communityName),
                                TabType.POSTS
                            )
                        _activeTab.emit(ActiveTab.Tab(tab.id))
                    }
                }
                route.startsWith(VermilionScreen.Home.name) -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        tabSupervisor.addNewTabIfNotExists(
                            ParentId(VermilionScreen.Home.name),
                            TabType.HOME
                        )
                        _activeTab.emit(ActiveTab.Home)
                    }
                }
                else -> {
                    viewModelScope.launch { _activeTab.emit(ActiveTab.None) }
                }
            }
        }
    }

    fun onTabClicked(tab: TabState) {
        emitRouteEvent(tab.type, tab.parentId)
    }

    private fun emitRouteEvent(type: TabType, parentId: ParentId) {
        if (type == TabType.POST_DETAILS) {
            val route =
                "${VermilionScreen.PostDetails}/${parentId.value}"
            viewModelScope.launch { _routeEvents.emit(route) }
        } else if (type == TabType.POSTS) {
            val route =
                "${VermilionScreen.Posts}/${parentId.value}"
            viewModelScope.launch { _routeEvents.emit(route) }
        }
    }

    fun onTabCloseClicked(tab: TabState) {
        viewModelScope.launch {
            tabSupervisor.removeTab(tab)
            _clearTabFromBackstackEvents.emit(tab)
        }
    }

    fun onHomeButtonClicked() {
        viewModelScope.launch { _routeEvents.emit(VermilionScreen.Home.name) }
    }

    fun onUserButtonClicked() {
        viewModelScope.launch { _routeEvents.emit(VermilionScreen.MyAccount.name) }
    }

    fun onCommunityClicked(community: Community) {
        viewModelScope.launch { _routeEvents.emit("${VermilionScreen.Posts.name}/${community.routeName}") }
    }

    val subscribedCommunities: Flow<List<Community>> = communityRepository.subscribedCommunities()
}
