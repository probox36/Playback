package com.buoyancy.playback.utils

class StringUtils {
    companion object {
        fun composeErrorMessage(emitter: String, error: Throwable): String {
            val excClass = error.javaClass.simpleName
            val excMessage = error.message
            return "$emitter has thrown $excClass exception: $excMessage"
        }

        fun formatDuration(milliseconds: Long): String {
            val totalSeconds = milliseconds / 1000
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60

            return String.format("%02d:%02d", minutes, seconds)
        }
    }
}