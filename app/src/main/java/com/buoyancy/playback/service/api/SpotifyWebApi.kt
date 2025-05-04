package com.buoyancy.playback.service.api

import com.buoyancy.playback.model.spotify.Queue

interface SpotifyWebApi {
    suspend fun getPlayerQueue(): Queue?
}