package com.buoyancy.playback.service.retrofit

import com.buoyancy.playback.service.retrofit.template.SpotifyApiTemplate
import okhttp3.OkHttpClient
import retrofit2.Retrofit

class SpotifyApiClient(private val accessToken: String) {

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://api.spotify.com/")
        .client(createOkHttpClient())
        .build()

    private fun createOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", "Bearer $accessToken")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    val service: SpotifyApiTemplate = retrofit.create(SpotifyApiTemplate::class.java)
}