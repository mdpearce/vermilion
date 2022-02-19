package com.neaniesoft.vermilion.tabs.domain.entities

import java.time.Instant

data class NewTabState(
    val parentId: ParentId,
    val type: TabType,
    val displayName: DisplayName,
    val createdAt: Instant,
    val scrollPosition: ScrollPosition
)
