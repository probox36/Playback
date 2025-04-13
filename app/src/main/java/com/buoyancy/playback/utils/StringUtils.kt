package com.buoyancy.playback.utils

class StringUtils {
    companion object {
        fun composeErrorMessage(emitter: String, error: Throwable): String {
            val excClass = error.javaClass.simpleName
            val excMessage = error.message
            return "$emitter has thrown $excClass exception: $excMessage"
        }
    }
}