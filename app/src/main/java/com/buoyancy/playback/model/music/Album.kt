package com.buoyancy.playback.model.music

import com.google.gson.annotations.SerializedName

data class Album(
    @SerializedName("id") val id: String,
    @SerializedName("images") val images: List<CoverImage>,
    @SerializedName("name") val name: String
)