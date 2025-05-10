package com.buoyancy.playback.model.music

import com.google.gson.annotations.SerializedName

data class User (
    @SerializedName("display_name") val name: String
)