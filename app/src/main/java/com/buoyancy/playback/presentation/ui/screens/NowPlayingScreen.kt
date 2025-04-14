package com.buoyancy.playback.presentation.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.buoyancy.playback.viewmodel.MusicPlayerViewModel

@Composable
fun NowPlayingScreen(
    viewModel: MusicPlayerViewModel,
    modifier: Modifier = Modifier
) {
    Surface(modifier = modifier.fillMaxWidth()) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            // Картинка альбома
            AsyncImage(
                model = viewModel.coverUri.value,
                contentDescription = null,
                modifier = Modifier
                    .size(240.dp)
                    .clip(MaterialTheme.shapes.medium),
                placeholder = ColorPainter(Color.Gray),
                error = ColorPainter(Color.Gray)
            )

            Spacer(Modifier.height(16.dp))

            // Название альбома
            Text(
                text = viewModel.currentTrackName.value,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.width(240.dp)
            )

            Spacer(Modifier.height(16.dp))

            // Ряд с временными метками
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = viewModel.playbackPosition.value,
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = viewModel.trackLength.value,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Spacer(Modifier.height(16.dp))

            // Кнопки управления
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Кнопка "Предыдущий трек"
                Button(
                    onClick = { viewModel.onPrevClick() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Text("⏮")
                }

                Spacer(Modifier.width(16.dp))

                // Кнопка "Пауза/Воспроизведение"
                Button(
                    onClick = { viewModel.onPlayClick() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Text("⏯")
                }

                Spacer(Modifier.width(16.dp))

                // Кнопка "Следующий трек"
                Button(
                    onClick = { viewModel.onNextClick() },
                    modifier = Modifier.size(64.dp)
                ) {
                    Text("⏭")
                }
            }
        }
    }
}