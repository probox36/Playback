package com.buoyancy.playback.model.music

import com.google.gson.annotations.SerializedName

data class Track(
    @SerializedName("album") val album: Album,
    @SerializedName("artists") val artists: List<Artist>,
    @SerializedName("id") val id: String,
    @SerializedName("is_playable") val isPlayable: Boolean,
    @SerializedName("name") val name: String,
    @SerializedName("uri") val uri: String,
    @SerializedName("duration_ms") val duration: Long
) {
    override fun toString(): String {
        return name
    }
}