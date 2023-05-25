package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class RiskFactorResponse(
    @SerializedName("non_lab")
    var nonLab: ArrayList<RiskClassificationModel>?
)