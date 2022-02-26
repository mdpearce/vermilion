package com.neaniesoft.vermilion.posts.domain.entities

import android.net.Uri
import java.time.Instant

data class Post(
    val id: PostId,
    val title: PostTitle,
    val summary: PostSummary,
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

sealed class PostFlair {
    object NoFlair : PostFlair()
    data class TextFlair(
        val text: PostFlairText,
        val backgroundColor: PostFlairBackgroundColor,
        val textColor: PostFlairTextColor
    ) : PostFlair()
}

enum class PostFlairTextColor {
    DARK, LIGHT
}

@JvmInline
value class PostFlairText(val value: String)

@JvmInline
value class PostFlairBackgroundColor(val value: Int)
