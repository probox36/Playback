package com.buoyancy.playback.service.retrofit.template

import com.buoyancy.playback.model.music.Queue
import retrofit2.Response
import retrofit2.http.GET

interface SpotifyApiTemplate {
    @GET("v1/me/player/queue")
    suspend fun getPlayerQueue(): Response<Queue>
}