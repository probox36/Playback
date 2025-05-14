package com.buoyancy.playback.service.networking.http

import SavedTracksResponse
import com.buoyancy.playback.model.music.PlaylistResponse
import com.buoyancy.playback.model.music.Queue
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Query

interface SpotifyApiTemplate {
    @GET("v1/me/player/queue")
    suspend fun getPlayerQueue(): Response<Queue>

    @GET("v1/me/playlists")
    suspend fun getUserPlaylists(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<PlaylistResponse>

    @GET("v1/me/tracks")
    suspend fun getSavedTracks(
        @Query("limit") limit: Int,
        @Query("offset") offset: Int
    ): Response<SavedTracksResponse>

    @GET("v1/me/tracks/contains")
    suspend fun getIfTracksInSaved(
        @Query("ids") ids: List<String>
    ): Response<List<Boolean?>>

    @PUT("v1/me/tracks")
    suspend fun saveTracks(
        @Query("ids") ids: List<String>
    ): Response<Unit>
}