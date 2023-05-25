package com.medtroniclabs.opensource.data.ui

import com.google.gson.annotations.SerializedName

data class PatientBasicRequest(
    @SerializedName("id")
    var _id: Long
)