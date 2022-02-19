package com.neaniesoft.vermilion.tabs.domain.ports

import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.tabs.domain.entities.DisplayName
import com.neaniesoft.vermilion.tabs.domain.entities.NewTabState
import com.neaniesoft.vermilion.tabs.domain.entities.TabState
import kotlinx.coroutines.flow.Flow

interface TabRepository {
    val currentTabs: Flow<List<TabState>>

    suspend fun addNewTabIfNotExists(tab: NewTabState): TabState

    suspend fun removeTab(tab: TabState)

    suspend fun displayNameForPostDetails(postId: PostId): DisplayName
}
