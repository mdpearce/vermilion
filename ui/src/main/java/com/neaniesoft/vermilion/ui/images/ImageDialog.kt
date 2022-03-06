package com.neaniesoft.vermilion.ui.images

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.rememberImagePainter
import com.neaniesoft.vermilion.ui.videos.ZoomableDialog

@ExperimentalMaterialApi
@Composable
fun ImageDialog(imageUri: Uri, onDismiss: () -> Unit) {
    Log.d("ImageDialog", "Loading image uri: $imageUri")
    val painter = rememberImagePainter(imageUri)
    val zoomableState = rememberZoomableState(maxScale = 6f)
    ZoomableDialog(state = zoomableState, onDismiss = onDismiss) {
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painter,
            contentDescription = "Full size image",
            contentScale = ContentScale.FillWidth,
        )
    }
}
