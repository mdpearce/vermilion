package com.neaniesoft.vermilion.ui.images

import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitTouchSlopOrCancellation
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.gestures.forEachGesture
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.consumePositionChange
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChange
import androidx.compose.ui.input.pointer.positionChangeConsumed
import androidx.compose.ui.layout.layout
import kotlinx.coroutines.launch

// Based on MIT Licenced code from https://github.com/Tlaster/Zoomable

/**
 * A zoomable layout that can handle zoom in and out with drag support.
 *
 * @param state the state object to be used to observe the [Zoomable] state.
 * @param modifier the modifier to apply to this layout.
 * @param doubleTapScale a function called on double tap gesture, will scale to returned value.
 * @param content a block which describes the content.
 */
@Composable
fun Zoomable(
    state: ZoomableState,
    modifier: Modifier = Modifier,
    enable: Boolean = true,
    doubleTapScale: (() -> Float)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val scope = rememberCoroutineScope()
    BoxWithConstraints(
        modifier = modifier
    ) {
        var childWidth by remember { mutableStateOf(0) }
        var childHeight by remember { mutableStateOf(0) }
        LaunchedEffect(
            childHeight,
            childWidth,
            state.scale
        ) {
            val maxX = (childWidth * state.scale - constraints.maxWidth)
                .coerceAtLeast(0F) / 2F
            val maxY = (childHeight * state.scale - constraints.maxHeight)
                .coerceAtLeast(0F) / 2F
            state.updateBounds(maxX, maxY)
        }
        val transformableState = rememberTransformableState { zoomChange, _, _ ->
            if (enable) {
                scope.launch {
                    state.onZoomChange(zoomChange)
                }
            }
        }
        val doubleTapModifier = if (doubleTapScale != null && enable) {
            Modifier.pointerInput(Unit) {
                detectTapGestures(
                    onDoubleTap = {
                        scope.launch {
                            state.animateScaleTo(doubleTapScale())
                        }
                    }
                )
            }
        } else {
            Modifier
        }
        Box(
            modifier = Modifier
                .pointerInput(Unit) {
                    detectDrag(
                        onDrag = { change, dragAmount ->
                            if (state.zooming && enable) {
                                change.consumePositionChange()
                                scope.launch {
                                    state.drag(dragAmount)
                                    state.addPosition(
                                        change.uptimeMillis,
                                        change.position
                                    )
                                }
                            }
                        },
                        onDragCancel = {
                            if (enable) {
                                state.resetTracking()
                            }
                        },
                        onDragEnd = {
                            if (state.zooming && enable) {
                                scope.launch {
                                    state.dragEnd()
                                }
                            }
                        }
                    )
                }
                .then(doubleTapModifier)
                .transformable(state = transformableState)
                .layout { measurable, constraints ->
                    val placeable =
                        measurable.measure(constraints = constraints)
                    childHeight = placeable.height
                    childWidth = placeable.width
                    layout(
                        width = constraints.maxWidth,
                        height = constraints.maxHeight
                    ) {
                        placeable.placeRelativeWithLayer(
                            (constraints.maxWidth - placeable.width) / 2,
                            (constraints.maxHeight - placeable.height) / 2
                        ) {
                            scaleX = state.scale
                            scaleY = state.scale
                            translationX = state.translateX
                            translationY = state.translateY
                        }
                    }
                }
        ) {
            content.invoke(this)
        }
    }
}

private suspend fun PointerInputScope.detectDrag(
    onDragStart: (Offset) -> Unit = { },
    onDragEnd: () -> Unit = { },
    onDragCancel: () -> Unit = { },
    onDrag: (change: PointerInputChange, dragAmount: Offset) -> Unit
) {
    forEachGesture {
        awaitPointerEventScope {
            val down = awaitFirstDown(requireUnconsumed = false)
            var drag: PointerInputChange?
            do {
                drag = awaitTouchSlopOrCancellation(down.id, onDrag)
            } while (drag != null && !drag.positionChangeConsumed())
            if (drag != null) {
                onDragStart.invoke(drag.position)
                if (
                    !drag(drag.id) {
                        onDrag(it, it.positionChange())
                    }
                ) {
                    onDragCancel()
                } else {
                    onDragEnd()
                }
            }
        }
    }
}
