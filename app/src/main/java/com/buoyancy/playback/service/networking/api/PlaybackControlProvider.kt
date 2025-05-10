package com.buoyancy.playback.service.networking.api

interface PlaybackControlProvider {

    fun playPause()
    fun seekTo(position: Long)
    fun pause()
    fun resume()
    fun next()
    fun previous()
    fun play(uri: String)
}