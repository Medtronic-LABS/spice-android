package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class RiskFactorModel(
    var bmi: String,
    var color: String? = null,
    var sbp: String,
    @SerializedName("risk_level")
    var riskLevel: String,
    @SerializedName("risk_score")
    var riskScore: Int
)
