package com.neaniesoft.vermilion.posts.data

import androidx.core.net.toUri
import com.neaniesoft.vermilion.coreentities.CommunityId
import com.neaniesoft.vermilion.coreentities.CommunityName
import com.neaniesoft.vermilion.coreentities.FrontPage
import com.neaniesoft.vermilion.coreentities.NamedCommunity
import com.neaniesoft.vermilion.dbentities.posts.PostRecord
import com.neaniesoft.vermilion.dbentities.posts.PostType
import com.neaniesoft.vermilion.dbentities.posts.PostWithHistory
import com.neaniesoft.vermilion.posts.domain.entities.AuthorName
import com.neaniesoft.vermilion.posts.domain.entities.CommentCount
import com.neaniesoft.vermilion.posts.domain.entities.DefaultThumbnail
import com.neaniesoft.vermilion.posts.domain.entities.ImagePostSummary
import com.neaniesoft.vermilion.posts.domain.entities.LinkHost
import com.neaniesoft.vermilion.posts.domain.entities.LinkPostSummary
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostFlags
import com.neaniesoft.vermilion.posts.domain.entities.PostFlair
import com.neaniesoft.vermilion.posts.domain.entities.PostFlairBackgroundColor
import com.neaniesoft.vermilion.posts.domain.entities.PostFlairText
import com.neaniesoft.vermilion.posts.domain.entities.PostFlairTextColor
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.domain.entities.PostTitle
import com.neaniesoft.vermilion.posts.domain.entities.PreviewText
import com.neaniesoft.vermilion.posts.domain.entities.Score
import com.neaniesoft.vermilion.posts.domain.entities.TextPostSummary
import com.neaniesoft.vermilion.posts.domain.entities.ThumbnailSummary
import com.neaniesoft.vermilion.posts.domain.entities.UriImage
import com.neaniesoft.vermilion.posts.domain.entities.VideoPostSummary
import com.neaniesoft.vermilion.ui.videos.VideoDescriptor
import com.neaniesoft.vermilion.ui.videos.VideoHeight
import com.neaniesoft.vermilion.ui.videos.VideoWidth
import org.commonmark.node.Document
import org.commonmark.parser.Parser
import java.time.Clock
import java.time.Instant
import java.util.UUID

fun PostWithHistory.toPost(markdownParser: Parser): Post {
    return post.toPost(
        markdownParser,
        additionalFlags = if (history.isNotEmpty()) {
            setOf(PostFlags.VIEWED)
        } else {
            emptySet()
        }
    )
}

fun PostRecord.toPost(markdownParser: Parser, additionalFlags: Set<PostFlags> = emptySet()): Post {
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
        videoPreview = if (previewVideoFallback == null) {
            null
        } else {
            VideoDescriptor(
                width = VideoWidth(requireNotNull(previewVideoWidth)),
                height = VideoHeight(requireNotNull(previewVideoHeight)),
                dash = requireNotNull(previewVideoDash).toUri(),
                hls = requireNotNull(previewVideoHls).toUri(),
                fallback = requireNotNull(previewVideoFallback).toUri()
            )
        },
        attachedVideo = VideoDescriptor(
            width = VideoWidth(requireNotNull(videoWidth)),
            height = VideoHeight(requireNotNull(videoHeight)),
            dash = requireNotNull(videoDash).toUri(),
            hls = requireNotNull(videoHls).toUri(),
            fallback = requireNotNull(videoFallback).toUri()
        ),
        community = if (communityName == FrontPage.routeName) {
            FrontPage
        } else {
            NamedCommunity(CommunityName(communityName), CommunityId(communityId))
        },
        authorName = AuthorName(authorName),
        postedAt = Instant.ofEpochMilli(postedAt),
        awardCounts = emptyMap(), // TODO implement caching of awards
        commentCount = CommentCount(commentCount),
        score = Score(score),
        flags = flags.split(",").filter { it.isNotEmpty() }.map { PostFlags.valueOf(it) }.toSet()
            .plus(additionalFlags),
        link = linkUri.toUri(),
        flair = when (val text = flairText) {
            null -> {
                PostFlair.NoFlair
            }
            else -> {
                PostFlair.TextFlair(
                    PostFlairText(text),
                    PostFlairBackgroundColor(flairBackgroundColor),
                    when (flairTextColor) {
                        PostFlairTextColor.DARK.name -> PostFlairTextColor.DARK
                        PostFlairTextColor.LIGHT.name -> PostFlairTextColor.LIGHT
                        else -> throw IllegalStateException("Unable to parse flair text color")
                    }
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
    previewVideoWidth = videoPreview?.width?.value,
    previewVideoHeight = videoPreview?.height?.value,
    previewVideoDash = videoPreview?.dash?.toString(),
    previewVideoHls = videoPreview?.hls?.toString(),
    previewVideoFallback = videoPreview?.fallback?.toString(),
    videoWidth = attachedVideo?.width?.value,
    videoHeight = attachedVideo?.height?.value,
    videoDash = attachedVideo?.dash?.toString(),
    videoHls = attachedVideo?.hls?.toString(),
    videoFallback = attachedVideo?.fallback?.toString(),
    communityName = community.routeName,
    communityId = (community as? NamedCommunity)?.id?.value ?: "",
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
    },
    flairTextColor = when (flair) {
        is PostFlair.NoFlair -> PostFlairTextColor.DARK.name
        is PostFlair.TextFlair -> flair.textColor.name
    }
)
