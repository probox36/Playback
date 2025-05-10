package com.buoyancy.playback.presentation.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.buoyancy.playback.R
import com.buoyancy.playback.model.music.Track
import com.buoyancy.playback.utils.StringUtils.Companion.formatDuration
import com.buoyancy.playback.viewmodel.MusicPlayerViewModel
import com.buoyancy.playback.utils.ImgUtils.pickCover

val lightThemeColor = Color(0xFFF0E1DE)
const val trackNameFontWeight = 500
const val artistNameFontWeight = 550
val trackNameFontSize = 20.sp
val artistNameFontSize = 16.sp
private val preferredCoverSize = 300 * 300

@Composable
fun MusicPlayerCard(
    trackInd: Int,
    viewModel: MusicPlayerViewModel,
    isActive: State<Boolean>,
    isVisible: State<Boolean>,
    modifier: Modifier = Modifier,
    contentWidth: Dp = 300.dp
) {
    val queue: MutableState<List<Track?>> = viewModel.musicService.queue
    val track: Track? = if (queue.value.size > trackInd) queue.value[trackInd] else null
    val coverUri = track?.album?.images?.let { pickCover(it, preferredCoverSize) }

    BoxWithConstraints(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .then(modifier)
    ) {
        val scope = this
        val targetWidth = LocalConfiguration.current.screenWidthDp.dp - 20.dp
        // Основной контейнер карточки
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(
                    if (scope.maxHeight < targetWidth) scope.maxHeight else targetWidth
                )
                .clip(RoundedCornerShape(50))
        ) {
            // Размытый фон альбома
            AsyncImage(
                model = coverUri,
                contentDescription = "Album background",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .blur(20.dp),
                colorFilter = ColorFilter.tint(Color.Black.copy(alpha = 0.4f), BlendMode.Darken)
            )

            // Основное содержимое
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Обложка альбома
                AsyncImage(
                    model = coverUri,
                    contentDescription = "Album cover",
                    modifier = Modifier
                        .size(contentWidth)
                        .clip(RoundedCornerShape(30.dp)),
                    contentScale = ContentScale.Crop
                )

                Spacer(Modifier.height(16.dp))

                // Информация о треке
                TrackInfo(track, contentWidth)

                Spacer(Modifier.height(16.dp))

                // Прогресс-бар
                ProgressBar(
                    if (isActive.value) viewModel.playbackPosition else remember { mutableDoubleStateOf(0.0) },
                    if (isActive.value) viewModel.timePassedStr else remember { mutableStateOf("00:00") },
                    track?.duration?.let { formatDuration(it) } ?: "00:00",
                    viewModel.seeking,
                    contentWidth
                )

                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalTextApi::class)
@Composable
private fun TrackInfo(track: Track?, width: Dp = 300.dp) {

    val unboundedFont = FontFamily(
        Font(
            R.font.unbounded_variable,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(trackNameFontWeight)
            )
        )
    )
    val manropeFont = FontFamily(
        Font(
            R.font.manrope_variable,
            variationSettings = FontVariation.Settings(
                FontVariation.weight(artistNameFontWeight)
            )
        )
    )

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = track?.name ?: "",
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontFamily = unboundedFont,
            fontSize = trackNameFontSize,
            color = lightThemeColor,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(width)
        )

        Spacer(Modifier.height(5.dp))

        Text(
            text = track?.artists?.map{ it.name }?.joinToString(", ") ?: "",
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            fontFamily = manropeFont,
            fontSize = artistNameFontSize,
            color = lightThemeColor,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.width(width)
        )
    }
}