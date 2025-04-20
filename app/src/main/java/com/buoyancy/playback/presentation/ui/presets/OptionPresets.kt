package com.buoyancy.playback.presentation.ui.presets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.runtime.mutableStateOf
import com.buoyancy.playback.model.OptionComponentState
import com.buoyancy.playback.viewmodel.MusicPlayerViewModel

class OptionPresets {
    companion object {

        fun nowPlayingOptions(): List<OptionComponentState> {
            return listOf(
                OptionComponentState("Поделиться", Icons.Default.Share, mutableStateOf(false)),
                OptionComponentState("Добавить в избранное", Icons.Default.Favorite, mutableStateOf(false)),
                OptionComponentState("Повтор", Icons.Default.RepeatOne, mutableStateOf(false)),
                OptionComponentState("Шафл", Icons.Default.Shuffle, mutableStateOf(false))
            )
        }

        fun nowPlayingCallbacks(viewModel: MusicPlayerViewModel): List<() -> Unit> {
            return listOf(
                { viewModel.triggerToast("Option Поделиться chosen!") },
                { viewModel.triggerToast("Option Добавить в избранное chosen!") },
                { viewModel.triggerToast("Option Повтор chosen!") },
                { viewModel.triggerToast("Option Шафл chosen!") }
            )
        }
    }
}