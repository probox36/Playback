package com.buoyancy.playback.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buoyancy.playback.service.SpotifyPlaybackController
import com.buoyancy.playback.viewmodel.exceptions.NoConnectionToSpotifyException
import com.spotify.protocol.types.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MusicPlayerViewModel @Inject constructor(
    private val playbackController: SpotifyPlaybackController
): ViewModel() {

    var trackName = mutableStateOf("")
        private set
    var artistName = mutableStateOf("")
        private set
    var timePassed = mutableStateOf("00:00")
        private set
    var trackDuration = mutableStateOf("00:00")
        private set
    var coverUri = mutableStateOf("")
        private set
    var playbackPosition = mutableDoubleStateOf(0.0)
        private set

    private val noConnectionMessage = "Playback controller not initialized. Re-trying connection..."
    private val coverBaseUrl = "https://i.scdn.co/image/"
    private var coverHash = ""

    private val _toastEvent = MutableSharedFlow<String>() // или MutableLiveData
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    init {
        playbackController.subscribe { processPlayerState(it) }
    }

    private fun triggerToast(message: String) {
        viewModelScope.launch {
            _toastEvent.emit(message)
        }
    }

    private fun processPlayerState(state: PlayerState) {

        val track = state.track
        trackName.value = track.name
        artistName.value = track.artist.name
        val msPassed = state.playbackPosition
        val msDuration = track.duration
        playbackPosition.doubleValue = msPassed.toDouble() / msDuration.toDouble()
        timePassed.value = formatDuration(msPassed)
        trackDuration.value = formatDuration(msDuration)

        val newUri = track.imageUri.raw
        if (newUri != null) {
            val newHash = newUri.substringAfterLast(":").trim('\'')
            if (newHash != coverHash) {
                coverHash = newHash
                coverUri.value = coverBaseUrl + coverHash
                Log.i("MainActivity", "New cover uri: ${coverUri.value}")
            }
        }
    }

    private fun executePlaybackAction(action: () -> Unit) {
        try {
            action()
        } catch (e: NoConnectionToSpotifyException) {
            triggerToast(noConnectionMessage)
            playbackController.connect()
        }
    }

    fun onPrevClick() {
        executePlaybackAction {
            playbackController.previous()
        }
    }
    fun onNextClick() {
        executePlaybackAction {
            playbackController.next()
        }
    }
    fun onPlayClick() {
        executePlaybackAction { playbackController.playPause() }
    }

    fun disconnect() {
        playbackController.disconnect()
    }

    private fun formatDuration(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return String.format("%02d:%02d", minutes, seconds)
    }

}