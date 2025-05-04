package com.buoyancy.playback.utils

import android.content.Context
import android.widget.Toast

object ToastUtils {
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    fun toast(text: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(appContext, text, duration).show()
    }
}