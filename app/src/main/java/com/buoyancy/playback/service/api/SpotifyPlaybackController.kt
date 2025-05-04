package com.buoyancy.playback.service.api

import com.spotify.protocol.types.PlayerState

interface SpotifyPlaybackController {

    fun playPause()
    fun seekTo(position: Long)
    fun pause()
    fun resume()
    fun next()
    fun previous()
    fun disconnect()
    fun subscribeToPlayerState(listener: (PlayerState) -> Unit)
}