package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName


/**
 * [tenantId] -> Should be passed in both request & header on changing facility
 */
data class DeviceInfo(
    val deviceId: String,
    val name: String,
    val model: String,
    val type: String,
    val version: String,
    @SerializedName("id")
    val deviceInfoId: Long? = null,
    var tenantId: Long? = null,
)