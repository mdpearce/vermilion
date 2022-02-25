package com.neaniesoft.vermilion.tabs.domain.ports

import com.neaniesoft.vermilion.tabs.domain.entities.DisplayName
import com.neaniesoft.vermilion.tabs.domain.entities.NewTabState
import com.neaniesoft.vermilion.tabs.domain.entities.ParentId
import com.neaniesoft.vermilion.tabs.domain.entities.ScrollPosition
import com.neaniesoft.vermilion.tabs.domain.entities.TabState
import com.neaniesoft.vermilion.tabs.domain.entities.TabType
import kotlinx.coroutines.flow.Flow

interface TabRepository {
    val currentTabs: Flow<List<TabState>>

    suspend fun addNewTabIfNotExists(tab: NewTabState): TabState

    suspend fun removeTab(tab: TabState)

    suspend fun removeAll()

    suspend fun updateScrollStateForTab(
        parentId: ParentId,
        type: TabType,
        scrollPosition: ScrollPosition
    )

    suspend fun displayName(parentId: ParentId, type: TabType): DisplayName

    suspend fun findTab(parentId: ParentId, type: TabType): TabState?
}
