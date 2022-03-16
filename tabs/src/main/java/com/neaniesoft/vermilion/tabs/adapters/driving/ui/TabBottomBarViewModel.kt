package com.neaniesoft.vermilion.tabs.adapters.driving.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.neaniesoft.vermilion.tabs.domain.TabSupervisor
import com.neaniesoft.vermilion.tabs.domain.entities.TabState
import com.neaniesoft.vermilion.uistate.TabType
import com.neaniesoft.vermilion.utils.logger
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TabBottomBarViewModel @Inject constructor(
    private val tabSupervisor: TabSupervisor
) : ViewModel() {
    private val logger by logger()
    val tabs = tabSupervisor.currentTabs

    val activeTab = tabSupervisor.activeTab

    private val _routeEvents: MutableSharedFlow<String> = MutableSharedFlow()
    val routeEvents = _routeEvents.asSharedFlow()

    fun onTabClicked(tabState: TabState) {
        logger.debugIfEnabled { "Tab clicked: $tabState" }
        viewModelScope.launch {
            val route = when (tabState.type) {
                TabType.HOME -> tabState.parentId.value
                TabType.POSTS -> "Posts/${tabState.parentId.value}"
                TabType.POST_DETAILS -> "PostDetails/${tabState.parentId.value}"
            }
            _routeEvents.emit(route)
        }
    }

    fun onHomeClicked() {
        viewModelScope.launch {
            _routeEvents.emit("Home")
        }
    }

    fun onTabCloseClicked(tabState: TabState) {
        viewModelScope.launch { tabSupervisor.removeTab(tabState) }
    }
}
