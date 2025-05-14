package com.buoyancy.playback.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Stable
data class IndicatorComponentState(
    val activated: Boolean,
    val activatedIcon: ImageVector,
    val nonActivatedIcon: ImageVector,
    val modifier: Modifier = Modifier
)