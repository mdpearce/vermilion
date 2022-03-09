package com.neaniesoft.vermilion.ui.videos.custom.youtube

import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.neaniesoft.vermilion.ui.images.rememberZoomableState
import com.neaniesoft.vermilion.ui.videos.ZoomableDialog

@ExperimentalMaterialApi
@Composable
fun YouTubeDialog(videoId: String, onDismiss: () -> Unit) {
    Log.d("YouTubeDialog", "Loading video: $videoId")
    val state = rememberZoomableState(maxScale = 6f)
    val youTubePlayerState = rememberYouTubePlayerState()
    ZoomableDialog(state = state, onDismiss = onDismiss) {
        YouTubePlayer(youTubePlayerState, videoId = videoId, modifier = Modifier.fillMaxSize())
    }
}
