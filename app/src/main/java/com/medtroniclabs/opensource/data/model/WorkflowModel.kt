package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class WorkflowModel(
    @SerializedName("bp_log")
    val bpLog: Boolean? = null,
    @SerializedName("glucose_log")
    val glucoseLog: Boolean? = null,
    val phq4: Boolean? = null,
    val pregnancy: Boolean? = null
)
