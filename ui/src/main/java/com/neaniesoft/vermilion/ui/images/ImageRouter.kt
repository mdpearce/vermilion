package com.neaniesoft.vermilion.ui.images

import android.net.Uri
import com.neaniesoft.vermilion.ui.images.matchers.ImageHostMatchResult
import com.neaniesoft.vermilion.ui.images.matchers.ImageHostMatcher
import com.neaniesoft.vermilion.utils.logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImageRouter @Inject constructor(private val matchers: Set<@JvmSuppressWildcards ImageHostMatcher>) {
    private val logger by logger()

    fun directImageUriOrNull(uri: Uri): Uri? {
        matchers.forEach { matcher ->
            val result = matcher.match(uri)
            if (result is ImageHostMatchResult.DirectImageUri) {
                logger.debugIfEnabled { "Matched a direct image URI: ${result.uri}" }
                return result.uri
            }
        }
        return null
    }
}
