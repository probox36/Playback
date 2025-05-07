package com.buoyancy.playback.presentation.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableDoubleState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.times
import com.buoyancy.playback.R

@OptIn(ExperimentalTextApi::class)
@Composable
fun ProgressBar(
    playbackPosition: MutableDoubleState,
    timePassed: MutableState<String>,
    trackDuration: String,
    seeking: MutableState<Boolean>,
    columnWidth: Dp = 300.dp,
    axisHeight: Dp = 60.dp
) {
    // Константы
    val fontSize = 14.sp
    val axisPadding = 4.dp
    val middleLayerPadding = 3.dp
    val totalPadding = axisPadding + middleLayerPadding
    val cornerRadius = axisHeight / 2

    // Цвета
    val axisColor = Color(0xFF702A24)
    val middleLayerColor = Color(0xFF4C1D19)
    val trackColor = Color(0xFFF0E1DE)

    // Размеры
    val availableWidth = columnWidth - 2 * totalPadding
    val availableHeight = axisHeight - 2 * totalPadding

    // Шрифт
    val unboundedFont = FontFamily(
        Font(
            R.font.unbounded_variable,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(FontWeight.W600.weight)
            )
        )
    )

    // Анимации
    val animatedWidth = remember { Animatable(availableHeight.value) }
    LaunchedEffect(playbackPosition.doubleValue) {
        val targetWidth = availableHeight + (availableWidth - availableHeight) * playbackPosition.doubleValue.toFloat()
        if (seeking.value) {
            animatedWidth.snapTo(targetWidth.value)
        } else {
            animatedWidth.animateTo(
                targetValue = targetWidth.value,
                animationSpec = tween(250)
            )
        }
    }

    val animatedBorderRadius by animateIntAsState(
        targetValue = if (seeking.value) 10 else 50,
        animationSpec = tween(durationMillis = 135),
    )

    Column(modifier = Modifier.width(columnWidth)) {
        // Основной контейнер прогресс-бара
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(axisHeight)
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
                // Фоновые метки времени
                TimeLabels(
                    timePassed = timePassed.value,
                    trackDuration = trackDuration,
                    fontFamily = unboundedFont,
                    fontSize = fontSize,
                    textColor = trackColor,
                    modifier = Modifier
                        .width(availableWidth)
                        .fillMaxHeight()
                        .padding(horizontal = cornerRadius * 2 / 3)
                        .align(Alignment.Center)
                )

                // Анимированный прогресс
                Box(
                    modifier = Modifier
                        .padding(middleLayerPadding)
                        .size(height = availableHeight, width = animatedWidth.value.dp)
                        .clip(RoundedCornerShape(
                            50,
                            animatedBorderRadius,
                            animatedBorderRadius,
                            50))
                        .background(trackColor)
                        .wrapContentSize(Alignment.CenterStart, true)
                ) {
                    // Метки времени поверх прогресса
                    TimeLabels(
                        timePassed = timePassed.value,
                        trackDuration = trackDuration,
                        fontFamily = unboundedFont,
                        fontSize = fontSize,
                        textColor = middleLayerColor,
                        modifier = Modifier
                            .requiredSize(availableWidth, availableHeight)
                            .padding(horizontal = cornerRadius * 2 / 3)
                    )
                }
            }
        }
    }
}

// Вынесенный компонент меток времени
@Composable
private fun TimeLabels(
    timePassed: String,
    trackDuration: String,
    fontFamily: FontFamily,
    fontSize: TextUnit,
    textColor: Color,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        // Прошедшее время (слева)
        Text(
            text = timePassed,
            fontFamily = fontFamily,
            fontSize = fontSize,
            color = textColor,
            modifier = Modifier.align(Alignment.CenterStart)
        )

        // Длительность трека (справа)
        Text(
            text = trackDuration,
            fontFamily = fontFamily,
            fontSize = fontSize,
            color = textColor,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProgressBarPreview() {
    ProgressBar(
        playbackPosition = remember { mutableDoubleStateOf(0.75) },
        timePassed = remember { mutableStateOf("00:45") },
        trackDuration = "01:00",
        seeking = remember { mutableStateOf(false) }
    )
}