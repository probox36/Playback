package com.buoyancy.playback.model

interface GestureHandler {

    fun onTap()
    fun onDoubleTap()
    fun onHorizontalDrag(dX: Float)
    fun onHorizontalDragStart()
    fun onHorizontalDragEnd()
    fun onVerticalDrag(dY: Float)
    fun onVerticalDragStart()
    fun onVerticalDragEnd()
}