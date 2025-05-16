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
import com.spotify.protocol.client.CallResult
import com.spotify.protocol.types.Empty
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Repeat
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
import com.buoyancy.playback.utils.StringUtils.Companion.trimUri

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
    private var queueUpdated = false

    var currentTrackUri by mutableStateOf("")
        private set
    var currentTrackIsSaved by mutableStateOf(false)
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
            if (!pressedPrev && !queueUpdated && currentTrackUri != "") {
                if (isRepeatOn()) toggleRepeat()
                appendQueue()
            }
            else { pressedPrev = false; queueUpdated = false }
            currentTrackUri = state.track.uri
            coroutineScope.launch { currentTrackIsSaved = isInFavourites().await() }
        }
    }

    fun getCurrentTrackIndex(): Int {
        return queue.value.indexOfFirst { it?.uri == currentTrackUri }
    }
    fun getTrack(index: Int): Track? {
        return if (queue.value.isNotEmpty() && index != -1) {
            queue.value[index]
        } else null
    }
    fun getCurrentTrack(): Track? {
        return getTrack(getCurrentTrackIndex())
    }
    fun getNextTrack(): Track? {
        val index = getCurrentTrackIndex()
        return if (index != -1) getTrack(index + 1)
        else null
    }
    fun getPrevTrack(): Track? {
        val index = getCurrentTrackIndex()
        return if (index != -1) getTrack(index - 1)
        else null
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
        queueUpdated = true
    }

    // Single get request is not guaranteed to give you a fresh queue. Web API's kinda slow
    fun getQueue(): Deferred<List<Track?>> = coroutineScope.async {
        repeat(6) { attempt ->
            logDebug("${attempt+1} attempt to get queue...")
            val newQueue = webApi?.getQueue()?.filterNotNull() ?: listOf() // will sometimes throw SpotifyWebApiExc
            if (newQueue.isNotEmpty() && newQueue != queue.value) {
                return@async newQueue
            }
            delay(500 * (attempt + 1L))
        }
        throw TimeoutException("Failed to get new queue after 6 attempts")
    }

    private fun appendQueue() {
        coroutineScope.launch {
            logDebug("appendQueue called")
            val newQueue: List<Track?>
            try {
                newQueue = getQueue().await()
            } catch (e: Exception) {
                logDebug(e.message)
                queue.value = listOf()
                return@launch
            }

            if (currentTrackUri != "" && newQueue.isNotEmpty()) {

                val lastIndexInQueue = queue.value.indexOfLast { it?.uri == currentTrackUri }
                val firstIndexInNewQueue = newQueue.indexOfFirst { it?.uri == currentTrackUri }

                when {
                    lastIndexInQueue != -1 && firstIndexInNewQueue != -1 -> {
                        val updatedQueue = queue.value.toMutableList().apply {
                            subList(lastIndexInQueue + 1, size).clear()
                            addAll(newQueue.subList(firstIndexInNewQueue + 1, newQueue.size))
                        }
                        queue.value = updatedQueue
                        return@launch
                    }
                    else -> { updateQueue() }
                }
            } else {
                logDebug("Something went wrong with appending the queue")
            }
        }
    }

    private fun updatePlaylists() {
        coroutineScope.launch {
            playlists.value = webApi?.getUserPlaylists() ?: listOf()
        }
    }

    fun saveCurrentTrack() {
        coroutineScope.launch {
            webApi?.saveTrack(trimUri(currentTrackUri))
            currentTrackIsSaved = true
        }
    }

    fun previous() : CallResult<Empty>? {
        pressedPrev = true; return playbackController.executeIfReady{ previous() }
    }
    fun next() : CallResult<Empty>? {
        return playbackController.executeIfReady{ next() }
    }
    fun pause() : CallResult<Empty>? {
        return playbackController.executeIfReady { pause() }
    }
    fun resume() : CallResult<Empty>? {
        return playbackController.executeIfReady { resume() }
    }
    fun seekTo(position: Long) : CallResult<Empty>? {
        return playbackController.executeIfReady { seekTo(position) }
    }
    fun playPause() : CallResult<Empty>? {
        return playbackController.executeIfReady { playPause() }
    }
    fun toggleRepeat() : CallResult<Empty>? {
        return playbackController.executeIfReady { toggleRepeat() }
    }
    fun toggleShuffle() : CallResult<Empty>? {
        logDebug("Toggle shuffle called")
        val result = playbackController.executeIfReady { toggleShuffle() }
            ?.setResultCallback { updateQueue() }
        return result
    }

    fun play(uri: String) : CallResult<Empty>? { return playbackController?.play(uri) }

    fun isPaused(): Boolean? { return playbackController?.state?.isPaused }
    fun isShuffleOn(): Boolean? { return playbackController?.state?.playbackOptions?.isShuffling }
    fun isRepeatOn(): Boolean { return playbackController?.state?.playbackOptions?.repeatMode == Repeat.ONE }
    fun isInFavourites(): Deferred<Boolean> = coroutineScope.async {
        logDebug("Current track uri = ${trimUri(currentTrackUri)}")
        webApi?.isTrackInSaved(trimUri(currentTrackUri)) ?: false
    }
    fun trackDuration(): Long? { return playbackController?.state?.track?.duration }
    fun subscribeToPlayerState(listener: (PlayerState) -> Unit) {
        this.playerVmSubscription = listener
    }

    fun disconnect() {
        if (playbackControllerReady) playbackController?.disconnect()
        tokenManager.stopTokenRefreshing()
    }

    private inline fun SpotifyPlaybackController?.executeIfReady(
        action: SpotifyPlaybackController.() -> CallResult<Empty>
    ) : CallResult<Empty>? {
        if (playbackControllerReady) return this?.action()
        else {
            toast(noConnectionMsg)
            return null
        }
    }

    private fun logDebug(message: String?) { message?.let { Log.d(tag, it) } }
    private fun logError(message: String?) { message?.let { Log.e(tag, it) } }
}