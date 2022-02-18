package com.neaniesoft.vermilion.tabs.domain.entities

import java.time.Instant

data class TabState(
    val id: TabId,
    val type: TabType,
    val route: Route,
    val displayName: DisplayName,
    val createdAt: Instant,
    val tabSortOrder: TabSortOrderIndex,
    val scrollPosition: ScrollPosition
)

@JvmInline
value class TabId(val value: Int)

@JvmInline
value class Route(val value: String)

enum class TabType {
    POSTS, POST_DETAILS
}

@JvmInline
value class DisplayName(val value: String)

@JvmInline
value class TabSortOrderIndex(val value: Int)

@JvmInline
value class ScrollPosition(val value: String)
