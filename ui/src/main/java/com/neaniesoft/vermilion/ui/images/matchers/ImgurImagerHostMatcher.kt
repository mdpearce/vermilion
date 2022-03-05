package com.neaniesoft.vermilion.ui.images.matchers

import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImgurImagerHostMatcher @Inject constructor() : ImageHostMatcher {
    override fun match(uri: Uri): ImageHostMatchResult {
        return if (uri.host == "i.imgur.com") {
            ImageHostMatchResult.DirectImageUri(uri)
        } else {
            ImageHostMatchResult.NoMatch
        }
    }
}
