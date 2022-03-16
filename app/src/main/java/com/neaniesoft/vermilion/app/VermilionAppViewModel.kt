package com.neaniesoft.vermilion.app

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination
import com.neaniesoft.vermilion.accounts.domain.entities.UserAccount
import com.neaniesoft.vermilion.communities.data.database.CommunityRepository
import com.neaniesoft.vermilion.coreentities.Community
import com.neaniesoft.vermilion.uistate.TabType
import com.neaniesoft.vermilion.uistate.UiStateProvider
import com.neaniesoft.vermilion.utils.logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VermilionAppViewModel @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val uiStateProvider: UiStateProvider
) : ViewModel() {
    private val logger by logger()

    private val _routeEvents = MutableSharedFlow<String>()
    val routeEvents: SharedFlow<String> = _routeEvents.asSharedFlow()

    val currentTabRemovedEvents = uiStateProvider.activeTabClosedEvents

    fun onNavigationEvent(destination: NavDestination, args: Bundle?) {
        val route = destination.route
        if (route != null) {
            when {
                route.startsWith(VermilionScreen.PostDetails.name) -> {
                    val id =
                        requireNotNull(args?.getString("postId")) { "Received a post details route with no id" }
                    viewModelScope.launch(Dispatchers.IO) {
                        uiStateProvider.setActiveTab(TabType.POST_DETAILS, id)
                    }
                }
                route.startsWith(VermilionScreen.Posts.name) -> {
                    val communityName =
                        requireNotNull(args?.getString("communityName")) { "Received a posts route with no name " }
                    viewModelScope.launch(Dispatchers.IO) {
                        uiStateProvider.setActiveTab(TabType.POSTS, communityName)
                    }
                }
                route.startsWith(VermilionScreen.Home.name) -> {
                    viewModelScope.launch(Dispatchers.IO) {
                        uiStateProvider.setActiveTab(TabType.HOME, VermilionScreen.Home.name)
                    }
                }
                else -> {
                    logger.debugIfEnabled { "Received route was not a tab" }
                }
            }
        }
    }

    fun onUserButtonClicked() {
        viewModelScope.launch { _routeEvents.emit(VermilionScreen.MyAccount.name) }
    }

    fun onCommunityClicked(community: Community) {
        viewModelScope.launch { _routeEvents.emit("${VermilionScreen.Posts.name}/${community.routeName}") }
    }

    val subscribedCommunities: Flow<List<Community>> = communityRepository.subscribedCommunities()

    fun onUserChanged(userAccount: UserAccount?) {
        if (userAccount != null) {
            viewModelScope.launch(Dispatchers.IO) {
                communityRepository.updateSubscribedCommunities()
            }
        }
    }
}
