package com.neaniesoft.vermilion.ui.videos.direct

import android.net.Uri
import android.util.Log
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import com.neaniesoft.vermilion.ui.images.rememberZoomableState
import com.neaniesoft.vermilion.ui.videos.ZoomableDialog
import com.neaniesoft.vermilion.ui.videos.exoplayer.ExoPlayerWithControls
import com.neaniesoft.vermilion.ui.videos.exoplayer.rememberExoPlayerState

@ExperimentalMaterialApi
@Composable
fun VideoDialog(uri: Uri, onDismiss: () -> Unit) {
    Log.d("VideoDialog", "Loading video: $uri")
    val zoomableState = rememberZoomableState(maxScale = 6f)
    val exoPlayerState = rememberExoPlayerState(
        initialMediaUri = uri
    )

    ZoomableDialog(state = zoomableState, onDismiss = onDismiss) {
        ExoPlayerWithControls(exoPlayerState)
    }
}
