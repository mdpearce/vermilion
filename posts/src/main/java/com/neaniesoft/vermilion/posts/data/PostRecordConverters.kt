package com.neaniesoft.vermilion.posts.data

import androidx.core.net.toUri
import com.neaniesoft.vermilion.coreentities.CommunityId
import com.neaniesoft.vermilion.coreentities.CommunityName
import com.neaniesoft.vermilion.coreentities.FrontPage
import com.neaniesoft.vermilion.coreentities.NamedCommunity
import com.neaniesoft.vermilion.coreentities.UriImage
import com.neaniesoft.vermilion.db.PostQuery
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

fun PostQuery.toPost(markdownParser: Parser): Post {
    return Post(
        id = PostId(post_id),
        title = PostTitle(title),
        imagePreview = preview_uri?.let {
            UriImage(
                it.toUri(),
                preview_width?.toInt() ?: 0,
                preview_height?.toInt() ?: 0
            )
        },
        animatedImagePreview = animated_preview_uri?.let {
            AnimatedImagePreview(
                it.toUri(),
                animated_preview_width?.toInt() ?: 0,
                animated_preview_height?.toInt() ?: 0
            )
        },
        thumbnail = thumbnail_uri?.thumbnail() ?: DefaultThumbnail,
        linkHost = LinkHost(link_host),
        text = preview_text?.markdownText(markdownParser),
        videoPreview = if (preview_video_fallback == null) {
            null
        } else {
            VideoDescriptor(
                width = VideoWidth(requireNotNull(preview_video_width).toInt()),
                height = VideoHeight(requireNotNull(preview_video_height).toInt()),
                dash = requireNotNull(preview_video_dash).toUri(),
                hls = requireNotNull(preview_video_hls).toUri(),
                fallback = requireNotNull(preview_video_fallback).toUri()
            )
        },
        attachedVideo = if (video_fallback == null) {
            null
        } else {
            VideoDescriptor(
                width = VideoWidth(requireNotNull(video_width).toInt()),
                height = VideoHeight(requireNotNull(video_height).toInt()),
                dash = requireNotNull(video_dash).toUri(),
                hls = requireNotNull(video_hls).toUri(),
                fallback = requireNotNull(video_fallback).toUri()
            )
        },
        community = if (community_name == FrontPage.routeName) {
            FrontPage
        } else {
            NamedCommunity(CommunityName(community_name), CommunityId(community_id))
        },
        authorName = AuthorName(author_name),
        postedAt = Instant.ofEpochMilli(posted_at),
        awardCounts = emptyMap(), // TODO implement caching of awards
        commentCount = CommentCount(comment_count.toInt()),
        score = Score(score.toInt()),
        flags = flags.split(",").filter { it.isNotEmpty() }.map { PostFlags.valueOf(it) }.toSet()
            .plus(
                if (visited_at != null) {
                    setOf(PostFlags.VIEWED)
                } else {
                    emptySet()
                }
            ),
        link = link_uri.toUri(),
        flair = when (val text = flair_text) {
            null -> {
                PostFlair.NoFlair
            }
            else -> {
                PostFlair.TextFlair(
                    PostFlairText(text),
                    PostFlairBackgroundColor(flair_background_color.toInt()),
                    when (flair_text_color) {
                        PostFlairTextColor.DARK.name -> PostFlairTextColor.DARK
                        PostFlairTextColor.LIGHT.name -> PostFlairTextColor.LIGHT
                        else -> throw IllegalStateException("Unable to parse flair text color")
                    }
                )
            }
        },
        type = Post.Type.valueOf(post_type),
        gallery = gallery()
    )
}

fun com.neaniesoft.vermilion.db.Post.toPost(
    markdownParser: Parser,
    additionalFlags: Set<PostFlags> = emptySet()
): Post {
    return Post(
        id = PostId(post_id),
        title = PostTitle(title),
        imagePreview = preview_uri?.let {
            UriImage(
                it.toUri(),
                preview_width?.toInt() ?: 0,
                preview_height?.toInt() ?: 0
            )
        },
        animatedImagePreview = animated_preview_uri?.let {
            AnimatedImagePreview(
                it.toUri(),
                animated_preview_width?.toInt() ?: 0,
                animated_preview_height?.toInt() ?: 0
            )
        },
        thumbnail = thumbnail_uri?.thumbnail() ?: DefaultThumbnail,
        linkHost = LinkHost(link_host),
        text = preview_text?.markdownText(markdownParser),
        videoPreview = if (preview_video_fallback == null) {
            null
        } else {
            VideoDescriptor(
                width = VideoWidth(requireNotNull(preview_video_width).toInt()),
                height = VideoHeight(requireNotNull(preview_video_height).toInt()),
                dash = requireNotNull(preview_video_dash).toUri(),
                hls = requireNotNull(preview_video_hls).toUri(),
                fallback = requireNotNull(preview_video_fallback).toUri()
            )
        },
        attachedVideo = if (video_fallback == null) {
            null
        } else {
            VideoDescriptor(
                width = VideoWidth(requireNotNull(video_width).toInt()),
                height = VideoHeight(requireNotNull(video_height).toInt()),
                dash = requireNotNull(video_dash).toUri(),
                hls = requireNotNull(video_hls).toUri(),
                fallback = requireNotNull(video_fallback).toUri()
            )
        },
        community = if (community_name == FrontPage.routeName) {
            FrontPage
        } else {
            NamedCommunity(CommunityName(community_name), CommunityId(community_id))
        },
        authorName = AuthorName(author_name),
        postedAt = Instant.ofEpochMilli(posted_at),
        awardCounts = emptyMap(), // TODO implement caching of awards
        commentCount = CommentCount(comment_count.toInt()),
        score = Score(score.toInt()),
        flags = flags.split(",").filter { it.isNotEmpty() }.map { PostFlags.valueOf(it) }.toSet()
            .plus(additionalFlags),
        link = link_uri.toUri(),
        flair = when (val text = flair_text) {
            null -> {
                PostFlair.NoFlair
            }
            else -> {
                PostFlair.TextFlair(
                    PostFlairText(text),
                    PostFlairBackgroundColor(flair_background_color.toInt()),
                    when (flair_text_color) {
                        PostFlairTextColor.DARK.name -> PostFlairTextColor.DARK
                        PostFlairTextColor.LIGHT.name -> PostFlairTextColor.LIGHT
                        else -> throw IllegalStateException("Unable to parse flair text color")
                    }
                )
            }
        },
        type = Post.Type.valueOf(post_type),
        gallery = gallery()
    )
}

// TODO this is truly disgusting. Use a separate table!
private fun com.neaniesoft.vermilion.db.Post.gallery(): List<UriImage> {
    val uris = gallery_item_uris?.split(",")?.filterNot { it.isEmpty() } ?: emptyList()
    val widths =
        gallery_item_widths?.split(",")?.filterNot { it.isEmpty() }?.map { it.toInt() }
            ?: emptyList()
    val heights = gallery_item_heights?.split(",")?.filterNot { it.isEmpty() }?.map { it.toInt() }
        ?: emptyList()

    if (uris.size != widths.size || uris.size != heights.size) {
        throw IllegalStateException("Invalid serialized gallery, field counts do not match. uris: ${uris.size}, widths: ${widths.size}, heights: ${heights.size}")
    }

    return uris.mapIndexed { i, uri ->
        UriImage(uri.toUri(), width = widths[i], height = heights[i])
    }
}

// TODO this is truly disgusting. Use a separate table!
private fun PostQuery.gallery(): List<UriImage> {
    val uris = gallery_item_uris?.split(",")?.filterNot { it.isEmpty() } ?: emptyList()
    val widths =
        gallery_item_widths?.split(",")?.filterNot { it.isEmpty() }?.map { it.toInt() }
            ?: emptyList()
    val heights = gallery_item_heights?.split(",")?.filterNot { it.isEmpty() }?.map { it.toInt() }
        ?: emptyList()

    if (uris.size != widths.size || uris.size != heights.size) {
        throw IllegalStateException("Invalid serialized gallery, field counts do not match. uris: ${uris.size}, widths: ${widths.size}, heights: ${heights.size}")
    }

    return uris.mapIndexed { i, uri ->
        UriImage(uri.toUri(), width = widths[i], height = heights[i])
    }
}

fun Post.toPostSqlRecord(query: String, clock: Clock): com.neaniesoft.vermilion.db.Post =
    com.neaniesoft.vermilion.db.Post(
        id = 0L,
        post_id = id.value,
        query = query,
        inserted_at = clock.millis(),
        title = title.value,
        post_type = type.name,
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