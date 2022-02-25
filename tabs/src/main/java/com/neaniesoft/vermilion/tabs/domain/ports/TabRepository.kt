package com.neaniesoft.vermilion.tabs.domain.ports

import com.neaniesoft.vermilion.posts.domain.entities.PostId
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

    suspend fun displayNameForPostDetails(postId: PostId): DisplayName

    suspend fun removeAll()

    suspend fun updateScrollStateForTab(parentId: ParentId, type: TabType, scrollPosition: ScrollPosition)
}
