package com.neaniesoft.vermilion.posts.data

import androidx.core.net.toUri
import com.neaniesoft.vermilion.dbentities.posts.PostRecord
import com.neaniesoft.vermilion.dbentities.posts.PostType
import com.neaniesoft.vermilion.posts.domain.entities.AuthorName
import com.neaniesoft.vermilion.posts.domain.entities.CommentCount
import com.neaniesoft.vermilion.posts.domain.entities.CommunityName
import com.neaniesoft.vermilion.posts.domain.entities.FrontPage
import com.neaniesoft.vermilion.posts.domain.entities.ImagePostSummary
import com.neaniesoft.vermilion.posts.domain.entities.LinkHost
import com.neaniesoft.vermilion.posts.domain.entities.LinkPostSummary
import com.neaniesoft.vermilion.posts.domain.entities.NamedCommunity
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostFlags
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.domain.entities.PostTitle
import com.neaniesoft.vermilion.posts.domain.entities.PreviewText
import com.neaniesoft.vermilion.posts.domain.entities.Score
import com.neaniesoft.vermilion.posts.domain.entities.TextPostSummary
import com.neaniesoft.vermilion.posts.domain.entities.UriImage
import com.neaniesoft.vermilion.posts.domain.entities.VideoPostSummary
import java.net.URL
import java.time.Clock
import java.time.Instant
import java.util.UUID

fun PostRecord.toPost(): Post {
    return Post(
        id = PostId(postId),
        title = PostTitle(title),
        summary = when (postType) {
            PostType.IMAGE -> ImagePostSummary(
                LinkHost(linkHost),
                thumbnailUri = thumbnailUri?.toUri()
                    ?: throw IllegalStateException("Image post with no thumbnail returned from db"),
                preview =
                UriImage(
                    previewUri?.toUri()
                        ?: throw IllegalStateException("Image post with no preview returned from db"),
                    previewWidth ?: 0,
                    previewHeight ?: 0

                ),
                fullSizeUri = linkUri.toUri()
            )
            PostType.LINK -> TODO()
            PostType.TEXT -> TextPostSummary(
                previewText = PreviewText(previewText ?: "")
            )
            PostType.VIDEO -> VideoPostSummary(
                LinkHost(linkHost),
                thumbnailUri = thumbnailUri?.toUri()
                    ?: throw IllegalStateException("Video post with no thumbnail returned from db"),
                preview =
                UriImage(
                    previewUri?.toUri()
                        ?: throw IllegalStateException("Video post with no preview returned from db"),
                    previewWidth ?: 0,
                    previewHeight ?: 0

                ),
                linkUri = linkUri.toUri()
            )
        },
        community = if (communityName == FrontPage.routeName) {
            FrontPage
        } else {
            NamedCommunity(CommunityName(communityName))
        },
        authorName = AuthorName(authorName),
        postedAt = Instant.ofEpochMilli(postedAt),
        awardCounts = emptyMap(), // TODO implement caching of awards
        commentCount = CommentCount(commentCount),
        score = Score(score),
        flags = flags.split(",").filter { it.isNotEmpty() }.map { PostFlags.valueOf(it) }.toSet(),
        link = URL(linkUri)
    )
}

fun Post.toPostRecord(query: String, clock: Clock): PostRecord = PostRecord(
    id = UUID.randomUUID().toString(),
    postId = id.value,
    query = query,
    insertedAt = clock.millis(),
    title = title.value,
    postType = when (summary) {
        is TextPostSummary -> PostType.TEXT
        is ImagePostSummary -> PostType.IMAGE
        is LinkPostSummary -> PostType.LINK
        is VideoPostSummary -> PostType.VIDEO
    },
    linkHost = link.host,
    thumbnailUri = when (summary) {
        is ImagePostSummary -> summary.thumbnailUri.toString()
        is VideoPostSummary -> summary.thumbnailUri.toString()
        else -> null
    },
    previewUri = when (summary) {
        is ImagePostSummary -> summary.preview?.uri.toString()
        is VideoPostSummary -> summary.preview?.uri.toString()
        else -> null
    },
    previewWidth = when (summary) {
        is ImagePostSummary -> summary.preview?.width
        is VideoPostSummary -> summary.preview?.width
        else -> null
    },
    previewHeight = when (summary) {
        is ImagePostSummary -> summary.preview?.height
        is VideoPostSummary -> summary.preview?.height
        else -> null
    },
    linkUri = link.toString(),
    previewText = when (summary) {
        is TextPostSummary -> summary.previewText.value
        else -> null
    },
    communityName = community.routeName,
    authorName = authorName.value,
    postedAt = postedAt.toEpochMilli(),
    commentCount = commentCount.value,
    score = score.value,
    flags = flags.joinToString(",") { it.name }
)
