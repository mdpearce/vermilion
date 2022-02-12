package com.neaniesoft.vermilion.posts.adapters.driven

import android.net.Uri
import com.neaniesoft.vermilion.api.entities.Awarding
import com.neaniesoft.vermilion.api.entities.Link
import com.neaniesoft.vermilion.api.entities.LinkThing
import com.neaniesoft.vermilion.posts.adapters.driven.http.PostsService
import com.neaniesoft.vermilion.posts.domain.entities.AfterKey
import com.neaniesoft.vermilion.posts.domain.entities.AuthorName
import com.neaniesoft.vermilion.posts.domain.entities.Award
import com.neaniesoft.vermilion.posts.domain.entities.AwardCount
import com.neaniesoft.vermilion.posts.domain.entities.AwardName
import com.neaniesoft.vermilion.posts.domain.entities.BeforeKey
import com.neaniesoft.vermilion.posts.domain.entities.CommentCount
import com.neaniesoft.vermilion.posts.domain.entities.Community
import com.neaniesoft.vermilion.posts.domain.entities.CommunityId
import com.neaniesoft.vermilion.posts.domain.entities.CommunityName
import com.neaniesoft.vermilion.posts.domain.entities.FirstSet
import com.neaniesoft.vermilion.posts.domain.entities.FrontPage
import com.neaniesoft.vermilion.posts.domain.entities.ImagePostSummary
import com.neaniesoft.vermilion.posts.domain.entities.LinkHost
import com.neaniesoft.vermilion.posts.domain.entities.ListingKey
import com.neaniesoft.vermilion.posts.domain.entities.NamedCommunity
import com.neaniesoft.vermilion.posts.domain.entities.Post
import com.neaniesoft.vermilion.posts.domain.entities.PostFlags
import com.neaniesoft.vermilion.posts.domain.entities.PostSummary
import com.neaniesoft.vermilion.posts.domain.entities.PostTitle
import com.neaniesoft.vermilion.posts.domain.entities.PreviewText
import com.neaniesoft.vermilion.posts.domain.entities.ResultSet
import com.neaniesoft.vermilion.posts.domain.entities.Score
import com.neaniesoft.vermilion.posts.domain.entities.TextPostSummary
import com.neaniesoft.vermilion.posts.domain.entities.UriImage
import com.neaniesoft.vermilion.posts.domain.entities.VideoPostSummary
import com.neaniesoft.vermilion.posts.domain.ports.PostRepository
import com.neaniesoft.vermilion.utils.logger
import org.apache.commons.text.StringEscapeUtils
import java.net.URL
import java.time.Instant
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.roundToLong

@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postsService: PostsService
) : PostRepository {

    private val logger by logger()

    override suspend fun postsForCommunity(
        community: Community,
        requestedCount: Int,
        previousCount: Int?,
        listingKey: ListingKey
    ): ResultSet<Post> {
        logger.debugIfEnabled { "Loading posts for $community" }
        val response = when (community) {
            is FrontPage -> {
                when (listingKey) {
                    is BeforeKey -> postsService.frontPageBest(
                        requestedCount,
                        listingKey.value,
                        null,
                        previousCount
                    )
                    is AfterKey -> postsService.frontPageBest(
                        requestedCount,
                        null,
                        listingKey.value,
                        previousCount
                    )
                    is FirstSet -> postsService.frontPageBest(requestedCount, null, null, null)
                }
            }
            is NamedCommunity -> TODO()
        }

        val posts = response.data.children.map { child ->
            if (child is LinkThing) {
                child.data.toPost()
            } else {
                throw IllegalArgumentException("Unknown thing type in posts response")
            }
        }

        return ResultSet(
            posts,
            response.data.before?.let { BeforeKey(it) },
            response.data.after?.let { AfterKey(it) },
            previousCount ?: 0
        )
    }
}

internal fun Link.toPost(): Post {
    return Post(
        PostTitle(title),
        postSummary(),
        NamedCommunity(CommunityName(subreddit), CommunityId(subredditId)),
        AuthorName(author),
        Instant.ofEpochMilli((created * 1000.0).roundToLong()),
        allAwardings.toAwardsMap(),
        CommentCount(numComments),
        Score(score),
        flags(),
        URL(url)
    )
}

internal fun Link.postSummary(): PostSummary {
    val hint = postHint?.lowercase(Locale.ENGLISH) ?: "self"
    return when {
        hint.endsWith("image") -> {
            ImagePostSummary(
                LinkHost(domain),
                Uri.parse(thumbnail),
                preview?.images?.first()?.resolutions?.sortedBy { image -> image.height }?.map {
                    UriImage(
                        Uri.parse(StringEscapeUtils.unescapeHtml4(it.url)),
                        it.width,
                        it.height
                    )
                } ?: emptyList(),
                Uri.parse(url)
            )
        }
        hint.endsWith("self") -> {
            TextPostSummary(
                PreviewText(selfText)
            )
        }
        hint.endsWith("video") -> {
            VideoPostSummary(
                LinkHost(domain),
                Uri.parse(thumbnail),
                preview?.images?.first()?.resolutions?.sortedBy { image -> image.height }?.map {
                    UriImage(
                        Uri.parse(StringEscapeUtils.unescapeHtml4(it.url)),
                        it.width,
                        it.height
                    )
                } ?: emptyList(),
                Uri.parse(url)
            )
        }
        else -> {
            TextPostSummary(PreviewText(""))
        }
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
