package com.buoyancy.playback.presentation.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import com.buoyancy.playback.model.GestureEvent
import com.buoyancy.playback.model.OptionComponentState
import com.buoyancy.playback.viewmodel.MusicPlayerViewModel
import kotlin.math.roundToInt
import android.view.HapticFeedbackConstants as haptics

@Composable
fun Options(
    viewModel: MusicPlayerViewModel,
    options: List<OptionComponentState>,
    callbacks: List<() -> Unit>
) {
    // Constants
    val spacerHeight = 8.dp
    val optionHeight = 55.dp
    val step = optionHeight + spacerHeight
    val backgroundColor = Color(0xFF4C1D19)

    // State and derived values
    val rOptions = remember { options }
    val density = LocalDensity.current
    val view = LocalView.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val dY = viewModel.yDrag
    val statusBarHeight = with(density) {
        WindowInsets.statusBars.getTop(density).toDp()
    }

    val boxHeight = remember { mutableStateOf(0.dp) }
    val index = remember { mutableIntStateOf(-1) }

    // Derived states
    val activated by remember { derivedStateOf { dY.floatValue > 0 } }
    index.intValue = if (activated) {
        (dY.floatValue * 0.45f / step.value)
            .roundToInt()
            .coerceAtMost(rOptions.lastIndex)
    } else -1

    // Animations
    val animatedActivationOffset by animateDpAsState(
        targetValue = if (activated) statusBarHeight + optionHeight else 0.dp,
        animationSpec = tween(250),
        label = "activationAnimation"
    )

    val animatedDragOffset by animateDpAsState(
        targetValue = if (activated) (index.intValue * step.value).dp else 0.dp,
        animationSpec = tween(135),
        label = "dragAnimation"
    )

    val backgroundAlpha by animateFloatAsState(
        targetValue = if (activated) 0.8f else 0f,
        animationSpec = tween(350),
        label = "backgroundAnimation"
    )

    // Effects
    LaunchedEffect(index.intValue) {
        if (index.intValue in rOptions.indices) {
            view.performHapticFeedback(haptics.SEGMENT_FREQUENT_TICK)
            rOptions.forEachIndexed { optInd, option ->
                option.isActive.value = (index.intValue == rOptions.lastIndex - optInd)
            }
        }
    }

    LaunchedEffect(viewModel, lifecycleOwner) {
        viewModel.gestureEvents
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { event ->
                if (event is GestureEvent.VerticalDragEnded && index.intValue >= 0) {
                    callbacks[rOptions.lastIndex - index.intValue]()
                }
            }
    }

    // Layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor.copy(alpha = backgroundAlpha))
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .onSizeChanged { size ->
                    boxHeight.value = with(density) { size.height.toDp() }
                }
                .offset(
                    y = with(boxHeight) {
                        value * -1 + animatedDragOffset.coerceAtMost(value - step) + animatedActivationOffset
                    }
                )
        ) {
            rOptions.forEachIndexed { optNum, option ->
                Option(option.text, option.icon, option.isActive)
                if (optNum != rOptions.lastIndex) Spacer(Modifier.height(spacerHeight))
            }
        }
    }
}