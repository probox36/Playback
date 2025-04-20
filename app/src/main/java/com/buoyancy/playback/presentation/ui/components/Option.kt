package com.buoyancy.playback.presentation.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buoyancy.playback.R

@OptIn(ExperimentalTextApi::class)
@Composable
fun Option(
    text: String,
    icon: ImageVector,
    isActive: MutableState<Boolean>,
    modifier: Modifier = Modifier,
    baseHeight: Dp = 55.dp,
    baseWidth: Dp = 350.dp
) {
    // Цвета
    val contentColor = Color(0xFF4C1D19)
    val backgroundColor = Color(0xFFF0E1DE)

    // Размеры
    val expandedWidthOffset = 40.dp
    val iconSize = baseHeight / 2
    val horizontalPadding = baseHeight / 3
    val shape = RoundedCornerShape(baseHeight / 2)

    // Шрифт
    val manropeFont = FontFamily(
        Font(
            R.font.manrope_variable,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(610)
            )
        )
    )

    // Текст
    val textStyle = TextStyle(
        fontFamily = manropeFont,
        fontSize = 17.sp,
        color = contentColor
    )

    // Анимация
    val animatedWidth by animateDpAsState(
        targetValue = if (isActive.value) baseWidth + expandedWidthOffset else baseWidth,
        animationSpec = tween(durationMillis = 135),
    )

    Box(
        modifier = modifier
            .height(baseHeight)
            .width(animatedWidth)
            .background(
                color = backgroundColor,
                shape = shape
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = horizontalPadding),
        ) {
            // Иконка
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.height(baseHeight)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(iconSize)
                )
            }

            // Текст
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = text,
                    style = textStyle,
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}