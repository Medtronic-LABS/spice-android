package com.medtroniclabs.opensource.appextensions

import com.medtroniclabs.opensource.BuildConfig

/**
 * extension method to check the current context is debug
 * returns yes to the callback
 */
fun isDebug(callback: (yes: Boolean) -> Unit) {
    if (BuildConfig.DEBUG) {
        callback.invoke(true)
    } else {
        callback.invoke(false)
    }
}