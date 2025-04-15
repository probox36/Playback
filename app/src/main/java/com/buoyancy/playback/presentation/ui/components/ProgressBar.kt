package com.buoyancy.playback.presentation.ui.components

import android.view.animation.AccelerateDecelerateInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableDoubleState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times

@Composable
fun ProgressBar(
    playbackPosition: MutableDoubleState,
    timePassed: MutableState<String>,
    trackDuration: MutableState<String>,
) {
    val axisColor = Color(0xFF702A24)
    val middleLayerColor = Color(0xFF4C1D19)
    val trackColor = Color(0xFFF0E1DE)

    val axisPadding = 4.dp
    val middleLayerPadding = 3.dp
    val totalPadding = axisPadding + middleLayerPadding

    val axisHeight = 50.dp
    val columnWidth = 300.dp
    val cornerRadius = axisHeight / 2

    val availableWidth = columnWidth - 2 * totalPadding
    val availableHeight = axisHeight - 2 * totalPadding

    val animatedWidth = remember { Animatable(availableHeight.value) }

    LaunchedEffect(playbackPosition.doubleValue) {
        val targetWidth = availableHeight + (availableWidth - availableHeight) * playbackPosition.doubleValue.toFloat()
        animatedWidth.animateTo(
            targetValue = targetWidth.value,
            animationSpec = tween(
                durationMillis = 500,
                easing = { AccelerateDecelerateInterpolator().getInterpolation(it) }
            )
        )
    }

    Column(
        modifier = Modifier
            .width(columnWidth)
    ) {
        Box(
            modifier = Modifier
                .width(columnWidth)
                .size(height = axisHeight, width = columnWidth)
                .clip(RoundedCornerShape(cornerRadius))
                .background(axisColor)
        ) {
            // Промежуточный слой
            Box(
                modifier = Modifier
                    .padding(axisPadding)
                    .matchParentSize()
                    .clip(RoundedCornerShape(cornerRadius - axisPadding))
                    .background(middleLayerColor)
            ) {
                // След (прогресс) с анимированной шириной
                Box(
                    modifier = Modifier
                        .padding(middleLayerPadding)
                        .size(height = availableHeight, width = animatedWidth.value.dp)
                        .clip(RoundedCornerShape(cornerRadius - totalPadding))
                        .background(trackColor)
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        // Ряд с временными метками
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = cornerRadius / 2),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = timePassed.value,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = trackDuration.value,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressBarPreview() {
    ProgressBar(
        playbackPosition = remember { mutableDoubleStateOf(0.75) },
        timePassed = remember { mutableStateOf("00:45") },
        trackDuration = remember { mutableStateOf("01:00") }
    )
}