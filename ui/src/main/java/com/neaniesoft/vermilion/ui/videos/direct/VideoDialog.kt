package com.neaniesoft.vermilion.ui.videos.direct

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.neaniesoft.vermilion.ui.images.rememberZoomableState
import com.neaniesoft.vermilion.ui.videos.Video
import com.neaniesoft.vermilion.ui.videos.VideoPlayer
import com.neaniesoft.vermilion.ui.videos.ZoomableDialog
import com.neaniesoft.vermilion.ui.videos.rememberVideoPlayerState

@ExperimentalMaterialApi
@Composable
fun VideoDialog(videoDescriptor: VideoDescriptor, onDismiss: () -> Unit) {
    Log.d("VideoDialog", "Loading video: $videoDescriptor")
    val zoomableState = rememberZoomableState(maxScale = 6f)
    val videoPlayerState = rememberVideoPlayerState()
    ZoomableDialog(state = zoomableState, onDismiss = onDismiss) {
        VideoPlayer(
            state = videoPlayerState,
            video = Video.DescriptorVideo(videoDescriptor),
            modifier = Modifier.fillMaxSize()
        )
    }
}
