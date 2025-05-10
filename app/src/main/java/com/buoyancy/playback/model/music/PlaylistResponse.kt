package com.buoyancy.playback.model.music

import com.google.gson.annotations.SerializedName

class PlaylistResponse (
    @SerializedName("limit") val limit: Int,
    @SerializedName("offset") val offset: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("previous") val previousPage: String,
    @SerializedName("next") val nextPage: String,
    @SerializedName("items") val playlists: List<Playlist>,
)