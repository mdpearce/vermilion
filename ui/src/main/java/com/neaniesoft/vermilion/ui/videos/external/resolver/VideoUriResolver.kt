package com.neaniesoft.vermilion.ui.videos.external.resolver

import android.net.Uri
import com.github.michaelbull.result.Result

interface VideoUriResolver {
    suspend fun resolve(uri: Uri): Result<Uri, VideoResolverError>
    fun handles(uri: Uri): Boolean
}
