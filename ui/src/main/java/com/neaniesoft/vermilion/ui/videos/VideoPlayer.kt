package com.neaniesoft.vermilion.ui.videos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.ui.StyledPlayerView

@Composable
fun VideoPlayer(video: VideoDescriptor) {
    val context = LocalContext.current
    val player = remember {
        ExoPlayer.Builder(context).build()
    }
    val playerView = remember {
        StyledPlayerView(context)
    }
    val mediaItem = remember { MediaItem.fromUri(video.dash) }

    val playWhenReady = rememberSaveable {
        mutableStateOf(true)
    }

    player.setMediaItem(mediaItem)
    playerView.player = player
    LaunchedEffect(key1 = player) {
        player.prepare()
        player.playWhenReady = playWhenReady.value
    }

    AndroidView(factory = {
        playerView
    })
}
