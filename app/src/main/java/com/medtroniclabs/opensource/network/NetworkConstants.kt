package com.medtroniclabs.opensource.network

import com.medtroniclabs.opensource.BuildConfig

object NetworkConstants {
    const val BASE_URL = BuildConfig.SERVER_URL
    const val NETWORK_ERROR = "1"
    const val DEVICE_DETAILS = "devicedetails"
    const val LOGIN = "session"
    const val FORGOT_PASSWORD = "forgot-password"
}