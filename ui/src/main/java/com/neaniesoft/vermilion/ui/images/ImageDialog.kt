package com.neaniesoft.vermilion.ui.images

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter

@Composable
fun ImageDialog(imageUri: Uri) {
    Log.d("ImageDialog", "Loading image uri: $imageUri")
    Surface(Modifier.fillMaxSize()) {
        val painter = rememberImagePainter(imageUri)

        ZoomableImage(painter = painter)
    }
}

private const val DEFAULT_MAX_ZOOM_LEVEL = 6f

@Composable
fun ZoomableImage(painter: ImagePainter, maxZoomLevel: Float = DEFAULT_MAX_ZOOM_LEVEL) {
    var scale by remember {
        mutableStateOf(1f)
    }

    var translation by remember {
        mutableStateOf(Offset(0f, 0f))
    }

    Image(
        painter = painter,
        contentDescription = "Full screen image",
        contentScale = ContentScale.FillWidth,
        modifier = Modifier
            .fillMaxSize()
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                translationX = translation.x,
                translationY = translation.y
            )
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = when {
                        // TODO: Fix the jitter at max and min zoom level
                        scale < 1f -> 1f
                        scale > maxZoomLevel -> maxZoomLevel
                        else -> scale * zoom
                    }
                    // TODO: Clamp this to the edges of the screen so we can't pan about arbitrarily
                    translation += pan * scale
                }
            }
    )
}
