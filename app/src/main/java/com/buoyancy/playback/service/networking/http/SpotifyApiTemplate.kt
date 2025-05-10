package com.buoyancy.playback.service.networking.http

import com.buoyancy.playback.model.music.PlaylistResponse
import com.buoyancy.playback.model.music.Queue
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface SpotifyApiTemplate {
    @GET("v1/me/player/queue")
    suspend fun getPlayerQueue(): Response<Queue>

    @GET("v1/me/playlists")
    suspend fun getUserPlaylists(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<PlaylistResponse>
}