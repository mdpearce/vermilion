package com.neaniesoft.vermilion.ui.images

import android.net.Uri
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.Surface
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import coil.compose.rememberImagePainter
import kotlin.math.roundToInt

@ExperimentalMaterialApi
@Composable
fun ImageDialog(imageUri: Uri) {
    Log.d("ImageDialog", "Loading image uri: $imageUri")
    Surface(Modifier.fillMaxSize()) {
        val painter = rememberImagePainter(imageUri)

        val zoomableState = rememberZoomableState(maxScale = 6f)

        val swipeableState = rememberSwipeableState(initialValue = 0)
        val sizePx = with(LocalDensity.current) {
            LocalConfiguration.current.screenHeightDp.dp.toPx()
        }
        val anchors = mapOf(0f to 0, sizePx to 1, 0 - sizePx to 2)
        Log.d("ImageDialog", "swipeableState: ${swipeableState.progress}")

        Box(
            Modifier
                .fillMaxSize()
                .swipeable(state = swipeableState,
                    anchors = anchors,
                    orientation = Orientation.Vertical,
                    thresholds = { _, _ -> FractionalThreshold(0.3f) })
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) },
                contentAlignment = Alignment.Center
            )
            {
                Zoomable(state = zoomableState) {
                    Image(
                        painter = painter,
                        contentDescription = "Full size image",
                        contentScale = ContentScale.FillWidth,
                    )
                }
            }
        }
    }
}
