package com.neaniesoft.vermilion.posts.domain.routing

import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YoutubeCustomVideoRouteMatcher @Inject constructor() : CustomVideoRouteMatcher {
    override fun match(linkUri: Uri): CustomVideoMatchResult {
        return when (linkUri.host) {
            "youtu.be", "youtube.com" -> {
                val videoId = linkUri.pathSegments.lastOrNull()
                if (videoId.isNullOrEmpty()) {
                    CustomVideoMatchResult.NoMatch
                } else {
                    CustomVideoMatchResult.RouteMatch("YouTube/$videoId")
                }
            }
            else -> CustomVideoMatchResult.NoMatch
        }
    }
}
