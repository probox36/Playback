package com.buoyancy.playback.utils

import com.buoyancy.playback.model.music.CoverImage
import kotlin.math.abs

object ImgUtils {

    fun pickCover(covers: List<CoverImage>, preferredCoverSize: Int): String? {
        var chosenCover: String ? = null
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
}