package com.buoyancy.playback.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buoyancy.playback.model.GestureHandler
import com.buoyancy.playback.utils.StringUtils.Companion.formatDuration
import com.spotify.protocol.types.PlayerState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class PlayerEvent {
    data object VerticalDragEnded : PlayerEvent()
}

@HiltViewModel
class MusicPlayerViewModel @Inject constructor(
    val musicService: MusicService
) : GestureHandler, ViewModel() {

    init { musicService.subscribeToPlayerState { processPlayerState(it) } }

    // UI State
    var timePassed = mutableLongStateOf(0L)
    var timePassedStr = mutableStateOf("00:00")
    var playbackPosition = mutableDoubleStateOf(0.0)
    var yDrag = mutableFloatStateOf(0F)
    var seeking = mutableStateOf(false)
    var currentTrackUri by mutableStateOf("")

    // Private properties
    private var wasPaused = false
    private var savedPlaybackPosition = 0.0
    private var savedTrackDuration = 0L

    // Consts
    private val tag = "MusicPlayerVM"
    private val dragSensitivity = 600f

    // Events
    private val _playerEvents = MutableSharedFlow<PlayerEvent>()
    val playerEvents: SharedFlow<PlayerEvent> = _playerEvents

    // Main methods
    private fun processPlayerState(state: PlayerState) {
        if (seeking.value) return
        with(state.track) {
            timePassed.longValue = state.playbackPosition
            playbackPosition.doubleValue = state.playbackPosition.toDouble() / duration.toDouble()
            timePassedStr.value = formatDuration(state.playbackPosition)
            if (state.track.uri != currentTrackUri) {
                currentTrackUri = state.track.uri
            }
        }
    }

    // Playback actions
    fun onPrev() = preservePlaybackState {
        if (timePassed.longValue < 3000L)
            musicService.previous()
        else
            musicService.previous()
            musicService.previous()
    }
    fun onNext() = preservePlaybackState {
        musicService.next()
    }
    fun onPlayPause() = musicService.playPause()

    // Gesture handlers
    override fun onTap() { logDebug("Single tap detected!"); onPlayPause()}
    override fun onDoubleTap() { logDebug("Double tap detected!") }

    override fun onHorizontalDragStart() {
        seeking.value = true
        wasPaused = musicService.isPaused() ?: true
        savedPlaybackPosition = playbackPosition.doubleValue
        savedTrackDuration = musicService.trackDuration() ?: 0L
        musicService.pause()
    }

    override fun onHorizontalDrag(dX: Float) {
        playbackPosition.doubleValue = (savedPlaybackPosition + dX / dragSensitivity).coerceIn(0.0, 1.0)
        timePassedStr.value = formatDuration(absolutePlaybackPosition())
    }

    override fun onHorizontalDragEnd() {
        musicService.seekTo(absolutePlaybackPosition())
        seeking.value = false
        if (!wasPaused) musicService.resume()
    }

    override fun onVerticalDragStart() { logDebug("Vertical drag started") }
    override fun onVerticalDrag(dY: Float) {
        yDrag.floatValue = dY
    }
    override fun onVerticalDragEnd() {
        logDebug("Vertical drag ended")
        yDrag.floatValue = 0F
        viewModelScope.launch {
            _playerEvents.emit(PlayerEvent.VerticalDragEnded)
        }
    }

    // Helper methods
    private fun absolutePlaybackPosition() = (savedTrackDuration * playbackPosition.doubleValue).toLong()

    private fun preservePlaybackState(action: () -> Unit) {
        wasPaused = musicService.isPaused() ?: true
        action()
        if (wasPaused) musicService.pause()
    }

    private fun logDebug(message: String?) { message?.let { Log.d(tag, it) } }
}