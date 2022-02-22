package com.neaniesoft.vermilion.posts.domain.entities

import android.net.Uri
import org.commonmark.node.Document

sealed class PostSummary

data class ImagePostSummary(
    override val preview: UriImage?,
    override val thumbnail: Thumbnail,
    val linkHost: LinkHost,
    val fullSizeUri: Uri
) : PostSummary(), PreviewSummary, ThumbnailSummary

data class GalleryPostSummary(
    val thumbnailUri: Uri,
    val previewUri: Uri
)

data class LinkPostSummary(
    override val preview: UriImage?,
    override val thumbnail: Thumbnail,
    val linkHost: LinkHost
) : PostSummary(), PreviewSummary, ThumbnailSummary

data class TextPostSummary(
    val previewText: PreviewText,
    val previewTextMarkdown: Document
) : PostSummary()

data class UriImage(
    val uri: Uri,
    val width: Int,
    val height: Int
)

data class VideoPostSummary(
    override val preview: UriImage?,
    override val thumbnail: Thumbnail,
    val linkHost: LinkHost,
    val linkUri: Uri
) : PostSummary(), PreviewSummary, ThumbnailSummary

interface PreviewSummary {
    val preview: UriImage?
}

interface ThumbnailSummary {
    val thumbnail: Thumbnail
}
