package com.medtroniclabs.opensource.data.model

data class TransferCreateRequest(
    val patientTrackId: Long,
    val tenantId: Long,
    val transferTo: Long?,
    val transferSite: Long?,
    val oldSite: Long?,
    val transferReason: String? = null
)
