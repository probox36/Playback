package com.buoyancy.playback.service.api.impl

import android.util.Log
import com.buoyancy.playback.model.exceptions.SpotifyApiException
import com.buoyancy.playback.model.music.Queue
import com.buoyancy.playback.model.music.Track
import com.buoyancy.playback.service.api.MusicLibraryProvider
import com.buoyancy.playback.service.retrofit.SpotifyApiClient
import retrofit2.Response

class SpotifyWebApi(
    accessToken: String
) : MusicLibraryProvider {

    private val tag = "SpotifyWebApi"
    private val apiClient = SpotifyApiClient(accessToken)

    suspend fun getQueueRaw(): Queue? {
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

    override suspend fun getQueue(): List<Track?> {
        val queueRaw = getQueueRaw()
        val queue = mutableListOf(queueRaw?.currentlyPlaying)
        queueRaw?.queue?.let { queue.addAll(it) }
        return queue
    }

    override suspend fun getPrevious(): Track? {
        return null
    }

    override suspend fun getCurrent(): Track? {
        return getQueueRaw()?.currentlyPlaying
    }

    override suspend fun getNext(): Track? {
        return getQueueRaw()?.queue?.first()
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