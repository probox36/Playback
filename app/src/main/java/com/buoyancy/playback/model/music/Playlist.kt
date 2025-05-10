package com.buoyancy.playback.model.music

import com.google.gson.annotations.SerializedName

data class Playlist(
    @SerializedName("name") val name: String,
    @SerializedName("images") val images: List<CoverImage>,
    @SerializedName("owner") val owner: User,
    @SerializedName("type") val type: String,
    @SerializedName("uri") val uri: String
)