package com.buoyancy.playback.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import com.buoyancy.playback.presentation.ui.components.MusicPlayerCard
import com.buoyancy.playback.presentation.ui.components.Options
import com.buoyancy.playback.presentation.ui.presets.OptionPresets
import com.buoyancy.playback.viewmodel.MusicPlayerViewModel

@Composable
fun NowPlayingScreen(
    viewModel: MusicPlayerViewModel
) {
    val backgroundColor = Color(0xFF702A24)
    val pagerState = rememberPagerState(initialPage = 1, pageCount = { 3 })
    val pageHeightToScreenHeight = 0.7f
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val yCardOffset = screenHeight.times((1f - pageHeightToScreenHeight) / 2f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        VerticalPager(
            state = pagerState,
            pageSpacing = 40.dp,
            pageSize = PageSize.Fixed(screenHeight.times(pageHeightToScreenHeight)),
            contentPadding = PaddingValues(top = yCardOffset, bottom = yCardOffset),
            userScrollEnabled = true
        ) {
            MusicPlayerCard(viewModel)
        }

        Options(
            viewModel,
            OptionPresets.nowPlayingOptions(),
            OptionPresets.nowPlayingCallbacks( viewModel )
        )

        // Обработка событий скролла
        LaunchedEffect(pagerState.currentPage) {
            when (pagerState.currentPage) {
                0 -> {
//                    viewModel.triggerToast("onPrevCardSwipe()")
                }
                2 -> {
//                    viewModel.triggerToast("onNextCardSwipe()")
                }
            }
        }
    }
}