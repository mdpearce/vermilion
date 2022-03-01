package com.neaniesoft.vermilion.posts.domain.entities

import com.neaniesoft.vermilion.posts.domain.entities.PostId
import java.time.Instant

data class PostHistoryEntry(
    val postId: PostId,
    val viewedAt: Instant
)
