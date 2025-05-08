package com.buoyancy.playback.service.api.impl

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.buoyancy.playback.service.api.PlaybackControlProvider
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.types.PlayerState


class SpotifyPlaybackController(
    val remote: SpotifyAppRemote
) : PlaybackControlProvider {

    var notifySubscriber: (PlayerState) -> Unit = {}
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
                notifySubscriber(stateArg)
                state = stateArg
            }
            handler.postDelayed(this, pollingFrequency)
        }
    }

    init {
        monitorPlayback()
    }

    fun subscribeToPlayerState(listener: (PlayerState) -> Unit) {
        this.notifySubscriber = listener
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

    override fun playPause() {
        if (state?.isPaused == true) player.resume() else player.pause()
    }

    override fun seekTo(position: Long) { player.seekTo(position) }
    override fun pause() { player.pause() }
    override fun resume() { player.resume() }
    override fun next() { player.skipNext() }
    override fun previous() { player.skipPrevious() }
    fun disconnect() {
        stopMonitoringPlayback()
        SpotifyAppRemote.disconnect(remote)
    }
}