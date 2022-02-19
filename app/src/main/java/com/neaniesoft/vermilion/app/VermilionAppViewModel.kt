package com.neaniesoft.vermilion.app

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavDestination
import com.neaniesoft.vermilion.tabs.adapters.driving.ui.ActiveTab
import com.neaniesoft.vermilion.tabs.domain.TabSupervisor
import com.neaniesoft.vermilion.tabs.domain.entities.ParentId
import com.neaniesoft.vermilion.tabs.domain.entities.TabState
import com.neaniesoft.vermilion.tabs.domain.entities.TabType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class VermilionAppViewModel @Inject constructor(
    private val tabSupervisor: TabSupervisor
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
                        val tab = tabSupervisor.addNewPostDetailsTabIfNotExists(ParentId(id))
                        _activeTab.emit(ActiveTab.Tab(tab.id))
                    }
                }
                route.startsWith(VermilionScreen.Home.name) -> {
                    viewModelScope.launch { _activeTab.emit(ActiveTab.Home) }
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
            val route = "${VermilionScreen.PostDetails}/${parentId.value}"
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
}
