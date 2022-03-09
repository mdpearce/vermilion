package com.neaniesoft.vermilion.ui.videos.external.resolver

import android.net.Uri
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Result
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoUriResolverSupervisor @Inject constructor(
    private val resolvers: Set<@JvmSuppressWildcards VideoUriResolver>
) {
    fun canAnyResolverHandle(uri: Uri): Boolean {
        return resolvers.find { it.handles(uri) } != null
    }

    suspend fun resolve(uri: Uri): Result<Uri, VideoResolverError> {
        return resolvers.find { it.handles(uri) }?.resolve(uri) ?: Err(NoRegisteredResolvers)
    }
}
