package com.buoyancy.playback.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.buoyancy.playback.presentation.ui.components.GestureInterceptor
import com.buoyancy.playback.viewmodel.AlbumsViewModel
import com.buoyancy.playback.viewmodel.MusicPlayerViewModel

@Composable
fun MainScreen(
    playerViewModel: MusicPlayerViewModel,
    albumsViewModel: AlbumsViewModel
) {
    val pagerState = rememberPagerState(initialPage = 1) { 2 }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Cyan)
    ) { page ->
        when (page) {
            0 -> AlbumsScreen(viewModel = albumsViewModel)
            1 -> GestureInterceptor({ NowPlayingScreen(playerViewModel) }, playerViewModel)
        }
    }
}