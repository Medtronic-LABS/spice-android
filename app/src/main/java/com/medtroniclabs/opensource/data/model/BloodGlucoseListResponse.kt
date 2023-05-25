package com.medtroniclabs.opensource.data.model

import java.io.Serializable

data class BloodGlucoseListResponse(
    var skip: Int,
    var limit: Int,
    var total: Int,
    var glucoseLogList: ArrayList<BloodGlucose>?,
    var latestGlucoseLog: BloodGlucose?,
    var glucoseThreshold: List<GlucoseThreshold>? = null
) : Serializable

data class GlucoseThreshold(
    var fbs: Int,
    var rbs: Int,
    val unit: String
) : Serializable