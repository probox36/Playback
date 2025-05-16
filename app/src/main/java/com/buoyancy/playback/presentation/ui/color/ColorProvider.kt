package com.buoyancy.playback.presentation.ui.color

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.AndroidUiDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

object ColorProvider {

    private const val DEFAULT_PRIMARY = 0xFF702A24
    private const val DEFAULT_DARK = 0xFF4C1D19
    private const val DEFAULT_LIGHT = 0xFFF0E1DE

    private val factors = Triple(0.2f, 0.15f, 0.9f)
    private val coroutineScope = CoroutineScope(SupervisorJob() + AndroidUiDispatcher.Main)

    val primaryColor = mutableStateOf(Color(DEFAULT_PRIMARY))
    val darkColor = mutableStateOf(Color(DEFAULT_DARK))
    val lightColor = mutableStateOf(Color(DEFAULT_LIGHT))

    fun updateColors(newColor: Color) {
        coroutineScope.launch {
            launch { animateColor( from = primaryColor, to = newColor.lightness(factors.first) ) }
            launch { animateColor( from = darkColor, to = newColor.lightness(factors.second) ) }
            launch { animateColor( from = lightColor, to = newColor.lightness(factors.third) ) }
        }
    }

    private suspend fun animateColor(
        from: MutableState<Color>,
        to: Color
    ) {
        withContext(Dispatchers.Main) {
            Animatable(from.value).animateTo(
                targetValue = to,
                animationSpec = tween(durationMillis = 400)
            ) {
                from.value = value
            }
        }
    }

    private fun Color.toHsl(): Triple<Float, Float, Float> {
        val hsl = FloatArray(3)
        android.graphics.Color.colorToHSV(toArgb(), hsl)
        return Triple(hsl[0], hsl[1], hsl[2])
    }

    private fun Color.lightness(amount: Float): Color {
        val (baseHue, baseSat, _) = this.toHsl()
        return Color.hsl(baseHue, baseSat, amount)
    }

    private fun Color.saturate(amount: Float): Color {
        val (baseHue, _, baseLight) = this.toHsl()
        return Color.hsl(baseHue, amount, baseLight)
    }
}