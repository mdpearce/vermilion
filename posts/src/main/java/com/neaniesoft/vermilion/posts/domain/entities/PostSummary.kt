package com.neaniesoft.vermilion.posts.domain.entities

import java.net.URL

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