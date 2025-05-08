package com.buoyancy.playback.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AlbumsViewModel @Inject constructor(
    val musicService: MusicService
) : ViewModel() {
    init {
        Log.d("AlbumsViewModel", "AlbumsViewModel initiated!")
    }
}