package com.buoyancy.playback.model

interface TokenSubscription {

    fun onTokenReceived(token: String)
    fun onFailure(error: Throwable)
    fun onTokenTemporarilyInvalid()
}