package com.neaniesoft.vermilion.posts.domain.entities

import android.net.Uri

sealed class Thumbnail(
    val identifier: String
)

object NoThumbnail : Thumbnail("none")
object SelfThumbnail : Thumbnail("self")
object DefaultThumbnail : Thumbnail("default")
object SpoilerThumbnail : Thumbnail("spoiler")
object NsfwThumbnail : Thumbnail("nsfw")
data class UriThumbnail(val uri: Uri) : Thumbnail(uri.toString())
