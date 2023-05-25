package com.medtroniclabs.opensource.data.model

data class PatientRemoveRequest(
    val patientTrackId:Long,
    val tenantId:Long,
    val id: Long? = null, //Patient Id
    var deleteReason:String? = null,
    val deleteOtherReason:String?= null
)