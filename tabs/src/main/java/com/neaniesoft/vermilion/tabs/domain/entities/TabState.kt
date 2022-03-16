package com.neaniesoft.vermilion.tabs.domain.entities

import com.neaniesoft.vermilion.coreentities.ScrollPosition
import com.neaniesoft.vermilion.uistate.TabType
import java.time.Instant

data class TabState(
    val id: TabId,
    val parentId: ParentId,
    val type: TabType,
    val displayName: DisplayName,
    val createdAt: Instant,
    val tabSortOrder: TabSortOrderIndex,
    val scrollPosition: ScrollPosition
)
