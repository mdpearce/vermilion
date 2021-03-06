package com.neaniesoft.vermilion.ui.dialogs

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.neaniesoft.vermilion.ui.theme.AlmostBlack
import com.neaniesoft.vermilion.utils.anonymousLogger
import kotlin.math.roundToInt

private const val SwipeAwayAnimationDuration = 100

@ExperimentalMaterialApi
@Composable
fun FullscreenDialog(onDismiss: () -> Unit, content: @Composable () -> Unit) {
    val logger by anonymousLogger("FullscreenDialog")

    val animationSpec = tween<Float>(
        durationMillis = SwipeAwayAnimationDuration,
        easing = FastOutLinearInEasing
    )

    val swipeableState = rememberSwipeableState(initialValue = 0, animationSpec = animationSpec)
    val alpha = remember {
        derivedStateOf {
            if (swipeableState.progress.from == 0 && swipeableState.progress.to == 0) {
                1f
            } else {
                1f - swipeableState.progress.fraction
            }
        }
    }

    Surface(
        Modifier
            .fillMaxSize()
            .alpha(alpha = alpha.value),
        color = AlmostBlack
    ) {
        val sizePx = with(LocalDensity.current) {
            LocalConfiguration.current.screenHeightDp.dp.toPx()
        }
        val anchors = mapOf(0f to 0, sizePx to 1, 0 - sizePx to 2)

        val scale = remember {
            derivedStateOf {
                if (swipeableState.progress.from == 0 && swipeableState.progress.to == 0) {
                    1f
                } else {
                    1f - (swipeableState.progress.fraction / 2)
                }
            }
        }

        LaunchedEffect(key1 = swipeableState.currentValue) {
            if (swipeableState.currentValue != 0) {
                logger.debugIfEnabled { "Dismissing" }
                onDismiss()
            }
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
                    .scale(scale.value.coerceAtLeast(0f)),
                // .alpha(alpha.value.coerceAtLeast(0f)),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}
