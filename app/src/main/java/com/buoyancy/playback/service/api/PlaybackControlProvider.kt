package com.buoyancy.playback.service.api

interface PlaybackControlProvider {

    fun playPause()
    fun seekTo(position: Long)
    fun pause()
    fun resume()
    fun next()
    fun previous()
}