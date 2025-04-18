package com.buoyancy.playback.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.buoyancy.playback.model.GestureHandlingViewModel
import com.buoyancy.playback.service.SpotifyPlaybackController
import com.buoyancy.playback.utils.StringUtils.Companion.formatDuration
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
) : GestureHandlingViewModel() {

    // UI State (публичные mutableState с приватным сеттером)
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
    var seeking = mutableStateOf(false)
        private set

    // Приватные свойства
    private var wasPaused = false
    private var savedPlaybackPosition = 0.0
    private var savedTrackDuration = 0L
    private var coverHash = ""

    // Константы
    companion object {
        private const val COVER_BASE_URL = "https://i.scdn.co/image/"
        private const val NO_CONNECTION_MSG = "Playback controller not initialized. Re-trying connection..."
        private const val DRAG_SENSITIVITY = 500f
    }

    // События
    private val _toastEvent = MutableSharedFlow<String>()
    val toastEvent: SharedFlow<String> = _toastEvent.asSharedFlow()

    init {
        playbackController.subscribe(::processPlayerState)
    }

    // Основные методы
    private fun processPlayerState(state: PlayerState) {
        if (seeking.value) return

        with(state.track) {
            trackName.value = name
            artistName.value = artist.name
            playbackPosition.doubleValue = state.playbackPosition.toDouble() / duration.toDouble()
            timePassed.value = formatDuration(state.playbackPosition)
            trackDuration.value = formatDuration(duration)
            updateCoverImage(imageUri.raw)
        }
    }

    private fun updateCoverImage(newUri: String?) {
        newUri?.let { uri ->
            val newHash = uri.substringAfterLast(":").trim('\'')
            if (newHash != coverHash) {
                coverHash = newHash
                coverUri.value = COVER_BASE_URL + coverHash
            }
        }
    }

    private fun executePlaybackAction(action: () -> Unit) {
        try {
            action()
        } catch (e: NoConnectionToSpotifyException) {
            viewModelScope.launch {
                _toastEvent.emit(NO_CONNECTION_MSG)
                playbackController.connect()
            }
        }
    }

    // Управление плеером
    fun onPrevClick() = executePlaybackAction(playbackController::previous)
    fun onNextClick() = executePlaybackAction(playbackController::next)
    fun onPlayClick() = executePlaybackAction(playbackController::playPause)
    fun disconnect() = executePlaybackAction(playbackController::disconnect)

    // Обработчики жестов
    override fun onTap() { log("Single tap detected!") }
    override fun onDoubleTap() { log("Double tap detected!") }

    override fun onHorizontalDragStart() {
        seeking.value = true
        wasPaused = playbackController.state?.isPaused ?: true
        savedPlaybackPosition = playbackPosition.doubleValue
        savedTrackDuration = playbackController.state?.track?.duration ?: 0L
        playbackController.pause()
    }

    override fun onHorizontalDrag(dX: Float) {
        playbackPosition.doubleValue = (savedPlaybackPosition + dX / DRAG_SENSITIVITY).coerceIn(0.0, 1.0)
        timePassed.value = formatDuration(absolutePlaybackPosition())
    }

    override fun onHorizontalDragEnd() {
        executePlaybackAction {
            playbackController.seekTo(absolutePlaybackPosition())
        }
        seeking.value = false
        if (!wasPaused) playbackController.resume()
    }

    override fun onVerticalDrag(dY: Float) { log("Vertical drag: $dY") }
    override fun onVerticalDragStart() { log("Vertical drag started") }
    override fun onVerticalDragEnd() { log("Vertical drag ended") }

    // Вспомогательные методы
    private fun absolutePlaybackPosition() = (savedTrackDuration * playbackPosition.doubleValue).toLong()
    private fun log(message: String) { Log.i("MusicPlayerVM", message) }
}