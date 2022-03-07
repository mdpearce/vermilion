package com.neaniesoft.vermilion.ui.videos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.ui.StyledPlayerView

@Composable
fun VideoPlayer(video: VideoDescriptor) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // TODO This should be hoisted out to a state object for observation
    var autoPlay by rememberSaveable { mutableStateOf(true) }
    var window by rememberSaveable { mutableStateOf(0) }
    var position by rememberSaveable { mutableStateOf(0L) }

    val player = remember {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(video.dash))
            prepare()
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = autoPlay
            seekTo(window, position)
        }
    }

    fun updateState() {
        autoPlay = player.playWhenReady
        window = player.currentMediaItemIndex
        position = 0L.coerceAtLeast(player.contentPosition) // ??
    }

    val playerView = remember {
        StyledPlayerView(context).also { view ->
            lifecycle.addObserver(object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) {
                    view.onResume()
                    player.playWhenReady = autoPlay
                }

                override fun onStop(owner: LifecycleOwner) {
                    updateState()
                    view.onPause()
                    player.playWhenReady = false
                }
            })
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            updateState()
            player.release()
        }
    }

    AndroidView(factory = {
        playerView
    }, update = {
        playerView.player = player
    })
}
