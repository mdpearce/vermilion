package com.neaniesoft.vermilion.posts.domain.entities

import java.net.URL
import java.time.Instant

data class Post(
    val title: PostTitle,
    val summary: PostSummary,
    val communityName: CommunityName,
    val communityIconUrl: URL?,
    val authorName: AuthorName,
    val postedAt: Instant,
    val commentCount: CommentCount,
    val score: PostScore,
    val link: URL
)

@JvmInline
value class PostScore(val value: Int)

@JvmInline
value class CommentCount(val value: Int)

@JvmInline
value class AuthorName(val value: String)

@JvmInline
value class CommunityName(val value: String)

@JvmInline
value class PostTitle(val value: String)

@JvmInline
value class LinkHost(val value: String)

sealed class PostSummary
data class ImagePostSummary(
    val thumbnailUrl: URL,
    val previewUrl: URL,
    val fullSizeUrl: URL
) : PostSummary()

data class GalleryPostSummary(
    val thumbnailUrl: URL,
    val previewURL: URL
)

data class LinkPostSummary(
    val linkHost: LinkHost
) : PostSummary()

data class TextPostSummary(
    val previewText: PreviewText
) : PostSummary()

@JvmInline
value class PreviewText(val value: String)