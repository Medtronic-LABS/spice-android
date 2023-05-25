package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName


data class ConfirmDiagnosesRequest(
    var confirmDiagnosis: ArrayList<String>? = null,
    var diagnosisComments: String? = null,
    var patientTrackId: Long? = null,
    @SerializedName("tenant_id")
    var tenantId: Long? = null,
)