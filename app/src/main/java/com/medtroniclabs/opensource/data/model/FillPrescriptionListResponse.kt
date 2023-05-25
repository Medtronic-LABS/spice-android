package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class FillPrescriptionListResponse(
    val id: Long,
    val medicationName: String,
    val dosageUnitName: String?,
    val dosageUnitValue: String,
    val dosageFormName: String,
    val dosageFrequencyName: String,
    val prescribedDays: Int,
    var instructionNote: String = "",
    val endDate: String,
    val remainingPrescriptionDays: Int,
    var prescriptionFilledDays: Int? = null,
    val tenantId: Long,
    var reason: String? = null,
    var otherReasonDetail: String? = null,
    var instructionUpdated: Boolean = false,
    var prescription: String,
    var createdAt: String,
    var classificationName: String,
    var brandName: String,
    var instructionModified: String?,
    @SerializedName("product_number")
    var productNumber:String? = null,
) {
    var isSelected: Boolean = false
}