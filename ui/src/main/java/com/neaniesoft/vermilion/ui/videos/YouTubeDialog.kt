package com.neaniesoft.vermilion.ui.videos

import android.util.Log
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import com.neaniesoft.vermilion.ui.images.rememberZoomableState

@ExperimentalMaterialApi
@Composable
fun YouTubeDialog(videoId: String, onDismiss: () -> Unit) {
    Log.d("YouTubeDialog", "Loading video: $videoId")
    val state = rememberZoomableState(maxScale = 6f)
    ZoomableDialog(state = state, onDismiss = onDismiss) {
        YouTubePlayer(videoId = videoId)
    }
}