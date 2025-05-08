package com.buoyancy.playback.viewmodel

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import com.buoyancy.playback.model.TokenSubscription
import com.buoyancy.playback.model.exceptions.SpotifyApiException
import com.buoyancy.playback.model.music.Track
import com.buoyancy.playback.service.api.impl.SpotifyPlaybackController
import com.buoyancy.playback.service.api.impl.SpotifyWebApi
import com.buoyancy.playback.service.auth.AppRemoteManager
import com.buoyancy.playback.service.auth.TokenManager
import com.buoyancy.playback.utils.ToastUtils.toast
import com.spotify.android.appremote.api.Connector.ConnectionListener
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

open class MusicService @Inject constructor(
    private val appRemoteManager: AppRemoteManager,
    private val tokenManager: TokenManager
) {

    private val noConnectionMsg = "Playback controller not initialized. Re-trying connection..."
    private val tag = "BasePlayerVM"

    private var subscription: (PlayerState) -> Unit = {}
    var queue: MutableState<List<Track?>> = mutableStateOf(listOf())

    // Spotify api wrappers
    var webApi: SpotifyWebApi ? = null
    var playbackController: SpotifyPlaybackController ? = null
    var webApiReady = false
    var playbackControllerReady = false

    init {
        setupAppRemoteSubscription()
        setupTokenSubscription()
    }

    // Spotify app remote subscription. onConnected executes when remote is ready
    private fun setupAppRemoteSubscription() {
        appRemoteManager.subscribeForAppRemote(object : ConnectionListener {
            override fun onConnected(remoteArg: SpotifyAppRemote) {
                playbackController = SpotifyPlaybackController(remoteArg).apply {
                    subscribeToPlayerState{ subscription(it) }
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
                webApi = SpotifyWebApi(token)
                webApiReady = true
                logDebug("Received token and constructed SpotifyWebApi")
                CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {
                    try {
                        queue.value = webApi?.getQueue() ?: listOf()
                    } catch (e: SpotifyApiException) { logError(e.message) }
                }
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


    fun previous() { playbackController.executeIfReady{ previous() } }
    fun next() { playbackController.executeIfReady{ next() } }
    fun pause() { playbackController.executeIfReady { pause() } }
    fun resume() { playbackController.executeIfReady { resume() } }
    fun seekTo(position: Long) { playbackController.executeIfReady { seekTo(position) } }
    fun playPause() { playbackController.executeIfReady { playPause() } }

    fun isPaused(): Boolean? { return playbackController?.state?.isPaused }
    fun trackDuration(): Long? { return playbackController?.state?.track?.duration }
    fun subscribeToPlayerState(listener: (PlayerState) -> Unit) {
        this.subscription = listener
    }

    fun disconnect() {
        playbackController.executeIfReady { playbackController?.disconnect() }
        tokenManager.stopTokenRefreshing()
    }

    private inline fun SpotifyPlaybackController?.executeIfReady(
        action: SpotifyPlaybackController.() -> Unit
    ) {
        if (playbackControllerReady) this?.action() else toast(noConnectionMsg)
    }

    private fun logDebug(message: String?) { message?.let { Log.d(tag, it) } }
    private fun logError(message: String?) { message?.let { Log.e(tag, it) } }
}