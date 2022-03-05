package com.neaniesoft.vermilion.ui.images.matchers

import android.net.Uri

sealed class ImageHostMatchResult {
    object NoMatch : ImageHostMatchResult()
    data class DirectImageUri(val uri: Uri) : ImageHostMatchResult()
}
