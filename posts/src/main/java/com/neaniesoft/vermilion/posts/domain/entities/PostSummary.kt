package com.neaniesoft.vermilion.posts.domain.entities

import android.net.Uri

sealed class PostSummary

data class ImagePostSummary(
    val linkHost: LinkHost,
    val thumbnailUri: Uri,
    val previews: List<UriImage>,
    val fullSizeUri: Uri
) : PostSummary()

data class GalleryPostSummary(
    val thumbnailUri: Uri,
    val previewUri: Uri
)

data class LinkPostSummary(
    val linkHost: LinkHost
) : PostSummary()

data class TextPostSummary(
    val previewText: PreviewText
) : PostSummary()

data class UriImage(
    val uri: Uri,
    val width: Int,
    val height: Int
)

data class VideoPostSummary(
    val linkHost: LinkHost,
    val thumbnailUri: Uri,
    val previews: List<UriImage>,
    val linkUri: Uri
) : PostSummary()
