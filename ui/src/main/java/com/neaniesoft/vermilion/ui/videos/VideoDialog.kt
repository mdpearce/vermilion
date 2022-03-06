package com.neaniesoft.vermilion.ui.videos

import android.util.Log
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import com.neaniesoft.vermilion.ui.images.rememberZoomableState

@ExperimentalMaterialApi
@Composable
fun VideoDialog(videoDescriptor: VideoDescriptor, onDismiss: () -> Unit) {
    Log.d("VideoDialog", "Loading video: $videoDescriptor")
    val zoomableState = rememberZoomableState(maxScale = 6f)
    ZoomableDialog(state = zoomableState, onDismiss = onDismiss) {
        VideoPlayer(video = videoDescriptor)
    }
}
