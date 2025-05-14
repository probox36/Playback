package com.buoyancy.playback.model.music

import com.google.gson.annotations.SerializedName

class SavedTrack (
    @SerializedName("added_at") val addedAt: String,
    @SerializedName("track") val track: Track
)