package com.buoyancy.playback.service.networking.api

import com.buoyancy.playback.model.music.Playlist
import com.buoyancy.playback.model.music.Track

interface MusicLibraryProvider {
    suspend fun getQueue(): List<Track?>
    suspend fun getPrevious(): Track?
    suspend fun getCurrent(): Track?
    suspend fun getNext(): Track?
    suspend fun getUserPlaylists(): List<Playlist?>
}