package com.medtroniclabs.opensource.common

import com.medtroniclabs.opensource.BuildConfig

object AppConstants {
    @kotlin.jvm.JvmStatic
    val SALT: String = BuildConfig.SALT
    @kotlin.jvm.JvmStatic
    val SHA_MAC = "HmacSHA512"
    val ANDROID = "Android"
    const val National_Id_Char_Length = 4
    const val National_Id_Phone_Length = 5
    const val SPICE_MOBILE = "spice mobile"
}