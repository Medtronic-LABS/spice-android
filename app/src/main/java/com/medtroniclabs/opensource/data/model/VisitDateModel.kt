package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class VisitDateModel(
    val visitDate: String,
    @SerializedName("id")
    val _id: Long)