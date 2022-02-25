package com.neaniesoft.vermilion.tabs.domain

import com.neaniesoft.vermilion.tabs.domain.entities.NewTabState
import com.neaniesoft.vermilion.tabs.domain.entities.ParentId
import com.neaniesoft.vermilion.tabs.domain.entities.ScrollPosition
import com.neaniesoft.vermilion.tabs.domain.entities.TabState
import com.neaniesoft.vermilion.tabs.domain.entities.TabType
import com.neaniesoft.vermilion.tabs.domain.ports.TabRepository
import com.neaniesoft.vermilion.utils.CoroutinesModule
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.Instant
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TabSupervisor @Inject constructor(
    private val repository: TabRepository,
    private val clock: Clock,
    @Named(CoroutinesModule.IO_DISPATCHER) private val dispatcher: CoroutineDispatcher
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

    suspend fun addNewTabIfNotExists(parentId: ParentId, type: TabType): TabState {
        val displayName = repository.displayName(parentId, type)

        val tab = NewTabState(
            parentId,
            type,
            displayName,
            Instant.ofEpochMilli(clock.millis()),
            ScrollPosition(0, 0)
        )
        return repository.addNewTabIfNotExists(tab)
    }

    suspend fun removeTab(tab: TabState) {
        repository.removeTab(tab)
    }

    suspend fun updateScrollState(
        parentId: ParentId,
        type: TabType,
        scrollPosition: ScrollPosition
    ) {
        repository.updateScrollStateForTab(
            parentId,
            type,
            scrollPosition
        )
    }
}
