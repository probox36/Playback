package com.buoyancy.playback.presentation.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalView
import com.buoyancy.playback.model.GestureHandlingViewModel
import kotlin.math.abs
import android.view.HapticFeedbackConstants as Haptics

private sealed class SwipeType {
    data object Horizontal : SwipeType()
    data object Vertical : SwipeType()
}

private const val tan60 = 1.7321f

@Composable
fun GestureInterceptor(
    content: @Composable () -> Unit,
    viewModel: GestureHandlingViewModel
) {
    val view = LocalView.current
    val offsetX = remember { mutableFloatStateOf(0f) }
    val offsetY = remember { mutableFloatStateOf(0f) }

    var swipeType by remember { mutableStateOf<SwipeType?>(null) }

    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { viewModel.onTap() },
                    onDoubleTap = {
                        view.performHapticFeedback(Haptics.LONG_PRESS)
                        viewModel.onDoubleTap()
                    }
                )
            }
            .pointerInput(Unit) {
                detectDragGesturesAfterLongPress (
                    onDragStart = {
                        view.performHapticFeedback(Haptics.LONG_PRESS)
                    },
                    onDrag = { _, dragAmount ->

                        // Определяем тип жеста при первом движении (если еще не определен)
                        val dX = dragAmount.x
                        val dY = dragAmount.y

                        if (swipeType == null) {
                            if (abs(dX) / abs(dY) >= tan60) {
                                swipeType = SwipeType.Horizontal
                                viewModel.onHorizontalDragStart()
                            } else {
                                swipeType = SwipeType.Vertical
                                viewModel.onVerticalDragStart()
                            }
                        }

                        offsetX.floatValue += dX
                        offsetY.floatValue += dY

                        when (swipeType) {
                            SwipeType.Horizontal -> viewModel.onHorizontalDrag(offsetX.floatValue)
                            SwipeType.Vertical -> viewModel.onVerticalDrag(offsetY.floatValue)
                            null -> {}
                        }
                    },
                    onDragEnd = {
                        when (swipeType) {
                            SwipeType.Horizontal -> viewModel.onHorizontalDragEnd()
                            SwipeType.Vertical -> viewModel.onVerticalDragEnd()
                            null -> {}
                        }
                        swipeType = null
                        offsetX.floatValue = 0f
                        offsetY.floatValue = 0f
                    }
                )
            }
    ) {
        content()
    }
}