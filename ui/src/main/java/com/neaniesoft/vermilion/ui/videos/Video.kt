package com.neaniesoft.vermilion.ui.videos

import android.net.Uri

sealed class Video {
    data class DescriptorVideo(val descriptor: VideoDescriptor) : Video()
    data class UriVideo(val uri: Uri): Video()
}
