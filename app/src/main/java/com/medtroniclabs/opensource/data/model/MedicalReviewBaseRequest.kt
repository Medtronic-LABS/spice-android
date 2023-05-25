package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class MedicalReviewBaseRequest(
    val patientTrackId: Long,
    val tenantId: Long,
    val patientVisitId: Long? = null,
    var medicalReviewId: Long? = null,
    @SerializedName("detailedSummaryRequired")
    var isDetailedSummaryRequired: Boolean? = null,
    @SerializedName("latestRequired")
    val isLatestRequired: Boolean? = null,
    @SerializedName("medicalReviewSummary")
    val isMedicalReviewSummary: Boolean? = null
)

class PatientDetails(val visitID: Long, val initialReview: Boolean, val patientID: Long)