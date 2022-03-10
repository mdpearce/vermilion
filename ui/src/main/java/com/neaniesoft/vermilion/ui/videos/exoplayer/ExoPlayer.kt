package com.neaniesoft.vermilion.ui.videos.exoplayer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable

@Composable
fun rememberExoPlayerState(): ExoPlayerState = rememberSaveable(saver = ExoPlayerState.Saver) {
    ExoPlayerState()
}

@Stable
class ExoPlayerState() {

    companion object {
        val Saver: Saver<ExoPlayerState, *> = listSaver(
            save = {
                listOf<Any>()
            },
            restore = {
                ExoPlayerState()
            }
        )
    }
}

@Composable
fun ExoPlayer() {
}
