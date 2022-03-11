package com.neaniesoft.vermilion.ui.videos.external

import android.net.Uri
import android.util.Log
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import com.neaniesoft.vermilion.ui.images.rememberZoomableState
import com.neaniesoft.vermilion.ui.videos.ZoomableDialog
import com.neaniesoft.vermilion.ui.videos.exoplayer.ExoPlayerWithControls
import com.neaniesoft.vermilion.ui.videos.exoplayer.rememberExoPlayerState
import com.neaniesoft.vermilion.ui.videos.rememberVideoPlayerState

@ExperimentalMaterialApi
@Composable
fun ExternalVideoDialog(
    unresolvedUri: Uri,
    onDismiss: () -> Unit,
    viewModel: ExternalVideoDialogViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsState()
    val zoomableState = rememberZoomableState(maxScale = 6f)
    val videoPlayerState = rememberVideoPlayerState()
    val exoPlayerState = rememberExoPlayerState()

    LaunchedEffect(key1 = unresolvedUri) {
        viewModel.onResolveExternalUri(unresolvedUri)
    }

    ZoomableDialog(state = zoomableState, onDismiss = onDismiss) {
        when (val currentState = uiState) {
            is ExternalVideoDialogState.Loading -> {} // TODO: Put a progress spinner here
            is ExternalVideoDialogState.ErrorState -> {
                Log.e(
                    "ExternalVideoDialog",
                    "Error: ${currentState.error}"
                ) // TODO Handle this better
            }
            is ExternalVideoDialogState.PlayUriState -> {
                exoPlayerState.mediaUri = currentState.uri
                ExoPlayerWithControls(exoPlayerState)
            }
        }
    }
}
