package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class FillPrescriptionUpdateRequest(
    val patientTrackId: Long,
    val tenantId: Long,
    val patientVisitId: Long,
    val prescriptions: ArrayList<FillPrescription>
)

data class FillPrescription(
    val id: Long,
    val prescription: String,
    var prescriptionFilledDays: Int = 0,
    var reason: String? = null,
    var otherReasonDetail: String? = null,
    var instructionNote: String = "",
    var instructionUpdated: Boolean = false,
    @SerializedName("product_number")
    var productNumber: String? = null,
    @SerializedName("dosage_frequency_name")
    var dosageFrequencyName: String? = null,
    @SerializedName("medication_name")
    val medicationName: String,
)