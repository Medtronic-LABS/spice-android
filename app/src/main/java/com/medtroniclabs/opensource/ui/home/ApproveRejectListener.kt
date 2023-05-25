package com.medtroniclabs.opensource.ui.home

interface ApproveRejectListener {
    fun onTransferStatusUpdate(
        status: String,
        id: Long,
        tenantId: Long,
        reason: String
    )

    fun onViewDetail(patientID: Long)
}