package com.buoyancy.playback.service.networking.api

import com.spotify.protocol.client.CallResult
import com.spotify.protocol.types.Empty

interface PlaybackControlProvider {

    fun playPause() : CallResult<Empty>
    fun seekTo(position: Long) : CallResult<Empty>
    fun pause() : CallResult<Empty>
    fun resume() : CallResult<Empty>
    fun next() : CallResult<Empty>
    fun previous() : CallResult<Empty>
    fun play(uri: String) : CallResult<Empty>
}