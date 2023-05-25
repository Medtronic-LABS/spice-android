package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class PatientTransferNotificationCountResponse (
    @SerializedName("count")
    val patientTransferCount: Long
)