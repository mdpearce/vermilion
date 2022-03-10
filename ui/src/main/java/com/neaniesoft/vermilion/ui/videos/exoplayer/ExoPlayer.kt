package com.neaniesoft.vermilion.ui.videos.exoplayer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player

@Composable
fun rememberExoPlayerState(
    initialMediaItem: MediaItem,
    initialPosition: Long = 0,
    initialPlayWhenReady: Boolean = true,
): ExoPlayerState = rememberSaveable(saver = ExoPlayerState.Saver) {
    ExoPlayerState(
        initialMediaItem = initialMediaItem,
        initialPosition = initialPosition,
        initialPlayWhenReady = initialPlayWhenReady
    )
}

@Stable
class ExoPlayerState(
    initialMediaItem: MediaItem,
    initialPosition: Long = 0,
    initialPlayWhenReady: Boolean = true
) {
    var mediaItem: MediaItem by mutableStateOf(initialMediaItem)

    var position: Long by mutableStateOf(initialPosition)
        private set

    var playWhenReady: Boolean by mutableStateOf(initialPlayWhenReady)

    var playbackState: Int by mutableStateOf(Player.STATE_IDLE)
        private set

    var isPlaying: Boolean by mutableStateOf(false)
        private set

    var duration: Long by mutableStateOf(C.TIME_UNSET)
        private set

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
                    state.playWhenReady
                )
            },
            restore = {
                ExoPlayerState(
                    initialMediaItem = it[0] as MediaItem,
                    initialPosition = it[1] as Long,
                    initialPlayWhenReady = it[2] as Boolean
                )
            }
        )
    }
}

@Composable
fun ExoPlayer() {
}
