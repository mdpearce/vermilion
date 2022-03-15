package com.neaniesoft.vermilion.tabs.domain.ports

import com.neaniesoft.vermilion.tabs.domain.entities.DisplayName
import com.neaniesoft.vermilion.tabs.domain.entities.NewTabState
import com.neaniesoft.vermilion.tabs.domain.entities.ParentId
import com.neaniesoft.vermilion.tabs.domain.entities.TabState
import com.neaniesoft.vermilion.uistate.TabType
import kotlinx.coroutines.flow.Flow

interface TabRepository {
    val currentTabs: Flow<List<TabState>>

    val activeTab: Flow<TabState?>

    suspend fun addNewTabIfNotExists(tab: NewTabState): TabState

    suspend fun removeTab(tab: TabState)

    suspend fun removeAll()

    suspend fun updateScrollStateForTab(
        parentId: ParentId,
        type: TabType,
        scrollPosition: com.neaniesoft.vermilion.coreentities.ScrollPosition
    )

    suspend fun displayName(parentId: ParentId, type: TabType): DisplayName

    suspend fun findTab(parentId: ParentId, type: TabType): TabState?
    suspend fun setActiveTab(parentId: ParentId, type: TabType)
}
