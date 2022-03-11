package com.neaniesoft.vermilion.ui.videos.direct

import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.exoplayer2.MediaItem
import com.neaniesoft.vermilion.ui.images.rememberZoomableState
import com.neaniesoft.vermilion.ui.videos.ZoomableDialog
import com.neaniesoft.vermilion.ui.videos.exoplayer.ExoPlayer
import com.neaniesoft.vermilion.ui.videos.exoplayer.rememberExoPlayerState
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@ExperimentalMaterialApi
@Composable
fun VideoDialog(videoDescriptor: VideoDescriptor, onDismiss: () -> Unit) {
    Log.d("VideoDialog", "Loading video: $videoDescriptor")
    val zoomableState = rememberZoomableState(maxScale = 6f)
    val exoPlayerState = rememberExoPlayerState(
        initialMediaItem = MediaItem.fromUri(videoDescriptor.dash)
    )

    ZoomableDialog(state = zoomableState, onDismiss = onDismiss) {
        Box(contentAlignment = Alignment.Center) {
            ExoPlayer(exoPlayerState)
            Box(
                contentAlignment = Alignment.BottomEnd,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                VideoCounter(
                    exoPlayerState.secondsRemaining
                )
            }
        }
    }
}

@Composable
fun VideoCounter(secondsRemaining: Long) {
    val duration = secondsRemaining.toDuration(DurationUnit.SECONDS)
    val timeRemainingString = "%d:%02d".format(
        duration.inWholeMinutes,
        duration.toComponents { _, seconds, _ -> seconds })
    Text(text = timeRemainingString, modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.body1)
}
