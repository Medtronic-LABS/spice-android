package com.medtroniclabs.opensource.data.ui

import com.google.gson.annotations.SerializedName

class SymptomModel(
    @SerializedName("id")
    val _id: Long,
    val symptom: String,
    var isSelected: Boolean = false,
    val type: String? = null,
    val viewType: Int = 0,
    var otherSymptom: String? = null,
    @SerializedName("culture_value")
    val cultureValue:String? = null
)
