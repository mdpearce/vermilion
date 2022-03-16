package com.neaniesoft.vermilion.tabs.domain.entities

import com.neaniesoft.vermilion.uistate.TabType
import java.time.Instant

data class NewTabState(
    val parentId: ParentId,
    val type: TabType,
    val displayName: DisplayName,
    val createdAt: Instant,
    val scrollPosition: com.neaniesoft.vermilion.coreentities.ScrollPosition,
    val isActive: Boolean
)
