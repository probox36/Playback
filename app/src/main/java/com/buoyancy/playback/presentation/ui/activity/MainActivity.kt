package com.buoyancy.playback.presentation.ui.activity

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import com.buoyancy.playback.presentation.ui.screens.NowPlayingScreen
import com.buoyancy.playback.presentation.ui.theme.PlaybackTheme
import com.buoyancy.playback.service.SpotifyPlaybackController
import com.buoyancy.playback.viewmodel.exceptions.NoConnectionToSpotifyException
import com.spotify.protocol.types.PlayerState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var playbackController: SpotifyPlaybackController
    private var currentTrackName = mutableStateOf("")
    private var playbackPosition = mutableStateOf("00:00")
    private var trackLength = mutableStateOf("00:00")
    private var coverUri = mutableStateOf("")

    private val noConnectionMessage = "Playback controller not initialized. Re-trying connection..."
    private val coverBaseUrl = "https://i.scdn.co/image/"
    private var coverHash = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            playbackController.subscribe { processPlayerState(it) }
            PlaybackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    NowPlayingScreen({ onPrevClick() }, { onPlayClick() }, { onNextClick() },
                    coverUri, currentTrackName, playbackPosition, trackLength)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("MainActivity", "Destroy method called")
        if (this::playbackController.isInitialized) {
            playbackController.disconnect()
        }
    }

    private fun processPlayerState(state: PlayerState) {
        currentTrackName.value = state.track.name
        playbackPosition.value = formatDuration(state.playbackPosition)
        trackLength.value = formatDuration(state.track.duration)

        val newUri = state.track.imageUri.raw
        if (newUri != null) {
            val newHash = newUri.substringAfterLast(":").trim('\'')
            if (newHash != coverHash) {
                coverHash = newHash
                coverUri.value = coverBaseUrl + coverHash
                Log.i("MainActivity", "New cover uri: ${coverUri.value}")
            }
        }
    }

    private fun executePlaybackAction(action: () -> Unit) {
        try {
            action()
        } catch (e: NoConnectionToSpotifyException) {
            Toast.makeText(this, noConnectionMessage, Toast.LENGTH_SHORT).show()
            playbackController.connect()
        }
    }

    private fun onPrevClick() {
        executePlaybackAction {
            playbackController.previous()
        }
    }
    private fun onNextClick() {
        executePlaybackAction {
            playbackController.next()
        }
    }
    private fun onPlayClick() {
        executePlaybackAction { playbackController.playPause() }
    }

    private fun formatDuration(milliseconds: Long): String {
        val totalSeconds = milliseconds / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60

        return String.format("%02d:%02d", minutes, seconds)
    }
}