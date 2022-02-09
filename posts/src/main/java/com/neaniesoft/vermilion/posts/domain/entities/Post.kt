package com.neaniesoft.vermilion.posts.domain.entities

import java.net.URL
import java.time.Instant

data class Post(
    val title: PostTitle,
    val summary: PostSummary,
    val community: Community,
    val authorName: AuthorName,
    val postedAt: Instant,
    val awardCounts: Map<Award, AwardCount>,
    val commentCount: CommentCount,
    val score: Score,
    val flags: Set<PostFlags>,
    val link: URL
)
