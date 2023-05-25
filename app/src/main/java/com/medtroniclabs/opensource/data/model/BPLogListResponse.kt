package com.medtroniclabs.opensource.data.model

import java.io.Serializable

data class BPLogListResponse(
    var skip: Int,
    var limit: Int,
    var total: Int ?= null,
    var bpLogList: ArrayList<BPResponse>?,
    var latestBpLog: BPResponse?,
    var bpThreshold: BPThreshold? = null
): Serializable

data class BPThreshold(
    var systolic: Int,
    var diastolic: Int
) : Serializable