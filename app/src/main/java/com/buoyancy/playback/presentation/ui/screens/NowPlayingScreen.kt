package com.buoyancy.playback.presentation.ui.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.buoyancy.playback.presentation.ui.components.MusicPlayerCard
import com.buoyancy.playback.presentation.ui.components.Options
import com.buoyancy.playback.presentation.ui.presets.OptionPresets
import com.buoyancy.playback.viewmodel.MusicPlayerViewModel
import kotlinx.coroutines.delay

@Composable
fun NowPlayingScreen(
    viewModel: MusicPlayerViewModel
) {
    val backgroundColor = Color(0xFF702A24)
    val queue = viewModel.musicService.queue.value
    val pagerState = rememberPagerState(initialPage = 0) { queue.size }
    val musicService = viewModel.musicService

    val pageHeightToScreenHeight = 0.7f
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val yCardOffset = screenHeight * (1f - pageHeightToScreenHeight) / 2f

    var isProgrammaticScroll by remember { mutableStateOf(false) }
    var previousPage by remember { mutableIntStateOf(pagerState.settledPage) }

    LaunchedEffect(pagerState.settledPage) {
        if (!isProgrammaticScroll) {
            when {
                pagerState.settledPage > previousPage -> viewModel.onNext()
                pagerState.settledPage < previousPage -> viewModel.onPrev()
            }
        }
        previousPage = pagerState.settledPage
        isProgrammaticScroll = false
    }

    LaunchedEffect(musicService.currentTrackUri, queue) {
        if (queue.isNotEmpty()) {
            val currentTrackUri = musicService.currentTrackUri
            val pagerTrackUri = queue[pagerState.settledPage]?.uri
            if (currentTrackUri != pagerTrackUri) {
                queue.indexOfFirst { it?.uri == musicService.currentTrackUri }
                    .takeIf { it != -1 }
                    ?.let { newIndex ->
                        isProgrammaticScroll = true
                        pagerState.animateScrollToPage(newIndex)
                    }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        VerticalPager(
            state = pagerState,
            pageSpacing = 40.dp,
            pageSize = PageSize.Fixed(screenHeight * pageHeightToScreenHeight),
            contentPadding = PaddingValues(vertical = yCardOffset),
            userScrollEnabled = true
        ) { page ->
            MusicPlayerCard(
                trackInd = page,
                viewModel = viewModel,
                isActive = remember(page) { derivedStateOf { pagerState.settledPage == page } },
                isVisible = remember(page) { derivedStateOf {
                    page in (pagerState.settledPage - 1)..(pagerState.settledPage + 1)
                } }
            )
        }

        Options(
            viewModel,
            OptionPresets.nowPlayingOptions(),
            OptionPresets.nowPlayingCallbacks(viewModel)
        )
    }
}