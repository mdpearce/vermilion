package com.neaniesoft.vermilion.posts.domain.entities

import android.net.Uri
import com.neaniesoft.vermilion.coreentities.Community
import com.neaniesoft.vermilion.ui.videos.direct.VideoDescriptor
import java.time.Instant

data class Post(
    val id: PostId,
    val title: PostTitle,
    val imagePreview: UriImage?,
    val animatedImagePreview: AnimatedImagePreview?,
    val thumbnail: Thumbnail,
    val linkHost: LinkHost,
    val text: MarkdownText?,
    val videoPreview: VideoDescriptor?,
    val attachedVideo: VideoDescriptor?,
    val community: Community,
    val authorName: AuthorName,
    val postedAt: Instant,
    val awardCounts: Map<Award, AwardCount>,
    val commentCount: CommentCount,
    val score: Score,
    val flags: Set<PostFlags>,
    val link: Uri,
    val flair: PostFlair,
    val type: Type
) {
    enum class Type {
        TEXT, IMAGE, LINK, VIDEO
    }
}

fun Post.isNsfw(): Boolean = flags.contains(PostFlags.NSFW)
