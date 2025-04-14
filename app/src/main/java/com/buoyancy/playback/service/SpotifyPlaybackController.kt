package com.buoyancy.playback.service

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.buoyancy.playback.R
import com.buoyancy.playback.utils.StringUtils.Companion.composeErrorMessage
import com.buoyancy.playback.viewmodel.exceptions.NoConnectionToSpotifyException
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector
import com.spotify.android.appremote.api.PlayerApi
import com.spotify.android.appremote.api.SpotifyAppRemote
import com.spotify.protocol.client.Subscription
import com.spotify.protocol.types.PlayerState
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class SpotifyPlaybackController @Inject constructor(
    @ApplicationContext private val context: Context) {


    private val clientId = context.getString(R.string.CLIENT_ID)
    private val redirectUri = context.getString(R.string.REDIRECT_URI)

    private var notifySubscriber: (PlayerState) -> Unit = {}
    private val tag = "SpotifyPlaybackController"
    private val exceptionMessage = "Spotify app remote connection not yet established"
    private val noConnectionException = NoConnectionToSpotifyException(exceptionMessage)
    private val handler = Handler(Looper.getMainLooper())

    var state: PlayerState ? = null
        private set
    var remote: SpotifyAppRemote ? = null
        private set
    var player: PlayerApi ? = null
        private set
    var subscription: Subscription<PlayerState>? = null
        private set

    init {
        connect()
    }

    fun connect() {

        val connectionParams = ConnectionParams.Builder(clientId)
            .setRedirectUri(redirectUri)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(context, connectionParams, object : Connector.ConnectionListener {
            override fun onConnected(appRemoteArg: SpotifyAppRemote) {
                Log.d(tag, "$tag connected to Spotify API")
                remote = appRemoteArg
                monitorPlayback()
                player = remote?.playerApi
            }
            override fun onFailure(error: Throwable) {
                Log.e(tag, composeErrorMessage(tag, error), error)
                Log.d(tag, "$tag is retrying connection...")
                Thread.sleep(1000)
                connect()
                return
            }
        })
    }

    fun subscribe(listener: (PlayerState) -> Unit) {
        this.notifySubscriber = listener
    }

    private fun checkConnectionAndRun(action: () -> Unit) {
        if (state == null)
            throw noConnectionException
        else
            action()
    }

    private val monitorPlaybackStateTask = object : Runnable {
        override fun run() {

            subscription?.cancel()
            subscription = remote?.playerApi?.subscribeToPlayerState()
            subscription?.setEventCallback {
                state -> this@SpotifyPlaybackController.state = state
                notifySubscriber(state)
            }
            handler.postDelayed(this, 500)
        }
    }

    private fun monitorPlayback() {
        handler.post(monitorPlaybackStateTask)
    }

    private fun stopMonitoring() {
        handler.removeCallbacks(monitorPlaybackStateTask)
        subscription?.cancel()
    }

    fun playPause() {
        checkConnectionAndRun {
            if (state?.isPaused == true)  player?.resume() else player?.pause()
        }
    }

    fun next() { checkConnectionAndRun { player?.skipNext() } }
    fun previous() { checkConnectionAndRun { player?.skipPrevious() } }

    fun disconnect() {
        remote.let {
            stopMonitoring()
            SpotifyAppRemote.disconnect(it)
        }
    }
}