package com.neaniesoft.vermilion.ui.videos.exoplayer

import android.os.Bundle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import com.google.android.exoplayer2.C
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.PlaybackException
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.STATE_READY
import com.google.android.exoplayer2.Timeline
import com.google.android.exoplayer2.ui.StyledPlayerView
import com.neaniesoft.vermilion.utils.anonymousLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.ceil

@Composable
fun rememberExoPlayerState(
    initialMediaItem: MediaItem? = null,
    initialPosition: Long = 0,
    initialPlayWhenReady: Boolean = true,
    initialRepeatMode: Int = Player.REPEAT_MODE_OFF,
    initialMuteState: Boolean = false
): ExoPlayerState = rememberSaveable(saver = ExoPlayerState.Saver) {
    ExoPlayerState(
        initialMediaItem = initialMediaItem,
        initialPosition = initialPosition,
        initialPlayWhenReady = initialPlayWhenReady,
        initialRepeatMode = initialRepeatMode,
        initialMuteState = initialMuteState
    )
}

@Stable
class ExoPlayerState(
    initialMediaItem: MediaItem? = null,
    initialPosition: Long = 0,
    initialPlayWhenReady: Boolean = true,
    initialRepeatMode: Int = Player.REPEAT_MODE_OFF,
    initialMuteState: Boolean = false
) {
    var mediaItem: MediaItem? by mutableStateOf(initialMediaItem)

    var position: Long by mutableStateOf(initialPosition)
        private set

    var playWhenReady: Boolean by mutableStateOf(initialPlayWhenReady)

    var playbackState: Int by mutableStateOf(Player.STATE_IDLE)
        private set

    var duration: Long by mutableStateOf(C.TIME_UNSET)
        private set

    var repeatMode: Int by mutableStateOf(initialRepeatMode)

    var isMuted: Boolean by mutableStateOf(initialMuteState)

    val millisRemaining: Long by derivedStateOf {
        duration.coerceAtLeast(0) - position
    }

    val secondsRemaining: Long by derivedStateOf {
        ceil(millisRemaining / 1000.0).toLong()
    }

    internal fun onDurationUpdated(newDuration: Long) {
        duration = newDuration
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
                    state.mediaItem?.toBundle(),
                    state.position,
                    state.playWhenReady,
                    state.repeatMode,
                    state.isMuted
                )
            },
            restore = {
                val mediaItemBundle = it[0] as Bundle?
                val mediaItem =
                    mediaItemBundle?.let { bundle -> MediaItem.CREATOR.fromBundle(bundle) }
                ExoPlayerState(
                    initialMediaItem = mediaItem,
                    initialPosition = it[1] as Long,
                    initialPlayWhenReady = it[2] as Boolean,
                    initialRepeatMode = it[3] as Int,
                    initialMuteState = it[4] as Boolean
                )
            }
        )
    }
}

@Composable
fun ExoPlayer(state: ExoPlayerState = rememberExoPlayerState()) {
    val logger by anonymousLogger("ExoPlayerComposable")
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    val player: Player = remember {
        com.google.android.exoplayer2.ExoPlayer.Builder(context).build()
    }

    LaunchedEffect(Unit) {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                logger.debugIfEnabled { "Playback state changed: $playbackState" }
                state.onPlaybackStateChanged(playbackState)
            }

            override fun onTimelineChanged(timeline: Timeline, reason: Int) {
                logger.debugIfEnabled { "Timeline changed. New duration: ${player.duration}" }
                state.onDurationUpdated(player.duration)
            }

            override fun onPlayerError(error: PlaybackException) {
                logger.errorIfEnabled(error) { "Error playing video" }
            }
        })
    }

    LaunchedEffect(state.playbackState) {
        if (state.playbackState == STATE_READY) {
            logger.debugIfEnabled { "Playback State is READY. Starting polling for position" }
            while (isActive) {
                state.onPositionChanged(player.currentPosition)
                delay(100)
            }
        }
    }

    LaunchedEffect(state.mediaItem) {
        val mediaItem = state.mediaItem
        if (mediaItem != null) {
            player.setMediaItem(mediaItem)
            player.prepare()
            player.seekTo(state.position)
        }
    }

    LaunchedEffect(state.repeatMode) {
        player.repeatMode = state.repeatMode
    }

    LaunchedEffect(state.playWhenReady) {
        player.playWhenReady = state.playWhenReady
    }

    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }


    AndroidView(factory = {
        StyledPlayerView(context)
    }) { playerView ->
        playerView.useController = false
        playerView.player = player
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                playerView.onPause()
            }

            override fun onStop(owner: LifecycleOwner) {
                playerView.onResume()
            }
        })
    }
}
