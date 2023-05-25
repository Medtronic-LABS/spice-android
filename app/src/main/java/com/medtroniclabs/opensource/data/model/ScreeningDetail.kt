package com.medtroniclabs.opensource.data.model

data class ScreeningDetail(
    val patientTrackId: Long,
    val screeningId: Long,
    val isAssessmentDataRequired: Boolean
)


