package com.buoyancy.playback.service.api.impl

import android.util.Log
import com.buoyancy.playback.model.exceptions.SpotifyApiException
import com.buoyancy.playback.model.spotify.Queue
import com.buoyancy.playback.service.api.SpotifyWebApi
import com.buoyancy.playback.service.retrofit.SpotifyApiClient
import retrofit2.Response

class SpotifyWebApiImpl(
    accessToken: String
) : SpotifyWebApi {

    private val tag = "SpotifyWebApi"
    private val apiClient = SpotifyApiClient(accessToken)

    override suspend fun getPlayerQueue(): Queue? {
        return try {
            val response = apiClient.service.getPlayerQueue()
            when {
                response.isSuccessful -> response.body()
                else -> handleErrorResponse(response)
            }
        } catch (e: Exception) {
            Log.e(tag, "Queue request failed", e)
            null
        }
    }

    private fun handleErrorResponse(response: Response<*>): Nothing {
        when (response.code()) {
            401 -> throw SpotifyApiException("Bad or expired token")
            403 -> throw SpotifyApiException("Bad OAuth request")
            429 -> throw SpotifyApiException("The app has exceeded its rate limits")
            else -> throw SpotifyApiException(
                "Unknown API error: ${response.code()} - ${response.errorBody()?.string()}"
            )
        }
    }
}