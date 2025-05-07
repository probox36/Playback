package com.buoyancy.playback.model.music

import com.google.gson.annotations.SerializedName

data class CoverImage(
    @SerializedName("url") val url: String,
    @SerializedName("height") val height: Int,
    @SerializedName("width") val width: Int
)