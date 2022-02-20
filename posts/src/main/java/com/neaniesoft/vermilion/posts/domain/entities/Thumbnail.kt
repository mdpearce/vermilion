package com.neaniesoft.vermilion.posts.domain.entities

import android.net.Uri

sealed class Thumbnail

object SelfThumbnail : Thumbnail()
object DefaultThumbnail : Thumbnail()
data class UriThumbnail(val uri: Uri) : Thumbnail()
