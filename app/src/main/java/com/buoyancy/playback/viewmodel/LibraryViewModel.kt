package com.buoyancy.playback.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.buoyancy.playback.model.GestureEvent
import com.buoyancy.playback.model.GestureHandler
import com.buoyancy.playback.service.MusicService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    val musicService: MusicService
) : GestureHandler, ViewModel() {

    // Events
    private val _gestureEvents = MutableSharedFlow<GestureEvent>()
    val gestureEvents: SharedFlow<GestureEvent> = _gestureEvents

    val playlists = musicService.playlists
    override fun onTap() {
        viewModelScope.launch {
            _gestureEvents.emit(GestureEvent.Tap)
        }
    }

    override fun onDoubleTap() {}
    override fun onHorizontalDrag(dX: Float) {}
    override fun onHorizontalDragStart() {}
    override fun onHorizontalDragEnd() {}
    override fun onVerticalDrag(dY: Float) {}
    override fun onVerticalDragStart() {}
    override fun onVerticalDragEnd() {}
}