package com.neaniesoft.vermilion.ui.videos.external

import android.net.Uri
import com.neaniesoft.vermilion.ui.videos.external.resolver.VideoResolverError

sealed class ExternalVideoDialogState {
    object Loading : ExternalVideoDialogState()
    data class ErrorState(val error: VideoResolverError) : ExternalVideoDialogState()
    data class PlayUriState(val uri: Uri) : ExternalVideoDialogState()
}
