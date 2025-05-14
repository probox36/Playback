package com.buoyancy.playback.service.networking.api.impl

import SavedTracksResponse
import android.util.Log
import com.buoyancy.playback.model.exceptions.SpotifyApiException
import com.buoyancy.playback.model.music.Playlist
import com.buoyancy.playback.model.music.PlaylistResponse
import com.buoyancy.playback.model.music.Queue
import com.buoyancy.playback.model.music.Track
import com.buoyancy.playback.service.networking.api.MusicLibraryProvider
import com.buoyancy.playback.service.networking.http.SpotifyApiClient
import retrofit2.Response

class SpotifyWebApi(
    accessToken: String
) : MusicLibraryProvider {

    private val tag = "SpotifyWebApi"
    private val apiClient = SpotifyApiClient(accessToken)

    private suspend fun <T> makeRequest(
        request: suspend () -> Response<T>,
        errorMessage: String
    ): T? {
        return try {
            val response = request()
            when {
                response.isSuccessful -> response.body()
                else -> handleErrorResponse(response)
            }
        } catch (e: Exception) {
            Log.e(tag, errorMessage, e)
            null
        }
    }

    suspend fun getQueueRaw(): Queue? {
        return makeRequest(
            { apiClient.service.getPlayerQueue() },
            "Queue request failed"
        )
    }

    suspend fun getUserPlaylistsRaw(): PlaylistResponse? {
        return makeRequest(
            { apiClient.service.getUserPlaylists(20, 0) },
            "Playlists request failed"
        )
    }

    suspend fun getSavedTracksRaw(): SavedTracksResponse? {
        return makeRequest(
            { apiClient.service.getSavedTracks(20, 0) },
            "Saved tracks request failed"
        )
    }

    override suspend fun getUserPlaylists(): List<Playlist> {
        return getUserPlaylistsRaw()?.playlists ?: emptyList()
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

    override suspend fun isTrackInSaved(id: String): Boolean? {
        val inSaved = makeRequest(
            { apiClient.service.getIfTracksInSaved(listOf(id)) },
            "Saved tracks request failed"
        )
        return if (!inSaved.isNullOrEmpty()) { inSaved[0] } else false
    }

    override suspend fun getSavedTracks(): List<Track?> {
        return getSavedTracksRaw()?.tracks?.map{ it.track } ?: listOf()
    }

    override suspend fun saveTrack(id: String) {
        makeRequest(
            { apiClient.service.saveTracks(listOf(id)) },
            "Save track request failed"
        )
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