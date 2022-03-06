package com.neaniesoft.vermilion.posts.data

import android.graphics.Color
import android.net.Uri
import androidx.core.net.toUri
import com.neaniesoft.vermilion.api.entities.Awarding
import com.neaniesoft.vermilion.api.entities.Link
import com.neaniesoft.vermilion.api.entities.Preview
import com.neaniesoft.vermilion.coreentities.CommunityId
import com.neaniesoft.vermilion.coreentities.CommunityName
import com.neaniesoft.vermilion.coreentities.NamedCommunity
import com.neaniesoft.vermilion.posts.domain.choosePreviewImage
import com.neaniesoft.vermilion.posts.domain.entities.AuthorName
import com.neaniesoft.vermilion.posts.domain.entities.Award
import com.neaniesoft.vermilion.posts.domain.entities.AwardCount
import com.neaniesoft.vermilion.posts.domain.entities.AwardName
import com.neaniesoft.vermilion.posts.domain.entities.CommentCount
import com.neaniesoft.vermilion.posts.domain.entities.DefaultThumbnail
import com.neaniesoft.vermilion.posts.domain.entities.ImagePostSummary
import com.neaniesoft.vermilion.posts.domain.entities.LinkHost
import com.neaniesoft.vermilion.posts.domain.entities.LinkPostSummary
import com.neaniesoft.vermilion.posts.domain.entities.NsfwThumbnail
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostFlags
import com.neaniesoft.vermilion.posts.domain.entities.PostFlair
import com.neaniesoft.vermilion.posts.domain.entities.PostFlairBackgroundColor
import com.neaniesoft.vermilion.posts.domain.entities.PostFlairText
import com.neaniesoft.vermilion.posts.domain.entities.PostFlairTextColor
import com.neaniesoft.vermilion.posts.domain.entities.PostId
import com.neaniesoft.vermilion.posts.domain.entities.PostSummary
import com.neaniesoft.vermilion.posts.domain.entities.PostTitle
import com.neaniesoft.vermilion.posts.domain.entities.PreviewText
import com.neaniesoft.vermilion.posts.domain.entities.Score
import com.neaniesoft.vermilion.posts.domain.entities.SelfThumbnail
import com.neaniesoft.vermilion.posts.domain.entities.SpoilerThumbnail
import com.neaniesoft.vermilion.posts.domain.entities.TextPostSummary
import com.neaniesoft.vermilion.posts.domain.entities.Thumbnail
import com.neaniesoft.vermilion.posts.domain.entities.UriImage
import com.neaniesoft.vermilion.posts.domain.entities.UriThumbnail
import com.neaniesoft.vermilion.posts.domain.entities.VideoPostSummary
import com.neaniesoft.vermilion.ui.videos.VideoDescriptor
import com.neaniesoft.vermilion.ui.videos.VideoHeight
import com.neaniesoft.vermilion.ui.videos.VideoWidth
import com.neaniesoft.vermilion.utils.anonymousLogger
import org.apache.commons.text.StringEscapeUtils
import org.commonmark.node.Document
import org.commonmark.parser.Parser
import java.net.URL
import java.time.Instant
import java.util.Locale
import kotlin.math.roundToLong

fun Link.toPost(markdownParser: Parser): Post {
    return Post(
        PostId(id),
        PostTitle(StringEscapeUtils.unescapeHtml4(title)),
        postSummary(markdownParser),
        videoPreview(),
        NamedCommunity(CommunityName(subreddit), CommunityId(subredditId)),
        AuthorName(author),
        Instant.ofEpochMilli((created * 1000.0).roundToLong()),
        allAwardings.toAwardsMap(),
        CommentCount(numComments),
        Score(score),
        flags(),
        url.toUri(),
        flair()
    )
}

internal fun Link.videoPreview(): VideoDescriptor? {
    val redditVideo = preview?.redditVideoPreview ?: return null

    return with(redditVideo) {
        VideoDescriptor(
            width = VideoWidth(width),
            height = VideoHeight(height),
            dash = dashUrl.toUri(),
            hls = hlsUrl.toUri(),
            fallback = fallbackUrl.toUri()
        )
    }
}

internal fun Link.flair(): PostFlair {
    val flairText = linkFlairText
    return if (!flairText.isNullOrEmpty()) {
        val bg = if (!linkFlairBackgroundColor.isNullOrEmpty()) {
            Color.parseColor(linkFlairBackgroundColor)
        } else {
            0
        }
        PostFlair.TextFlair(
            PostFlairText(flairText),
            PostFlairBackgroundColor(bg),
            PostFlairTextColor.valueOf(linkFlairTextColor.uppercase())
        )
    } else {
        PostFlair.NoFlair
    }
}

internal fun Link.postSummary(markdownParser: Parser): PostSummary {
    val hint = postHint?.lowercase(Locale.ENGLISH) ?: ""
    return when {
        hint.endsWith("image") -> {
            ImagePostSummary(
                preview?.uriImage(),
                thumbnail.thumbnail(),
                LinkHost(domain),
                Uri.parse(url)
            )
        }
        hint.endsWith("self") || selfText.isNotEmpty() -> {
            val text = StringEscapeUtils.unescapeHtml4(selfText)
            TextPostSummary(
                PreviewText(text),
                markdownParser.parse(text) as Document
            )
        }
        hint.endsWith("video") -> {
            VideoPostSummary(
                preview?.uriImage(),
                thumbnail.thumbnail(),
                LinkHost(domain),
                Uri.parse(url)
            )
        }
        hint.endsWith("link") -> {
            LinkPostSummary(
                preview?.uriImage(),
                thumbnail.thumbnail(),
                LinkHost(domain)
            )
        }
        else -> {
            val logger by anonymousLogger("postSummary()")
            logger.debugIfEnabled { "Unrecognised post hint ($hint), defaulting to link post type" }
            LinkPostSummary(
                preview?.uriImage(),
                thumbnail.thumbnail(),
                LinkHost(domain)
            )
        }
    }
}

internal fun Preview.uriImage(): UriImage? {
    return images.firstOrNull()?.resolutions?.map {
        UriImage(
            Uri.parse(StringEscapeUtils.unescapeHtml4(it.url)),
            it.width,
            it.height
        )
    }?.choosePreviewImage()
}

internal fun String.thumbnail(): Thumbnail {
    return when {
        this == "self" -> SelfThumbnail
        this == "default" -> DefaultThumbnail
        this == "spoiler" -> SpoilerThumbnail
        this == "nsfw" -> NsfwThumbnail
        this.isNotEmpty() -> UriThumbnail(this.toUri())
        else -> DefaultThumbnail
    }
}

internal fun List<Awarding>.toAwardsMap(): Map<Award, AwardCount> =
    associateBy(keySelector = { awarding ->
        Award(AwardName(awarding.name), URL(awarding.iconUrl))
    }, valueTransform = { awarding ->
            AwardCount(awarding.count)
        })

internal fun Link.flags(): Set<PostFlags> {
    return mutableSetOf<PostFlags>().apply {
        if (over18 == true) {
            add(PostFlags.NSFW)
        }
        if (stickied) {
            add(PostFlags.STICKIED)
        }
        if (saved) {
            add(PostFlags.SAVED)
        }
    }
}
