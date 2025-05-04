package com.buoyancy.playback.presentation.ui.presets

import com.buoyancy.playback.model.SpotifyScope.AppRemoteControl
import com.buoyancy.playback.model.SpotifyScope.PlaylistModifyPrivate
import com.buoyancy.playback.model.SpotifyScope.PlaylistModifyPublic
import com.buoyancy.playback.model.SpotifyScope.PlaylistReadCollaborative
import com.buoyancy.playback.model.SpotifyScope.PlaylistReadPrivate
import com.buoyancy.playback.model.SpotifyScope.Streaming
import com.buoyancy.playback.model.SpotifyScope.UserLibraryModify
import com.buoyancy.playback.model.SpotifyScope.UserLibraryRead
import com.buoyancy.playback.model.SpotifyScope.UserModifyPlaybackState
import com.buoyancy.playback.model.SpotifyScope.UserReadCurrentlyPlaying
import com.buoyancy.playback.model.SpotifyScope.UserReadPlaybackPosition
import com.buoyancy.playback.model.SpotifyScope.UserReadPlaybackState
import com.buoyancy.playback.model.SpotifyScope.UserReadRecentlyPlayed

class ScopePresets {
    companion object {

        fun defaultScopes(): Array<String> {
            return listOf(
                AppRemoteControl,
                PlaylistReadPrivate,
                PlaylistReadCollaborative,
                PlaylistModifyPublic,
                PlaylistModifyPrivate,
                Streaming,
                UserLibraryRead,
                UserLibraryModify,
                UserModifyPlaybackState,
                UserReadPlaybackState,
                UserReadPlaybackPosition,
                UserReadCurrentlyPlaying,
                UserReadRecentlyPlayed
            ).map { it.uri }.toTypedArray()
        }
    }
}