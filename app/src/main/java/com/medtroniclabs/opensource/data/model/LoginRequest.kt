package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    var username: String,
    var password: String,
    var options: OptionsModel,
    @SerializedName("device_info")
    var deviceInfo: DeviceInfo
)

