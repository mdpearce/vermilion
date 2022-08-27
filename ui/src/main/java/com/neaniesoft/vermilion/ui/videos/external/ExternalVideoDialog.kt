package com.neaniesoft.vermilion.ui.videos.external

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.neaniesoft.vermilion.ui.images.rememberZoomableState
import com.neaniesoft.vermilion.ui.videos.ZoomableDialog
import com.neaniesoft.vermilion.ui.videos.exoplayer.ExoPlayerWithControls
import com.neaniesoft.vermilion.ui.videos.exoplayer.rememberExoPlayerState

@ExperimentalMaterialApi
@Composable
fun ExternalVideoDialog(
    unresolvedUri: Uri,
    onDismiss: () -> Unit,
    viewModel: ExternalVideoDialogViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val zoomableState = rememberZoomableState(maxScale = 6f)
    val exoPlayerState = rememberExoPlayerState()

    LaunchedEffect(key1 = unresolvedUri) {
        viewModel.onResolveExternalUri(unresolvedUri)
    }

    ZoomableDialog(state = zoomableState, onDismiss = onDismiss) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when (val currentState = uiState) {
                is ExternalVideoDialogState.Loading -> {
                    CircularProgressIndicator()
                }
                is ExternalVideoDialogState.ErrorState -> {
                    Text(
                        text = currentState.error.toString(),
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.error
                    )
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
}
