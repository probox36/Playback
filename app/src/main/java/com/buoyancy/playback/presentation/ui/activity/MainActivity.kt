package com.buoyancy.playback.presentation.ui.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.buoyancy.playback.presentation.ui.components.GestureInterceptor
import com.buoyancy.playback.presentation.ui.screens.NowPlayingScreen
import com.buoyancy.playback.presentation.ui.theme.PlaybackTheme
import com.buoyancy.playback.service.auth.TokenManager
import com.buoyancy.playback.utils.ToastUtils
import com.buoyancy.playback.viewmodel.MusicPlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import jakarta.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject lateinit var tokenManager: TokenManager
    private val playerViewModel: MusicPlayerViewModel by viewModels()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        tokenManager.handleAuthResult(resultCode, data)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ToastUtils.init(this)
        tokenManager.setActivity(this)
        tokenManager.requestToken()

        setContent {
            PlaybackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    GestureInterceptor({ NowPlayingScreen(playerViewModel) }, playerViewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("MainActivity", "Destroy method called")
        playerViewModel.disconnect()
        tokenManager.stopTokenRefreshing()
    }
}