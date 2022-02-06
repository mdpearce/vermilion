package com.neaniesoft.vermilion.posts.domain.entities

import java.net.URL
import java.time.Instant

data class Post(
    val title: PostTitle,
    val summary: PostSummary,
    val communityName: CommunityName,
    val communityIconUrl: URL?,
    val authorName: AuthorName,
    val postedAt: Instant,
    val commentCount: CommentCount,
    val score: Score,
    val link: URL
)
