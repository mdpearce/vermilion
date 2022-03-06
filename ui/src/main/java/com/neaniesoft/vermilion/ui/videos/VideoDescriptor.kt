package com.neaniesoft.vermilion.ui.videos

import android.net.Uri

data class VideoDescriptor(
    val width: VideoWidth,
    val height: VideoHeight,
    val dash: Uri,
    val hls: Uri,
    val fallback: Uri
)
