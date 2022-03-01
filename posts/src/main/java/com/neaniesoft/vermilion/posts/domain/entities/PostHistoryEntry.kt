package com.neaniesoft.vermilion.posts.domain.entities

import java.time.Instant

data class PostHistoryEntry(
    val postId: PostId,
    val viewedAt: Instant
)
