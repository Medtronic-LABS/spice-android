package com.medtroniclabs.opensource.data.model

data class FillPrescriptionRequest(
    val patientTrackId: Long,
    val tenantId: Long
)