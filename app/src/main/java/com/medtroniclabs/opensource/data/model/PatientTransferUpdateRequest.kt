package com.medtroniclabs.opensource.data.model

data class PatientTransferUpdateRequest(
    val id: Long,
    val tenantId: Long,
    val rejectReason: String? = null,
    val transferStatus: String
)