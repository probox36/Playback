package com.buoyancy.playback.model

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.Stable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@Stable
data class OptionComponentState(
    val text: String,
    val icon: ImageVector,
    val isActive: MutableState<Boolean>,
    val modifier: Modifier = Modifier
)