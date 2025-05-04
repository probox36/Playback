package com.buoyancy.playback.viewmodel

import android.util.Log
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.buoyancy.playback.model.GestureHandlingViewModel
import com.buoyancy.playback.model.TokenSubscription
import com.buoyancy.playback.service.api.impl.SpotifyPlaybackControllerImpl
import com.buoyancy.playback.service.api.impl.SpotifyWebApiImpl
import com.buoyancy.playback.service.auth.AppRemoteManager
import com.buoyancy.playback.service.auth.TokenManager
import com.buoyancy.playback.utils.StringUtils.Companion.formatDuration
import com.buoyancy.playback.utils.ToastUtils.toast
import com.spotify.android.appremote.api.Connector.ConnectionListener
import com.spotify.android.appremote.api.SpotifyAppRemote
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
    private val appRemoteManager: AppRemoteManager,
    private val tokenManager: TokenManager
) : GestureHandlingViewModel() {

    // UI State
    var trackName = mutableStateOf("")
    var artistName = mutableStateOf("")
    var timePassed = mutableStateOf("00:00")
    var trackDuration = mutableStateOf("00:00")
    var coverUri = mutableStateOf("")
    var playbackPosition = mutableDoubleStateOf(0.0)
    var yDrag = mutableFloatStateOf(0F)
    var seeking = mutableStateOf(false)

    // Private properties
    private val tag = "MusicPlayerVM"
    private var wasPaused = false
    private var savedPlaybackPosition = 0.0
    private var savedTrackDuration = 0L
    private var coverHash = ""

    // Spotify api wrappers
    private var webApi: SpotifyWebApiImpl ? = null
    private var playbackController: SpotifyPlaybackControllerImpl ? = null
    private var webApiReady = false
    private var playbackControllerReady = false

    // Consts
    companion object {
        private const val COVER_BASE_URL = "https://i.scdn.co/image/"
        private const val NO_CONNECTION_MSG = "Playback controller not initialized. Re-trying connection..."
        private const val DRAG_SENSITIVITY = 600f
    }

    // Events
    private val _playerEvents = MutableSharedFlow<PlayerEvent>()
    val playerEvents: SharedFlow<PlayerEvent> = _playerEvents

    init {
        setupAppRemoteSubscription()
        setupTokenSubscription()
    }

    // Spotify app remote subscription. onConnected executes when remote is ready
    private fun setupAppRemoteSubscription() {
        appRemoteManager.subscribeForAppRemote(object : ConnectionListener {
            override fun onConnected(remoteArg: SpotifyAppRemote) {
                playbackController = SpotifyPlaybackControllerImpl(remoteArg).apply {
                    subscribeToPlayerState(::processPlayerState)
                }
                playbackControllerReady = true
                logDebug("Received appRemote and constructed playback controller")
            }
            override fun onFailure(t: Throwable?) {}
        })
    }

    // Spotify web api token subscription. onTokenReceived executes when token is fetched
    private fun setupTokenSubscription() {
        tokenManager.subscribeForToken(object : TokenSubscription {
            override fun onTokenReceived(token: String) {
                webApi = SpotifyWebApiImpl(token)
                webApiReady = true
                logDebug("Received token and constructed SpotifyWebApi")
            }
            override fun onFailure(error: Throwable) {
                logError("Cannot construct SpotifyWebApi: failed to get token")
            }
            override fun onTokenTemporarilyInvalid() {
                webApiReady = false
                webApi = null
                logDebug("Token temporarily invalidated")
            }
        })
    }

    // Main methods
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

    private inline fun SpotifyPlaybackControllerImpl?.executeIfReady(
        action: SpotifyPlaybackControllerImpl.() -> Unit
    ) {
        if (playbackControllerReady) this?.action() else toast(NO_CONNECTION_MSG)
    }

    // Button callbacks
    fun onPrevClick() = playbackController.executeIfReady { previous() }
    fun onNextClick() = playbackController.executeIfReady { next() }
    fun onPlayClick() = playbackController.executeIfReady { playPause() }
    fun disconnect() {
        playbackController.executeIfReady { disconnect() }
        tokenManager.stopTokenRefreshing()
    }

    // Gesture handlers
    override fun onTap() { logDebug("Single tap detected!") }
    override fun onDoubleTap() { logDebug("Double tap detected!") }

    override fun onHorizontalDragStart() {
        seeking.value = true
        wasPaused = playbackController?.state?.isPaused ?: true
        savedPlaybackPosition = playbackPosition.doubleValue
        savedTrackDuration = playbackController?.state?.track?.duration ?: 0L
        playbackController?.pause()
    }

    override fun onHorizontalDrag(dX: Float) {
        playbackPosition.doubleValue = (savedPlaybackPosition + dX / DRAG_SENSITIVITY).coerceIn(0.0, 1.0)
        timePassed.value = formatDuration(absolutePlaybackPosition())
    }

    override fun onHorizontalDragEnd() {
        playbackController.executeIfReady { seekTo(absolutePlaybackPosition()) }
        seeking.value = false
        if (!wasPaused) playbackController.executeIfReady { resume() }
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
    private fun logDebug(message: String) { Log.d(tag, message) }
    private fun logError(message: String) { Log.e(tag, message) }
}