package com.buoyancy.playback.service.auth

import android.app.Activity
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.buoyancy.playback.R
import com.buoyancy.playback.model.TokenSubscription
import com.buoyancy.playback.presentation.ui.presets.ScopePresets.Companion.defaultScopes
import com.buoyancy.playback.utils.ToastUtils.toast
import com.spotify.sdk.android.auth.AuthorizationClient
import com.spotify.sdk.android.auth.AuthorizationRequest
import com.spotify.sdk.android.auth.AuthorizationResponse
import com.spotify.sdk.android.auth.AuthorizationResponse.Type.CODE
import com.spotify.sdk.android.auth.AuthorizationResponse.Type.ERROR
import com.spotify.sdk.android.auth.AuthorizationResponse.Type.TOKEN
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor() {

    // Private properties
    private val tag = "SpotifyWebApiConnector"
    private var authToken: String? = null
    private var tokenLifespan: Long? = null
    private val subscribers = mutableListOf<TokenSubscription>()
    private val handler = Handler(Looper.getMainLooper())
    private var currentActivity: Activity? = null

    private val tokenRefreshTask = object : Runnable {
        override fun run() {
            notifySubscribers { onTokenTemporarilyInvalid() }
            requestToken()
            tokenLifespan?.let { handler.postDelayed(this, it) }
        }
    }

    // Consts
    companion object {
        private const val AUTH_REQUEST_CODE = 1337
        private const val DEFAULT_TOKEN_LIFESPAN = 3550L * 1000
    }

    fun setActivity(activity: Activity) {
        currentActivity = activity
    }

    // Token request and refresh methods
    fun requestToken() {
        currentActivity?.let { activity ->
            AuthorizationClient.openLoginActivity(
                activity,
                AUTH_REQUEST_CODE,
                AuthorizationRequest.Builder(
                    activity.getString(R.string.CLIENT_ID),
                    TOKEN,
                    activity.getString(R.string.REDIRECT_URI)
                ).setScopes( defaultScopes() ).build()
            ).also { Log.d(tag, "Login activity opened") }
        } ?: throw IllegalStateException("Activity not set! Call setActivity() first.")
    }

    fun handleAuthResult(resultCode: Int, data: Intent?) {
        Log.d(tag, "Handling response")
        val response = AuthorizationClient.getResponse(resultCode, data)

        when (response.type) {
            TOKEN -> processTokenResponse(response)
            ERROR -> fail("received error response. See log for details", response.error)
            CODE -> fail("received code instead of token")
            else -> fail("spotify auth response is empty or contains an unknown error",
                "actual token type is ${response.type}. Error field of the response is ${response.error}"
            )
        }
    }

    private fun processTokenResponse(response: AuthorizationResponse) {
        response.accessToken?.let { token ->
            authToken = token
            tokenLifespan = (response.expiresIn - 50) * 1000L

            Log.d(tag, "Token received. Valid for $tokenLifespan ms")
            notifySubscribers { onTokenReceived(token) }
            scheduleTokenRefresh()
        } ?: fail("Token response with empty token")
    }

    private fun fail(message: String, error: String? = null) {
        Log.e(tag, message)
        error?.let { e -> Log.e(tag, "Received error: $e") }
        toast("Can't authorize in spotify api: $message")
        notifySubscribers { onFailure(IllegalStateException(message)) }
    }

    fun subscribeForToken(listener: TokenSubscription) {
        subscribers.add(listener)
        authToken?.let { listener.onTokenReceived(it) }
    }

    private fun scheduleTokenRefresh() {
        Log.d(tag, "Creating token refresh task")
        val delay = tokenLifespan ?: DEFAULT_TOKEN_LIFESPAN
        handler.postDelayed(tokenRefreshTask, delay)
        Log.d(tag, "Token refresh task will be executed in $delay ms")
    }

    fun stopTokenRefreshing() = handler.removeCallbacks(tokenRefreshTask)

    // Helper methods
    private inline fun notifySubscribers(action: TokenSubscription.() -> Unit) {
        subscribers.forEach { it.action() }
    }
}