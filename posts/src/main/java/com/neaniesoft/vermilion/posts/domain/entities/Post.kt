package com.neaniesoft.vermilion.posts.domain.entities

import android.net.Uri
import com.neaniesoft.vermilion.coreentities.Community
import com.neaniesoft.vermilion.ui.videos.VideoDescriptor
import java.time.Instant

data class Post(
    val id: PostId,
    val title: PostTitle,
    val summary: PostSummary,
    val videoPreview: VideoDescriptor?,
    val community: Community,
    val authorName: AuthorName,
    val postedAt: Instant,
    val awardCounts: Map<Award, AwardCount>,
    val commentCount: CommentCount,
    val score: Score,
    val flags: Set<PostFlags>,
    val link: Uri,
    val flair: PostFlair
)

fun Post.isNsfw(): Boolean = flags.contains(PostFlags.NSFW)
