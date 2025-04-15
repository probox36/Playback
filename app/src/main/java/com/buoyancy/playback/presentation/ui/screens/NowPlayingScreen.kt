package com.buoyancy.playback.presentation.ui.screens

import com.buoyancy.playback.presentation.ui.components.MusicPlayerCard
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.buoyancy.playback.viewmodel.MusicPlayerViewModel

@Composable
fun NowPlayingScreen(
    viewModel: MusicPlayerViewModel
) {
    val backgroundColor = Color(0xFF702A24) // Может быть динамическим (например, из ViewModel)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.Transparent // Чтобы не перекрывать фон
        ) {
            MusicPlayerCard(viewModel)
        }
    }
}