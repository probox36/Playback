package com.buoyancy.playback.utils

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.core.graphics.drawable.toBitmap
import androidx.palette.graphics.Palette
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.buoyancy.playback.model.music.CoverImage
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import kotlin.math.abs

object ImgUtils {

    fun pickCover(covers: List<CoverImage>, preferredCoverSize: Int): String {
        var chosenCover = covers.first().url
        var bestDelta = Int.MAX_VALUE

        covers.forEach{ c ->
            val delta = abs(preferredCoverSize - c.width * c.height)
            if (delta < bestDelta) {
                bestDelta = delta
                chosenCover = c.url
            }
        }
        return chosenCover
    }

    suspend fun loadBitmapByUrl(
        context: Context,
        url: String,
        imageLoader: ImageLoader = ImageLoader(context)
    ): Bitmap? {

        return try {
            val request = ImageRequest.Builder(context)
                .data(url)
                .allowHardware(false) // Важно: отключаем HARDWARE битмапы
                .build()

            when (val result = imageLoader.execute(request)) {
                is SuccessResult -> {
                    return result.drawable.toBitmap()
                }
                else -> {
                    Log.d("ImgUtils", "Failed to load bitmap from $url")
                    null
                }
            }
        } catch (e: Exception) { null }
    }

    suspend fun getDominantColor(bitmap: Bitmap): Color? = suspendCoroutine { continuation ->
        Palette.from(bitmap).generate { palette ->
            val color = palette?.dominantSwatch?.rgb?.let { Color(it) }
            continuation.resume(color)
        }
    }

    suspend fun getDominantColorByUrl(context: Context, url: String): Color? {
        return loadBitmapByUrl(context, url)?.let { getDominantColor(it) }
    }
}