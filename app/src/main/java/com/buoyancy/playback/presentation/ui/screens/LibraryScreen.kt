package com.buoyancy.playback.presentation.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.PagerSnapDistance
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.flowWithLifecycle
import com.buoyancy.playback.R
import com.buoyancy.playback.model.GestureEvent
import com.buoyancy.playback.presentation.ui.components.PlaylistCard
import com.buoyancy.playback.presentation.ui.components.trackNameFontWeight
import com.buoyancy.playback.presentation.ui.color.ColorProvider
import com.buoyancy.playback.viewmodel.LibraryViewModel
import android.view.HapticFeedbackConstants as haptics

@OptIn(ExperimentalTextApi::class)
@Composable
fun LibraryScreen(
    viewModel: LibraryViewModel
) {
    val musicService = viewModel.musicService
    val playlists = viewModel.playlists
    val pagerState = rememberPagerState(initialPage = 3) { playlists.value.size }
    val lifecycleOwner = LocalLifecycleOwner.current
    val backgroundColor = ColorProvider.primaryColor.value
    val lightColor = ColorProvider.lightColor.value
    val density = LocalDensity.current
    val view = LocalView.current
    val statusBarHeight = with(density) { WindowInsets.statusBars.getTop(density).toDp() }

    LaunchedEffect(viewModel, lifecycleOwner) {
        viewModel.gestureEvents
            .flowWithLifecycle(lifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .collect { event ->
                if (event is GestureEvent.Tap) {
                    musicService.play(playlists.value[pagerState.settledPage].uri)
                        ?.setResultCallback { musicService.updateQueue() }
                }
            }
    }

    LaunchedEffect(pagerState.currentPage) { view.performHapticFeedback(haptics.SEGMENT_FREQUENT_TICK) }

    val unboundedFont = FontFamily(
        Font(
            R.font.unbounded_variable,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(trackNameFontWeight)
            )
        )
    )

    val titleScale by animateFloatAsState(
        targetValue = if (pagerState.targetPage < 4) 1f else 0f,
        animationSpec = tween(300),
        label = "title_scale_animation"
    )

    val fling = PagerDefaults.flingBehavior(
        state = pagerState,
        pagerSnapDistance = PagerSnapDistance.atMost(5)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Box(
            modifier = Modifier
                .padding(top = statusBarHeight)
                .fillMaxWidth()
                .height(100.dp)
        ) {
            Text(
                text = "Библиотека",
                fontSize = 28.sp,
                fontFamily = unboundedFont,
                color = lightColor,
                textAlign = TextAlign.Left,
                modifier = Modifier
                    .padding(25.dp)
                    .align(Alignment.CenterStart)
                    .scale(titleScale)
                    .alpha(titleScale),
                fontWeight = FontWeight.Bold
            )
        }

        VerticalPager(
            state = pagerState,
            pageSpacing = 7.dp,
            pageSize = PageSize.Fixed(100.dp),
            contentPadding = PaddingValues(
                vertical = LocalConfiguration.current.screenHeightDp.dp / 2 + 15.dp,
                horizontal = 10.dp
            ),
            flingBehavior = fling,
            userScrollEnabled = true,
            modifier = Modifier.fillMaxHeight()
        ) { page ->
            PlaylistCard(playlists.value[page], page == pagerState.currentPage)
        }
    }
}