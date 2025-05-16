package com.buoyancy.playback.presentation.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.buoyancy.playback.R
import com.buoyancy.playback.model.IndicatorComponentState
import com.buoyancy.playback.presentation.ui.components.MusicPlayerCard
import com.buoyancy.playback.presentation.ui.components.Options
import com.buoyancy.playback.presentation.ui.presets.IndicatorPresets
import com.buoyancy.playback.presentation.ui.presets.OptionPresets
import com.buoyancy.playback.presentation.ui.color.ColorProvider
import com.buoyancy.playback.viewmodel.MusicPlayerViewModel

@Composable
fun NowPlayingScreen(
    viewModel: MusicPlayerViewModel
) {
    val queue = viewModel.musicService.queue.value
    val pagerState = rememberPagerState(initialPage = 0) { queue.size }
    val musicService = viewModel.musicService
    val presets = IndicatorPresets(viewModel)

    val pageHeightToScreenHeight = 0.7f
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val yCardOffset = screenHeight * (1f - pageHeightToScreenHeight) / 2f

    var isProgrammaticScroll by remember { mutableStateOf(false) }
    var previousPage by remember { mutableIntStateOf(pagerState.settledPage) }

    val backgroundColor = ColorProvider.primaryColor.value

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

        // Новый корневой элемент с квадратами и текстом
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            val indicatorYPadding = screenHeight * (1 - pageHeightToScreenHeight) / 2 - 40.dp

            // Верхний текст
            TrackLabel("ПРЕД: ${viewModel.prevTrackName.uppercase()}", Modifier
                .fillMaxWidth(0.45f)
                .padding(top = screenHeight * 0.04f)
                .align(Alignment.TopCenter)
            )

            // Верхний ряд квадратов
            IndicatorRow(
                indicator1 = presets.repeatState,
                indicator2 = presets.shuffleState,
                modifier = Modifier
                .padding(top = indicatorYPadding)
            )

            // Нижний текст
            TrackLabel("СЛЕД: ${viewModel.nextTrackName.uppercase()}", Modifier
                .fillMaxWidth(0.45f)
                .padding(bottom = screenHeight * 0.04f)
                .align(Alignment.BottomCenter)
            )

            // Нижний ряд квадратов
            IndicatorRow(
                indicator1 = presets.playingState,
                indicator2 = presets.savedState,
                modifier = Modifier
                .padding(bottom = indicatorYPadding)
                .align(Alignment.BottomCenter)
            )
        }

        Options(
            viewModel,
            OptionPresets.nowPlayingOptions(),
            OptionPresets.nowPlayingCallbacks(viewModel)
        )
    }
}

@OptIn(ExperimentalTextApi::class)
val manropeFont = FontFamily(
    Font(
        R.font.manrope_variable,
        variationSettings = FontVariation.Settings(
            FontVariation.weight(350)
        )
    )
)

@Composable
fun Indicator(state: IndicatorComponentState) {
    val lightColor = ColorProvider.lightColor.value
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(40.dp))
    {
        Icon(
            imageVector = if (state.activated) state.activatedIcon else state.nonActivatedIcon,
            contentDescription = null,
            tint = lightColor,
            modifier = Modifier.size(30.dp)
        )
    }
}

@Composable
fun IndicatorRow(indicator1: IndicatorComponentState, indicator2: IndicatorComponentState, modifier: Modifier = Modifier) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp)
            .then(modifier),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Indicator(indicator1)
        Indicator(indicator2)
    }
}

@Composable
fun TrackLabel(label: String, modifier: Modifier = Modifier) {
    Text(
        text = label,
        modifier = modifier,
        color = Color.White,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        fontFamily = manropeFont,
        fontSize = 13.sp
    )
}