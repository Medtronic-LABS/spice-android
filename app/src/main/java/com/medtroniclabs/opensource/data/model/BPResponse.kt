package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BPResponse(
    @SerializedName("id")
    val _id: Long,
    val avgSystolic: Double,
    val avgDiastolic: Double,
    val avgPulse: Double?,
    val bpTakenOn: String,
    val createdAt: String,
    val symptoms: ArrayList<String>? = null
): Serializable