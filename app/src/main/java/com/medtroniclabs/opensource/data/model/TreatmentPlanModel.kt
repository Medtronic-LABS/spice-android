package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class TreatmentPlanModel(
    val forms: ArrayList<FormsData>? = null,
    @SerializedName("label_name")
    var labelName: String? = null,
    @SerializedName("frequency_key")
    var frequencyKey: String? = null,
    val options: ArrayList<String>? = null,
)

data class FormsData(
    @SerializedName("label_name")
    val labelName: String? = null,
    @SerializedName("frequency_key")
    val frequencyKey: String? = null
)
