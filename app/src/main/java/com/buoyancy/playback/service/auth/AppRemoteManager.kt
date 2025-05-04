package com.buoyancy.playback.service.auth

import android.content.Context
import android.util.Log
import com.buoyancy.playback.R
import com.buoyancy.playback.utils.StringUtils.Companion.composeErrorMessage
import com.spotify.android.appremote.api.ConnectionParams
import com.spotify.android.appremote.api.Connector.ConnectionListener
import com.spotify.android.appremote.api.SpotifyAppRemote
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import com.buoyancy.playback.utils.ToastUtils.toast

class AppRemoteManager @Inject constructor(
    @ApplicationContext private val context: Context) {

    private val clientId = context.getString(R.string.CLIENT_ID)
    private val redirectUri = context.getString(R.string.REDIRECT_URI)
    private val subscribers = mutableListOf<ConnectionListener>()
    private val tag = "SpotifyPlaybackController"

    private var remote: SpotifyAppRemote ? = null

    init {
        connect()
    }

    fun connect() {
        val connectionParams = ConnectionParams.Builder(clientId)
            .setRedirectUri(redirectUri)
            .showAuthView(true)
            .build()

        SpotifyAppRemote.connect(context, connectionParams, object : ConnectionListener {
            override fun onConnected(appRemoteArg: SpotifyAppRemote) {
                Log.d(tag, "$tag connected to Spotify API")
                remote = appRemoteArg
                subscribers.forEach{ it.onConnected(remote) }
            }
            override fun onFailure(error: Throwable) {
                Log.e(tag, composeErrorMessage(tag, error), error)
                Log.d(tag, "$tag is retrying connection...")
                toast("Spotify app remote connection failed. Retrying...")
                Thread.sleep(1000)
                connect()
                return
            }
        })
    }

    fun subscribeForAppRemote(listener: ConnectionListener) {
        subscribers.add(listener)
        remote?.let { listener.onConnected(remote) }
    }
}