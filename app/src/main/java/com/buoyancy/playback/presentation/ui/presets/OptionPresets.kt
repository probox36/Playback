package com.buoyancy.playback.presentation.ui.presets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.runtime.mutableStateOf
import com.buoyancy.playback.model.OptionComponentState
import com.buoyancy.playback.viewmodel.MusicPlayerViewModel
import com.buoyancy.playback.utils.ToastUtils.toast

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
            val musicService = viewModel.musicService
            return listOf(
                { toast("Option Поделиться chosen!") },
                { musicService.saveCurrentTrack() },
                { musicService.toggleRepeat() },
                { musicService.toggleShuffle() }
            )
        }
    }
}