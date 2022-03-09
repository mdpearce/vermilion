package com.neaniesoft.vermilion.posts.domain.routing

import android.net.Uri
import com.neaniesoft.vermilion.utils.logger
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomVideoRouter @Inject constructor(
    private val matchers: Set<@JvmSuppressWildcards CustomVideoRouteMatcher>
) {
    private val logger by logger()

    fun routeForVideoUri(uri: Uri): String? {
        matchers.forEach { matcher ->
            val result = matcher.match(uri)
            if (result is CustomVideoMatchResult.RouteMatch) {
                logger.debugIfEnabled { "Matched a direct video route: ${result.route}" }
                return result.route
            }
        }
        // No match
        return null
    }
}
