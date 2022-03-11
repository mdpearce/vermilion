package com.neaniesoft.vermilion.ui.videos

import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import com.neaniesoft.vermilion.ui.dialogs.FullscreenDialog
import com.neaniesoft.vermilion.ui.images.Zoomable
import com.neaniesoft.vermilion.ui.images.ZoomableState

@ExperimentalMaterialApi
@Composable
fun ZoomableDialog(state: ZoomableState, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    FullscreenDialog(onDismiss) {
        Zoomable(state = state) {
            content()
        }
    }
}
