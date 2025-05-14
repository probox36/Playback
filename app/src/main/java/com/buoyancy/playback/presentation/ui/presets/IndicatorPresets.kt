package com.buoyancy.playback.presentation.ui.presets

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOn
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.ShuffleOn
import com.buoyancy.playback.model.IndicatorComponentState
import com.buoyancy.playback.viewmodel.MusicPlayerViewModel

class IndicatorPresets(val viewModel: MusicPlayerViewModel) {

    val playingState = IndicatorComponentState(!viewModel.isPlaying, Icons.Default.PlayArrow, Icons.Default.Pause)
    val repeatState = IndicatorComponentState(viewModel.repeatOn, Icons.Default.RepeatOn, Icons.Default.Repeat)
    val shuffleState = IndicatorComponentState(viewModel.shuffleOn, Icons.Default.ShuffleOn, Icons.Default.Shuffle)
    val savedState = IndicatorComponentState(viewModel.inSaved, Icons.Default.Favorite, Icons.Default.FavoriteBorder)
}