package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class PatientHistoryRequest(
    @SerializedName("latestRequired")
    val isLatestRequired: Boolean,
    val patientTrackId: Long,
    val tenantId: Long,
    val limit: Int? = null,
    val skip: Int? = null,
    var patientVisitId: Long? = null,
    var prescriptionId: Long? = null
)