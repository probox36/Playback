package com.buoyancy.playback.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.buoyancy.playback.presentation.ui.components.GestureInterceptor
import com.buoyancy.playback.viewmodel.LibraryViewModel
import com.buoyancy.playback.viewmodel.MusicPlayerViewModel

@Composable
fun MainScreen(
    playerViewModel: MusicPlayerViewModel,
    libraryViewModel: LibraryViewModel
) {
    val pagerState = rememberPagerState(initialPage = 1) { 2 }

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize()
    ) { page ->
        when (page) {
            0 -> GestureInterceptor({ LibraryScreen(libraryViewModel) }, libraryViewModel)
            1 -> GestureInterceptor({ NowPlayingScreen(playerViewModel) }, playerViewModel)
        }
    }
}