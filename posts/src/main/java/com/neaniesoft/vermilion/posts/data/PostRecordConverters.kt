package com.neaniesoft.vermilion.posts.data

import androidx.core.net.toUri
import com.neaniesoft.vermilion.dbentities.posts.PostRecord
import com.neaniesoft.vermilion.dbentities.posts.PostType
import com.neaniesoft.vermilion.posts.domain.entities.AuthorName
import com.neaniesoft.vermilion.posts.domain.entities.CommentCount
import com.neaniesoft.vermilion.posts.domain.entities.CommunityName
import com.neaniesoft.vermilion.posts.domain.entities.DefaultThumbnail
import com.neaniesoft.vermilion.posts.domain.entities.FrontPage
import com.neaniesoft.vermilion.posts.domain.entities.ImagePostSummary
import com.neaniesoft.vermilion.posts.domain.entities.LinkHost
import com.neaniesoft.vermilion.posts.domain.entities.LinkPostSummary
import com.neaniesoft.vermilion.posts.domain.entities.NamedCommunity
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostFlags
import com.neaniesoft.vermilion.posts.domain.entities.PostFlair
import com.neaniesoft.vermilion.posts.domain.entities.PostFlairBackgroundColor
import com.neaniesoft.vermilion.posts.domain.entities.PostFlairText
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.domain.entities.PostTitle
import com.neaniesoft.vermilion.posts.domain.entities.PreviewText
import com.neaniesoft.vermilion.posts.domain.entities.Score
import com.neaniesoft.vermilion.posts.domain.entities.TextPostSummary
import com.neaniesoft.vermilion.posts.domain.entities.ThumbnailSummary
import com.neaniesoft.vermilion.posts.domain.entities.UriImage
import com.neaniesoft.vermilion.posts.domain.entities.VideoPostSummary
import org.commonmark.node.Document
import org.commonmark.parser.Parser
import java.time.Clock
import java.time.Instant
import java.util.UUID

fun PostRecord.toPost(markdownParser: Parser): Post {
    return Post(
        id = PostId(postId),
        title = PostTitle(title),
        summary = when (postType) {
            PostType.IMAGE -> ImagePostSummary(
                preview =
                UriImage(
                    previewUri?.toUri()
                        ?: throw IllegalStateException("Image post with no preview returned from db"),
                    previewWidth ?: 0,
                    previewHeight ?: 0

                ),
                thumbnail = thumbnailUri?.thumbnail() ?: DefaultThumbnail,
                LinkHost(linkHost),
                fullSizeUri = linkUri.toUri()
            )
            PostType.LINK -> LinkPostSummary(
                linkHost = LinkHost(linkHost),
                thumbnail = thumbnailUri?.thumbnail() ?: DefaultThumbnail,
                preview = previewUri?.let { uri ->
                    UriImage(
                        uri.toUri(),
                        previewWidth ?: 0,
                        previewHeight ?: 0
                    )
                }
            )
            PostType.TEXT -> TextPostSummary(
                previewText = PreviewText(previewText ?: ""),
                previewTextMarkdown = markdownParser.parse(previewText ?: "") as Document
            )
            PostType.VIDEO -> VideoPostSummary(
                preview =
                UriImage(
                    previewUri?.toUri()
                        ?: throw IllegalStateException("Video post with no preview returned from db"),
                    previewWidth ?: 0,
                    previewHeight ?: 0

                ),
                thumbnail = thumbnailUri?.thumbnail() ?: DefaultThumbnail,
                LinkHost(linkHost),
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
        link = linkUri.toUri(),
        flair = when (val text = flairText) {
            null -> {
                PostFlair.NoFlair
            }
            else -> {
                PostFlair.TextFlair(
                    PostFlairText(text),
                    PostFlairBackgroundColor(flairBackgroundColor)
                )
            }
        }
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
    linkHost = link.host ?: "",
    thumbnailUri = when (summary) {
        is ThumbnailSummary -> summary.thumbnail.identifier
        else -> null
    },
    previewUri = when (summary) {
        is ImagePostSummary -> summary.preview?.uri.toString()
        is VideoPostSummary -> summary.preview?.uri.toString()
        is LinkPostSummary -> summary.preview?.uri?.toString()
        else -> null
    },
    previewWidth = when (summary) {
        is ImagePostSummary -> summary.preview?.width
        is VideoPostSummary -> summary.preview?.width
        is LinkPostSummary -> summary.preview?.width
        else -> null
    },
    previewHeight = when (summary) {
        is ImagePostSummary -> summary.preview?.height
        is VideoPostSummary -> summary.preview?.height
        is LinkPostSummary -> summary.preview?.height
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
    flags = flags.joinToString(",") { it.name },
    flairText = when (flair) {
        is PostFlair.NoFlair -> null
        is PostFlair.TextFlair -> flair.text.value
    },
    flairBackgroundColor = when (flair) {
        is PostFlair.NoFlair -> 0
        is PostFlair.TextFlair -> flair.backgroundColor.value
    }
)
