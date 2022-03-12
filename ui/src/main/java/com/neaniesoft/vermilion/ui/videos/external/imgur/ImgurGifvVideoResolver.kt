package com.neaniesoft.vermilion.ui.videos.external.imgur

import android.net.Uri
import androidx.core.net.toUri
import com.github.michaelbull.result.Err
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.neaniesoft.vermilion.ui.videos.external.resolver.InvalidUriForResolver
import com.neaniesoft.vermilion.ui.videos.external.resolver.VideoResolverError
import com.neaniesoft.vermilion.ui.videos.external.resolver.VideoUriResolver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ImgurGifvVideoResolver @Inject constructor() : VideoUriResolver {
    override suspend fun resolve(uri: Uri): Result<Uri, VideoResolverError> {
        return if (!handles(uri)) {
            Err(InvalidUriForResolver)
        } else {
            Ok(uri.toString().replace(".gifv", ".mp4").toUri())
        }
    }

    override fun handles(uri: Uri): Boolean {
        return uri.host == "i.imgur.com" && uri.path?.endsWith("gifv") == true
    }
}
