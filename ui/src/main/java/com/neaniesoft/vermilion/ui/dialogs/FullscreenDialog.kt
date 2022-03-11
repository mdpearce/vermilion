package com.neaniesoft.vermilion.ui.videos

import android.util.Log
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.neaniesoft.vermilion.ui.theme.AlmostBlack
import kotlin.math.roundToInt

@ExperimentalMaterialApi
@Composable
fun FullscreenDialog(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    Surface(Modifier.fillMaxSize(), color = AlmostBlack) {
        val swipeableState = rememberSwipeableState(initialValue = 0)
        val sizePx = with(LocalDensity.current) {
            LocalConfiguration.current.screenHeightDp.dp.toPx() / 2f
        }
        val anchors = mapOf(0f to 0, sizePx to 1, 0 - sizePx to 2)

        val alpha = remember {
            derivedStateOf {
                if (swipeableState.progress.from == 0 && swipeableState.progress.to == 0) {
                    1f
                } else {
                    1f - swipeableState.progress.fraction
                }
            }
        }

        val flingStarted by remember {
            derivedStateOf { swipeableState.isAnimationRunning && swipeableState.direction < 0 }
        }

        if (flingStarted) {
            Log.d("VideoDialog", "Dismissing")
            onDismiss()
        }

        Box(
            Modifier
                .fillMaxSize()
                .swipeable(
                    state = swipeableState,
                    anchors = anchors,
                    orientation = Orientation.Vertical,
                    thresholds = { _, _ -> FractionalThreshold(0.3f) }
                )
        ) {
            Box(
                Modifier
                    .fillMaxSize()
                    .offset { IntOffset(0, swipeableState.offset.value.roundToInt()) }
                    .alpha(alpha.value),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}
