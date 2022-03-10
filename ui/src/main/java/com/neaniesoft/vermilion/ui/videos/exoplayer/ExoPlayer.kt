package com.neaniesoft.vermilion.ui.videos.exoplayer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

@Composable
fun rememberExoPlayerState(
    initialMediaItem: MediaItem? = null,
    initialPosition: Long = 0,
    initialPlayWhenReady: Boolean = true,
    initialRepeatMode: Int = Player.REPEAT_MODE_OFF
): ExoPlayerState = rememberSaveable(saver = ExoPlayerState.Saver) {
    ExoPlayerState(
        initialMediaItem = initialMediaItem,
        initialPosition = initialPosition,
        initialPlayWhenReady = initialPlayWhenReady,
        initialRepeatMode = initialRepeatMode
    )
}

@Stable
class ExoPlayerState(
    initialMediaItem: MediaItem? = null,
    initialPosition: Long = 0,
    initialPlayWhenReady: Boolean = true,
    initialRepeatMode: Int = Player.REPEAT_MODE_OFF
) {
    var mediaItem: MediaItem? by mutableStateOf(initialMediaItem)

    var position: Long by mutableStateOf(initialPosition)
        private set

    var playWhenReady: Boolean by mutableStateOf(initialPlayWhenReady)

    var playbackState: Int by mutableStateOf(Player.STATE_IDLE)
        private set

    var isPlaying: Boolean by mutableStateOf(false)
        private set

    var duration: Long by mutableStateOf(C.TIME_UNSET)
        private set

    var repeatMode: Int by mutableStateOf(initialRepeatMode)

    internal fun onDurationUpdated(newDuration: Long) {
        duration = newDuration
    }

    internal fun onIsPlayingUpdated(newValue: Boolean) {
        isPlaying = newValue
    }

    internal fun onPlaybackStateChanged(newState: Int) {
        playbackState = newState
    }

    internal fun onPositionChanged(newPosition: Long) {
        position = newPosition
    }

    companion object {
        val Saver: Saver<ExoPlayerState, *> = listSaver(
            save = { state ->
                listOf(
                    state.mediaItem,
                    state.position,
                    state.playWhenReady,
                    state.repeatMode
                )
            },
            restore = {
                ExoPlayerState(
                    initialMediaItem = it[0] as MediaItem?,
                    initialPosition = it[1] as Long,
                    initialPlayWhenReady = it[2] as Boolean,
                    initialRepeatMode = it[3] as Int
                )
            }
        )
    }
}

@Composable
fun ExoPlayer(state: ExoPlayerState = rememberExoPlayerState()) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val exoPlayer = remember {
        com.google.android.exoplayer2.ExoPlayer.Builder(context).build()
    }

    LaunchedEffect(state.mediaItem) {
        val mediaItem = state.mediaItem
        if (mediaItem != null) {
            exoPlayer.setMediaItem(mediaItem)
            exoPlayer.seekTo(state.position)
        }
    }

    LaunchedEffect(state.repeatMode) {
        exoPlayer.repeatMode = state.repeatMode
    }

    LaunchedEffect(state.playWhenReady) {
        exoPlayer.playWhenReady = state.playWhenReady
    }
}
