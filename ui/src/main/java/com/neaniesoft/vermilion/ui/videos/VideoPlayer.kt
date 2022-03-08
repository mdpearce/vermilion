package com.neaniesoft.vermilion.ui.videos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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
fun rememberVideoPlayerState(
    initialAutoPlay: Boolean = true,
    initialPosition: Long = 0L
): VideoPlayerState = rememberSaveable(
    saver = VideoPlayerState.Saver
) {
    VideoPlayerState(
        initialAutoPlay, initialPosition
    )
}

@Stable
class VideoPlayerState(
    initialAutoPlay: Boolean = true,
    initialPosition: Long = 0L
) {
    var autoPlay by mutableStateOf(initialAutoPlay)
        private set
    var position by mutableStateOf(initialPosition)
        private set

    internal fun onUpdatePositionFromPlayer(newPos: Long) {
        position = newPos
    }

    internal fun onUpdateAutoPlayStateFromPlayer(newValue: Boolean) {
        autoPlay = newValue
    }

    companion object {
        val Saver: Saver<VideoPlayerState, *> = listSaver(
            save = {
                listOf(
                    it.autoPlay,
                    it.position
                )
            },
            restore = {
                VideoPlayerState(it[0] as Boolean, it[1] as Long)
            }
        )
    }
}

@Composable
fun VideoPlayer(state: VideoPlayerState, video: VideoDescriptor, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val player = remember {
        ExoPlayer.Builder(context).build()
    }

    LaunchedEffect(key1 = video) {
        player.apply {
            repeatMode = Player.REPEAT_MODE_ONE
            playWhenReady = state.autoPlay
            setMediaItem(MediaItem.fromUri(video.dash))
            prepare()
            seekTo(state.position)
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            player.release()
        }
    }

    AndroidView(
        factory = {
            StyledPlayerView(it)
        },
        modifier = modifier
    ) { playerView ->
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                playerView.onResume()
            }

            override fun onStop(owner: LifecycleOwner) {
                state.onUpdateAutoPlayStateFromPlayer(player.playWhenReady)
                state.onUpdatePositionFromPlayer(player.currentPosition)
                playerView.onPause()
            }
        })

        playerView.apply {
            this.player = player
        }
    }
}
