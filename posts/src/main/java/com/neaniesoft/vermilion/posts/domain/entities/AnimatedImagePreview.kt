package com.neaniesoft.vermilion.posts.domain.entities

import android.net.Uri

data class AnimatedImagePreview(
    val uri: Uri,
    val width: Int,
    val height: Int
)
