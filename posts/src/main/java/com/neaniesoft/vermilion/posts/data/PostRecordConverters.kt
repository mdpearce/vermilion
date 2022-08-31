package com.neaniesoft.vermilion.posts.data

import androidx.core.net.toUri
import com.neaniesoft.vermilion.coreentities.CommunityId
import com.neaniesoft.vermilion.coreentities.CommunityName
import com.neaniesoft.vermilion.coreentities.FrontPage
import com.neaniesoft.vermilion.coreentities.NamedCommunity
import com.neaniesoft.vermilion.coreentities.UriImage
import com.neaniesoft.vermilion.dbentities.posts.PostRecord
import com.neaniesoft.vermilion.dbentities.posts.PostType
import com.neaniesoft.vermilion.dbentities.posts.PostWithHistory
import com.neaniesoft.vermilion.posts.domain.entities.AnimatedImagePreview
import com.neaniesoft.vermilion.posts.domain.entities.AuthorName
import com.neaniesoft.vermilion.posts.domain.entities.CommentCount
import com.neaniesoft.vermilion.posts.domain.entities.DefaultThumbnail
import com.neaniesoft.vermilion.posts.domain.entities.LinkHost
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostFlags
import com.neaniesoft.vermilion.posts.domain.entities.PostFlair
import com.neaniesoft.vermilion.posts.domain.entities.PostFlairBackgroundColor
import com.neaniesoft.vermilion.posts.domain.entities.PostFlairText
import com.neaniesoft.vermilion.posts.domain.entities.PostFlairTextColor
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.domain.entities.PostTitle
import com.neaniesoft.vermilion.posts.domain.entities.Score
import com.neaniesoft.vermilion.ui.videos.direct.VideoDescriptor
import com.neaniesoft.vermilion.ui.videos.direct.VideoHeight
import com.neaniesoft.vermilion.ui.videos.direct.VideoWidth
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
        imagePreview = previewUri?.let {
            UriImage(
                it.toUri(),
                previewWidth ?: 0,
                previewHeight ?: 0
            )
        },
        animatedImagePreview = animatedPreviewUri?.let {
            AnimatedImagePreview(
                it.toUri(),
                animatedPreviewWidth ?: 0,
                animatedPreviewHeight ?: 0
            )
        },
        thumbnail = thumbnailUri?.thumbnail() ?: DefaultThumbnail,
        linkHost = LinkHost(linkHost),
        text = previewText?.markdownText(markdownParser),
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
        attachedVideo = if (videoFallback == null) {
            null
        } else {
            VideoDescriptor(
                width = VideoWidth(requireNotNull(videoWidth)),
                height = VideoHeight(requireNotNull(videoHeight)),
                dash = requireNotNull(videoDash).toUri(),
                hls = requireNotNull(videoHls).toUri(),
                fallback = requireNotNull(videoFallback).toUri()
            )
        },
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
        },
        type = when (postType) {
            PostType.TEXT -> Post.Type.TEXT
            PostType.IMAGE -> Post.Type.IMAGE
            PostType.LINK -> Post.Type.LINK
            PostType.VIDEO -> Post.Type.VIDEO
            PostType.GALLERY -> Post.Type.GALLERY
        },
        gallery = gallery()
    )
}

// TODO this is truly disgusting. Use a separate table!
private fun PostRecord.gallery(): List<UriImage> {
    val uris = galleryItemUris?.split(",")?.filterNot { it.isEmpty() } ?: emptyList()
    val widths =
        galleryItemWidths?.split(",")?.filterNot { it.isEmpty() }?.map { it.toInt() } ?: emptyList()
    val heights = galleryItemHeights?.split(",")?.filterNot { it.isEmpty() }?.map { it.toInt() }
        ?: emptyList()

    if (uris.size != widths.size || uris.size != heights.size) {
        throw IllegalStateException("Invalid serialized gallery, field counts do not match. uris: ${uris.size}, widths: ${widths.size}, heights: ${heights.size}")
    }

    return uris.mapIndexed { i, uri ->
        UriImage(uri.toUri(), width = widths[i], height = heights[i])
    }
}

fun Post.toPostRecord(query: String, clock: Clock): PostRecord = PostRecord(
    id = UUID.randomUUID().toString(),
    postId = id.value,
    query = query,
    insertedAt = clock.millis(),
    title = title.value,
    postType = when (type) {
        Post.Type.TEXT -> PostType.TEXT
        Post.Type.IMAGE -> PostType.IMAGE
        Post.Type.LINK -> PostType.LINK
        Post.Type.VIDEO -> PostType.VIDEO
        Post.Type.GALLERY -> PostType.GALLERY
    },
    linkHost = link.host ?: "",
    thumbnailUri = thumbnail.identifier,
    previewUri = imagePreview?.uri?.toString(),
    previewWidth = imagePreview?.width,
    previewHeight = imagePreview?.height,
    linkUri = link.toString(),
    previewText = text?.raw,
    previewVideoWidth = videoPreview?.width?.value,
    previewVideoHeight = videoPreview?.height?.value,
    previewVideoDash = videoPreview?.dash?.toString(),
    previewVideoHls = videoPreview?.hls?.toString(),
    previewVideoFallback = videoPreview?.fallback?.toString(),
    animatedPreviewWidth = animatedImagePreview?.width,
    animatedPreviewHeight = animatedImagePreview?.height,
    animatedPreviewUri = animatedImagePreview?.uri?.toString(),
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
    },
    galleryItemUris = gallery.joinToString(",") { it.uri.toString() },
    galleryItemWidths = gallery.joinToString(",") { it.width.toString() },
    galleryItemHeights = gallery.joinToString(",") { it.height.toString() }
)

fun Post.toPostSqlRecord(query: String, clock: Clock): com.neaniesoft.vermilion.db.Post = com.neaniesoft.vermilion.db.Post(
    id = 0,
    post_id = id.value,
    query = query,
    inserted_at = clock.millis(),
    title = title.value,
    post_type = when (type) {
        Post.Type.TEXT -> PostType.TEXT.name
        Post.Type.IMAGE -> PostType.IMAGE.name
        Post.Type.LINK -> PostType.LINK.name
        Post.Type.VIDEO -> PostType.VIDEO.name
        Post.Type.GALLERY -> PostType.GALLERY.name
    },
    link_host = link.host ?: "",
    thumbnail_uri = thumbnail.identifier,
    preview_uri = imagePreview?.uri?.toString(),
    preview_width = imagePreview?.width?.toLong(),
    preview_height = imagePreview?.height?.toLong(),
    link_uri = link.toString(),
    preview_text = text?.raw,
    preview_video_width = videoPreview?.width?.value?.toLong(),
    preview_video_height = videoPreview?.height?.value?.toLong(),
    preview_video_dash = videoPreview?.dash?.toString(),
    preview_video_hls = videoPreview?.hls?.toString(),
    preview_video_fallback = videoPreview?.fallback?.toString(),
    animated_preview_width = animatedImagePreview?.width?.toLong(),
    animated_preview_height = animatedImagePreview?.height?.toLong(),
    animated_preview_uri = animatedImagePreview?.uri?.toString(),
    video_width = attachedVideo?.width?.value?.toLong(),
    video_height = attachedVideo?.height?.value?.toLong(),
    video_dash = attachedVideo?.dash?.toString(),
    video_hls = attachedVideo?.hls?.toString(),
    video_fallback = attachedVideo?.fallback?.toString(),
    community_name = community.routeName,
    community_id = (community as? NamedCommunity)?.id?.value ?: "",
    author_name = authorName.value,
    posted_at = postedAt.toEpochMilli(),
    comment_count = commentCount.value.toLong(),
    score = score.value.toLong(),
    flags = flags.joinToString(",") { it.name },
    flair_text = when (flair) {
        is PostFlair.NoFlair -> null
        is PostFlair.TextFlair -> flair.text.value
    },
    flair_background_color = when (flair) {
        is PostFlair.NoFlair -> 0L
        is PostFlair.TextFlair -> flair.backgroundColor.value.toLong()
    },
    flair_text_color = when (flair) {
        is PostFlair.NoFlair -> PostFlairTextColor.DARK.name
        is PostFlair.TextFlair -> flair.textColor.name
    },
    gallery_item_uris = gallery.joinToString(",") { it.uri.toString() },
    gallery_item_widths = gallery.joinToString(",") { it.width.toString() },
    gallery_item_heights = gallery.joinToString(",") { it.height.toString() }
)