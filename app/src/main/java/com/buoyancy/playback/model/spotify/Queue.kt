package com.buoyancy.playback.model.spotify

import com.google.gson.annotations.SerializedName

data class Queue(
    @SerializedName("currently_playing") val currentlyPlaying: Track?,
    @SerializedName("queue") val queue: List<Track>
)