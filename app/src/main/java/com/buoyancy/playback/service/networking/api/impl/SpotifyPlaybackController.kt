package com.buoyancy.playback.service.networking.api.impl

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.buoyancy.playback.service.networking.api.PlaybackControlProvider
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.CallResult
import com.spotify.protocol.types.Empty
import com.spotify.protocol.types.PlayerState
import com.spotify.protocol.types.Repeat


class SpotifyPlaybackController(
    val remote: SpotifyAppRemote
) : PlaybackControlProvider {

    private var subscribers: MutableList<(PlayerState) -> Unit> = mutableListOf()
    private var handler = Handler(Looper.getMainLooper())
    private val pollingFrequency: Long = 250

    var player = remote.playerApi
        private set
    var subscription = player.subscribeToPlayerState()
        private set
    var state: PlayerState ? = null
        private set

    private var monitorPlaybackStateTask = object : Runnable {
        override fun run() {
            subscription.cancel()
            subscription = remote.playerApi.subscribeToPlayerState()
            subscription.setEventCallback { stateArg ->
                notifySubscribers(stateArg)
                state = stateArg
            }
            handler.postDelayed(this, pollingFrequency)
        }
    }

    init {
        monitorPlayback()
    }

    fun subscribeToPlayerState(listener: (PlayerState) -> Unit) {
        subscribers.add(listener)
    }

    private fun notifySubscribers(state: PlayerState) {
        for (sub in subscribers) { sub(state) }
    }

    private fun monitorPlayback() {
        Log.d("PlaybackController", "Started monitoring playback")
        handler.postDelayed(monitorPlaybackStateTask, 250)
    }

    private fun stopMonitoringPlayback() {
        Log.d("PlaybackController", "Stopped monitoring playback")
        handler.removeCallbacks(monitorPlaybackStateTask)
        subscription?.cancel()
    }

    override fun playPause() : CallResult<Empty> {
        return if (state?.isPaused == true) player.resume() else player.pause()
    }

    override fun seekTo(position: Long) : CallResult<Empty> { return player.seekTo(position) }
    override fun pause() : CallResult<Empty> { return player.pause() }
    override fun resume() : CallResult<Empty> { return player.resume() }
    override fun next() : CallResult<Empty> { return player.skipNext() }
    override fun previous() : CallResult<Empty> { return player.skipPrevious() }
    override fun play(uri: String) : CallResult<Empty> { return player.play(uri) }
    fun setShuffle(enabled: Boolean): CallResult<Empty> { return player.setShuffle(enabled) }
    fun toggleShuffle(): CallResult<Empty> { return if (state?.playbackOptions?.isShuffling == true)
        setShuffle(false) else setShuffle(true)
    }
    fun setRepeat(enabled: Boolean): CallResult<Empty> {
        return player.setRepeat( if (enabled) Repeat.ONE else Repeat.ALL )
    }
    fun toggleRepeat(): CallResult<Empty> { return if (state?.playbackOptions?.repeatMode == Repeat.ONE)
        setRepeat(false) else setRepeat(true)
    }
    fun disconnect() {
        stopMonitoringPlayback()
        SpotifyAppRemote.disconnect(remote)
    }
}