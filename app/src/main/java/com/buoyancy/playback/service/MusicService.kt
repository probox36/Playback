package com.buoyancy.playback.service

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.buoyancy.playback.model.TokenSubscription
import com.buoyancy.playback.model.music.Playlist
import com.buoyancy.playback.model.music.Track
import com.buoyancy.playback.service.networking.api.impl.SpotifyPlaybackController
import com.buoyancy.playback.service.networking.api.impl.SpotifyWebApi
import com.buoyancy.playback.service.networking.auth.AppRemoteManager
import com.buoyancy.playback.service.networking.auth.TokenManager
import com.buoyancy.playback.utils.ToastUtils.toast
import com.spotify.android.appremote.api.Connector.ConnectionListener
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.TimeoutException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
open class MusicService @Inject constructor(
    private val appRemoteManager: AppRemoteManager,
    private val tokenManager: TokenManager
) {

    private val noConnectionMsg = "Playback controller not initialized. Re-trying connection..."
    private val tag = "BasePlayerVM"
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var playerVmSubscription: (PlayerState) -> Unit = {}
    private var pressedPrev = false

    var currentTrackUri by mutableStateOf("")
        private set
    var queue: MutableState<List<Track?>> = mutableStateOf(listOf())
        private set
    var playlists: MutableState<List<Playlist>> = mutableStateOf(listOf())
        private set

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
                    subscribeToPlayerState{ playerVmSubscription(it) }
                    subscribeToPlayerState { processPlayerState(it) }
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
                updatePlaylists()
                updateQueue()
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

    private fun processPlayerState(state: PlayerState) {
        if (state.track.uri != currentTrackUri) {
            if (!pressedPrev && currentTrackUri != "") appendQueue() else pressedPrev = false
            currentTrackUri = state.track.uri
        }
    }

    // Single get request is not guaranteed to give you a fresh queue. Web API's kinda slow
    fun getQueue(): Deferred<List<Track?>> = coroutineScope.async {
        repeat(6) { attempt ->
            logDebug("${attempt+1} attempt to get queue...")
            val newQueue = webApi?.getQueue() ?: listOf() // will sometimes throw SpotifyWebApiExc
            if (newQueue.isNotEmpty() && newQueue != queue.value) {
                return@async newQueue
            }
            delay(500 * (attempt + 1L))
        }
        throw TimeoutException("Failed to get new queue after 5 attempts")
    }

    private fun appendQueue() {
        coroutineScope.launch {
            logDebug("appendQueue called!")
            var newQueue: List<Track?>
            try {
                newQueue = getQueue().await()
            } catch (e: Exception) {
                logDebug(e.message)
                queue.value = listOf()
                return@launch
            }

            if (currentTrackUri != "" && newQueue.isNotEmpty()) {
                val currentIndex = queue.value.indexOfFirst { it?.uri == currentTrackUri }
                if (currentIndex != -1) {
                    val updatedQueue = queue.value.toMutableList().apply {
                        subList(currentIndex, size).clear()
                        addAll(newQueue.filterNotNull())
                    }
                    queue.value = updatedQueue
                    return@launch
                } else updateQueue()
            } else {
                logDebug("Something went wrong with appending the queue")
            }
        }
    }

    fun updateQueue() {
        logDebug("updateQueue called")
        coroutineScope.launch {
            try {
                queue.value = getQueue().await()
            } catch (e: Exception) {
                queue.value = listOf()
                logDebug(e.message)
            }
        }
    }

    private fun updatePlaylists() {
        coroutineScope.launch {
            playlists.value = webApi?.getUserPlaylists() ?: listOf()
        }
    }

    fun previous() { playbackController.executeIfReady{ previous() }; pressedPrev = true }
    fun next() { playbackController.executeIfReady{ next() } }
    fun pause() { playbackController.executeIfReady { pause() } }
    fun resume() { playbackController.executeIfReady { resume() } }
    fun seekTo(position: Long) { playbackController.executeIfReady { seekTo(position) } }
    fun playPause() { playbackController.executeIfReady { playPause() } }
    fun play(uri: String) { playbackController.executeIfReady { play(uri) } }

    fun isPaused(): Boolean? { return playbackController?.state?.isPaused }
    fun trackDuration(): Long? { return playbackController?.state?.track?.duration }
    fun subscribeToPlayerState(listener: (PlayerState) -> Unit) {
        this.playerVmSubscription = listener
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