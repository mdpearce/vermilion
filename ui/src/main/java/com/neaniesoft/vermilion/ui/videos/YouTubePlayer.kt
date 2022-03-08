package com.neaniesoft.vermilion.ui.videos

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun rememberYouTubePlayerState(
    initialAutoPlay: Boolean = true,
    initialPosition: Float = 0f
): YouTubePlayerState = rememberSaveable(
    saver = YouTubePlayerState.Saver
) {
    YouTubePlayerState(
        initialAutoPlay, initialPosition
    )
}

@Stable
class YouTubePlayerState(
    initialAutoPlay: Boolean = true,
    initialPosition: Float = 0f
) {
    var autoPlay by mutableStateOf(initialAutoPlay)
        private set
    var position by mutableStateOf(initialPosition)
        private set

    internal fun onUpdatePositionFromPlayer(newPos: Float) {
        position = newPos
    }

    internal fun onUpdateAutoPlayStateFromPlayer(newValue: Boolean) {
        autoPlay = newValue
    }

    companion object {
        val Saver: Saver<YouTubePlayerState, *> = listSaver(
            save = {
                listOf(
                    it.autoPlay,
                    it.position
                )
            },
            restore = {
                YouTubePlayerState(it[0] as Boolean, it[1] as Float)
            }
        )
    }
}

@Composable
fun YouTubePlayer(playerState: YouTubePlayerState, videoId: String, modifier: Modifier = Modifier) {
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    AndroidView(factory = {
        YouTubePlayerView(it).apply {
            lifecycle.addObserver(this)
            addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                    playerState.onUpdatePositionFromPlayer(second)
                }

                override fun onReady(youTubePlayer: YouTubePlayer) {
                    youTubePlayer.loadVideo(videoId, playerState.position)
                }

                override fun onStateChange(
                    youTubePlayer: YouTubePlayer,
                    state: PlayerConstants.PlayerState
                ) {
                    when (state) {
                        PlayerConstants.PlayerState.PAUSED, PlayerConstants.PlayerState.ENDED -> {
                            playerState.onUpdateAutoPlayStateFromPlayer(false)
                        }
                        PlayerConstants.PlayerState.PLAYING, PlayerConstants.PlayerState.BUFFERING -> {
                            playerState.onUpdateAutoPlayStateFromPlayer(true)
                        }
                        else -> {}
                    }
                }
            })
        }
    }, update = { }, modifier = modifier)
}
