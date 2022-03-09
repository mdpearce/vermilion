package com.neaniesoft.vermilion.ui.videos.external.redgifs

import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.mapError
import com.github.michaelbull.result.runCatching
import com.neaniesoft.vermilion.ui.videos.external.resolver.ApiError
import com.neaniesoft.vermilion.ui.videos.external.resolver.InvalidUriForResolver
import com.neaniesoft.vermilion.ui.videos.external.resolver.VideoResolverError
import com.neaniesoft.vermilion.ui.videos.external.resolver.VideoUriResolver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RedGifsVideoUriResolver @Inject constructor(
    private val api: RedGifsApi
) : VideoUriResolver {
    override suspend fun resolve(uri: Uri): Result<Uri, VideoResolverError> {
        Log.d("RedGifsVideoUriResolver", "host: ${uri.host}, pathSegments: ${uri.pathSegments}")
        return if (handles(uri)) {
            val pathSegments = uri.pathSegments
            getVideoUri(pathSegments[1])
        } else {
            Err(InvalidUriForResolver)
        }
    }

    private suspend fun getVideoUri(id: String): Result<Uri, VideoResolverError> {
        return runCatching {
            api.getGif(id)
        }.mapError { ApiError(it) }
            .map { response ->
                response.gif.urls.hd.toUri()
            }
    }

    override fun handles(uri: Uri): Boolean {
        return (uri.host == "redgifs.com" || uri.host == "www.redgifs.com") &&
            uri.pathSegments.size == 2 &&
            uri.pathSegments[0] == "watch"
    }
}
