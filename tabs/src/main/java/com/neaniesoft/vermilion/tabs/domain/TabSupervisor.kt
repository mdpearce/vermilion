package com.neaniesoft.vermilion.tabs.domain

import com.neaniesoft.vermilion.tabs.domain.entities.DisplayName
import com.neaniesoft.vermilion.tabs.domain.entities.Route
import com.neaniesoft.vermilion.tabs.domain.entities.ScrollPosition
import com.neaniesoft.vermilion.tabs.domain.entities.TabId
import com.neaniesoft.vermilion.tabs.domain.entities.TabSortOrderIndex
import com.neaniesoft.vermilion.tabs.domain.entities.TabState
import com.neaniesoft.vermilion.tabs.domain.entities.TabType
import com.neaniesoft.vermilion.tabs.domain.ports.TabRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TabSupervisor @Inject constructor(
    private val repository: TabRepository,
    private val clock: Clock,
    private val dispatcher: CoroutineDispatcher
) {
    private val _currentTabs: MutableStateFlow<List<TabState>> = MutableStateFlow(emptyList())
    val currentTabs: StateFlow<List<TabState>> = _currentTabs.asStateFlow()

    private val scope = CoroutineScope(dispatcher)

    init {
        scope.launch {
            repository.currentTabs.collect {
                _currentTabs.emit(it)
            }
        }
    }

    suspend fun addNewPostDetailsTab(route: Route, displayName: DisplayName) {
        val tab = TabState(
            TabId(0),
            TabType.POST_DETAILS,
            route,
            displayName,
            Instant.ofEpochMilli(clock.millis()),
            TabSortOrderIndex(0),
            ScrollPosition(0)
        )
        repository.addNewTab(tab)
    }
}
