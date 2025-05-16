package com.buoyancy.playback.presentation.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.buoyancy.playback.R
import com.buoyancy.playback.model.music.Playlist
import com.buoyancy.playback.presentation.ui.color.ColorProvider
import com.buoyancy.playback.utils.ImgUtils.pickCover

@OptIn(ExperimentalTextApi::class)
@Composable
fun PlaylistCard(
    playlist: Playlist,
    isActive: Boolean
) {
    // Constants
    val lightColor = ColorProvider.lightColor.value
    val darkColor = ColorProvider.darkColor.value
    val targetWidth = LocalConfiguration.current.screenWidthDp.dp - 20.dp
    val imageSize = 300 * 300

    // Animations
    val animatedAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = if (isActive) tween(200) else tween(500),
        label = "tint_animation"
    )

    val borderRadius by animateIntAsState(
        targetValue = if (isActive) 20 else 50,
        animationSpec = if (isActive) tween(200) else tween(500),
        label = "border_animation"
    )

    // Fonts
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

    Box(
        modifier = Modifier
            .fillMaxHeight()
            .width(targetWidth)
            .clip(RoundedCornerShape(borderRadius))
            .background(darkColor)
    ) {
        // Background blur effect
        AsyncImage(
            model = pickCover(playlist.images, imageSize),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .blur(40.dp)
                .alpha(animatedAlpha),
            colorFilter = ColorFilter.tint(darkColor.copy(alpha = 0.25f), BlendMode.Darken)
        )

        // Content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(9.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Cover image
            AsyncImage(
                model = pickCover(playlist.images, imageSize),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(borderRadius, 20, 20, borderRadius))
            )

            Spacer(Modifier.width(15.dp))

            // Text content
            Column(Modifier.weight(1f)) {
                Text(
                    text = playlist.name,
                    color = lightColor, // Анимированный цвет текста
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                    fontFamily = unboundedFont,
                    fontSize = 16.sp
                )

                Text(
                    text = playlist.owner.name,
                    color = lightColor, // Анимированный цвет текста
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.fillMaxWidth(),
                    fontFamily = manropeFont,
                    fontSize = 14.sp
                )
            }
        }
    }
}