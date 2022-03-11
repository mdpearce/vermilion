package com.neaniesoft.vermilion.ui.videos.exoplayer

import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
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
import com.neaniesoft.vermilion.ui.R
import com.neaniesoft.vermilion.utils.anonymousLogger
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.ceil
import kotlin.time.DurationUnit
import kotlin.time.toDuration

@Composable
fun rememberExoPlayerState(
    initialMediaUri: Uri? = null,
    initialPosition: Long = 0,
    initialPlayWhenReady: Boolean = true,
    initialRepeatMode: Int = Player.REPEAT_MODE_ALL,
    initialMuteState: Boolean = false
): ExoPlayerState = rememberSaveable(saver = ExoPlayerState.Saver) {
    ExoPlayerState(
        initialMediaUri = initialMediaUri,
        initialPosition = initialPosition,
        initialPlayWhenReady = initialPlayWhenReady,
        initialRepeatMode = initialRepeatMode,
        initialMuteState = initialMuteState
    )
}

@Stable
class ExoPlayerState(
    initialMediaUri: Uri? = null,
    initialPosition: Long = 0,
    initialPlayWhenReady: Boolean = true,
    initialRepeatMode: Int = Player.REPEAT_MODE_OFF,
    initialMuteState: Boolean = false
) {
    var mediaUri: Uri? by mutableStateOf(initialMediaUri)

    var position: Long by mutableStateOf(initialPosition)
        private set

    var playWhenReady: Boolean by mutableStateOf(initialPlayWhenReady)

    var playbackState: Int by mutableStateOf(Player.STATE_IDLE)
        private set

    val isBuffering: Boolean by derivedStateOf { playbackState == Player.STATE_BUFFERING }

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
                    state.mediaUri,
                    state.position,
                    state.playWhenReady,
                    state.repeatMode,
                    state.isMuted
                )
            },
            restore = {
                ExoPlayerState(
                    initialMediaUri = it[0] as Uri?,
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

    LaunchedEffect(state.mediaUri) {
        val mediaItem = state.mediaUri?.let { uri -> MediaItem.fromUri(uri) }
        if (mediaItem != null) {
            player.setMediaItem(mediaItem)
            player.prepare()
            player.seekTo(state.position)
        } else {
            player.clearMediaItems()
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

@Composable
fun ExoPlayerWithControls(exoPlayerState: ExoPlayerState = rememberExoPlayerState()) {
    Box(contentAlignment = Alignment.Center) {
        ExoPlayer(exoPlayerState)
        if (exoPlayerState.isBuffering) {
            CircularProgressIndicator()
        }
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            VideoControlRow(
                secondsRemaining = exoPlayerState.secondsRemaining,
                isPlaying = exoPlayerState.playWhenReady,
                onPlayPauseClick = {
                    exoPlayerState.playWhenReady = !exoPlayerState.playWhenReady
                }
            )
        }
    }
}

@Composable
fun VideoControlRow(secondsRemaining: Long, isPlaying: Boolean, onPlayPauseClick: () -> Unit) {
    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
        PlayPauseButton(isPlaying = isPlaying, onPlayPauseClick)
        VideoCounter(secondsRemaining = secondsRemaining)
    }
}

@Composable
fun VideoCounter(secondsRemaining: Long) {
    val duration = secondsRemaining.toDuration(DurationUnit.SECONDS)
    val timeRemainingString = "%d:%02d".format(
        duration.inWholeMinutes,
        duration.toComponents { _, seconds, _ -> seconds }
    )
    Text(
        text = timeRemainingString,
        modifier = Modifier.padding(8.dp),
        style = MaterialTheme.typography.body1
    )
}

@Composable
fun PlayPauseButton(
    isPlaying: Boolean,
    onClick: () -> Unit
) {
    val iconResource = painterResource(
        id = if (isPlaying) {
            R.drawable.ic_baseline_pause_circle_filled_24
        } else {
            R.drawable.ic_baseline_play_circle_filled_24
        }
    )

    IconButton(onClick = onClick) {
        Icon(painter = iconResource, contentDescription = "Play/Pause")
    }
}
