package com.medtroniclabs.opensource.data.model

data class AssessmentListRequest(
    val patientTrackId: Long,
    val latestRequired: Boolean,
    var limit: Int? = null,
    var sortField: String? = null,
    var skip: Int = 0,
    val tenantId: Long? = null
)
