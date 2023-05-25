package com.medtroniclabs.opensource.data.model

import com.medtroniclabs.opensource.data.ui.BPModel

data class BPLogResponse(
    var _id: String? = null,
    var bplogDetails: ArrayList<BPModel> = arrayListOf(),
    var isRegularSmoker: Boolean? = null,
    var avgSystolic: Double = 0.0,
    var avgDiastolic: Double = 0.0,
    var avgPulse: Double = 0.0,
    var height: Int? = null,
    var weight: Int? = null,
    var bmi: Double? = null,
    var temperature: Int? = null,
    var cvdRiskLevel: String? = null,
    var cvdRiskScore: Int? = null,
    var isLatest: Boolean? = null
)
