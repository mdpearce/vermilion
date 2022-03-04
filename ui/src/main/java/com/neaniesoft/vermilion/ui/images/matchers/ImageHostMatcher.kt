package com.neaniesoft.vermilion.ui.images.matchers

import android.net.Uri

interface ImageHostMatcher {
    fun match(uri: Uri): ImageHostMatchResult
}
