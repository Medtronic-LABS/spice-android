package com.medtroniclabs.opensource.common

import android.content.Context
import android.os.Build
import android.provider.Settings
import com.medtroniclabs.opensource.common.AppConstants.ANDROID
import com.medtroniclabs.opensource.data.model.DeviceInfo

object DeviceInformation {
    fun getDeviceDetails(context: Context): DeviceInfo {
        return DeviceInfo(
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID),
            Build.MANUFACTURER,
            Build.MODEL,
            ANDROID,
            Build.VERSION.RELEASE
        )
    }

}