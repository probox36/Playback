package com.buoyancy.playback.model

sealed class GestureEvent {
    data object VerticalDragEnded : GestureEvent()
    data object Tap : GestureEvent()
}