package com.medtroniclabs.opensource.data.model

data class GlucoseLogResponse(
    var _id: Long? = null,
    var glucoseValue: Double? = null,
    var glucoseType: String? = null,
    var glucoseDateTime: String? = null,
    var lastMealTime: String? = null,
    var isLatest: Boolean? = null,
    val glucoseUnit: String? = null
)
