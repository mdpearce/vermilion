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
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

@Composable
fun YouTubePlayer(videoId: String) {
    val context = LocalContext.current
    val lifecycle = LocalLifecycleOwner.current.lifecycle

    // // TODO This should be hoisted out to a state object for observation
    var autoPlay by rememberSaveable { mutableStateOf(true) }
    var position by rememberSaveable { mutableStateOf(0f) }

    val playerView = remember {
        YouTubePlayerView(context).apply {
            // Ensure the player doesn't continue in background when not permitted
            lifecycle.addObserver(this)

            addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(youTubePlayer: YouTubePlayer) {
                    youTubePlayer.loadVideo(videoId, position)
                    if (!autoPlay) {
                        youTubePlayer.pause()
                    }
                }

                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                    position = second
                }

                override fun onStateChange(
                    youTubePlayer: YouTubePlayer,
                    state: PlayerConstants.PlayerState
                ) {
                    when (state) {
                        PlayerConstants.PlayerState.PAUSED, PlayerConstants.PlayerState.ENDED -> autoPlay =
                            false
                        PlayerConstants.PlayerState.PLAYING, PlayerConstants.PlayerState.BUFFERING -> autoPlay =
                            true
                        else -> {}
                    }
                }
            })
        }
    }

    DisposableEffect(key1 = Unit) {
        onDispose {
            playerView.release()
        }
    }

    AndroidView(factory = {
        playerView
    }, update = {})
}
