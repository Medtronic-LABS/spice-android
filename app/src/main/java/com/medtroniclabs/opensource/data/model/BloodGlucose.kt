package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BloodGlucose(
    @SerializedName("id")
    val _id: Long? = null,
    val glucoseValue: String? = null,
    val glucoseType: String? = null,
    val glucoseDateTime: String? = null,
    val createdAt: String? = null,
    val hba1c: String? = null,
    val hba1cUnit:String? = null,
    val symptoms: ArrayList<String>? = null,
    val glucoseUnit:String ?= null
): Serializable