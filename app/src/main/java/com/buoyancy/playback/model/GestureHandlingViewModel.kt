package com.buoyancy.playback.model

import androidx.lifecycle.ViewModel

abstract class GestureHandlingViewModel : ViewModel() {

    abstract fun onTap()
    abstract fun onDoubleTap()
    abstract fun onHorizontalDrag(dX: Float)
    abstract fun onHorizontalDragStart()
    abstract fun onHorizontalDragEnd()
    abstract fun onVerticalDrag(dY: Float)
    abstract fun onVerticalDragStart()
    abstract fun onVerticalDragEnd()
}