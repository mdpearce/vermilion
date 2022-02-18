package com.neaniesoft.vermilion.tabs.domain.ports

import com.neaniesoft.vermilion.tabs.domain.entities.TabState
import kotlinx.coroutines.flow.Flow

interface TabRepository {
    val currentTabs: Flow<List<TabState>>

    suspend fun addNewTab(tab: TabState)

    suspend fun removeTab(tab: TabState)
}
