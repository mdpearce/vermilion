package com.neaniesoft.vermilion.uistate

import com.neaniesoft.vermilion.coreentities.ScrollPosition
import kotlinx.coroutines.flow.Flow

interface UiStateProvider {
    suspend fun scrollPositionTab(tabType: TabType, parentId: String): ScrollPosition?

    suspend fun updateScrollPositionForTab(
        tabType: TabType,
        parentId: String,
        scrollPosition: ScrollPosition
    )

    val activeTabClosedEvents: Flow<ActiveTabClosedEvent>

    suspend fun setActiveTab(tabType: TabType, parentId: String)
}

object ActiveTabClosedEvent
