package com.medtroniclabs.opensource.data.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class PrescriptionRefillHistoryResponse (
    val id: Long?,
    val medicationName: String?,
    val dosageUnitName: String?,
    val dosageUnitValue: String?,
    val dosageFormName: String?,
    val dosageFrequencyName: String?,
    val instructionNote: String?,
    val endDate: String?,
    val prescribedDays: Int?,
    val remainingPrescriptionDays: Int?,
    val prescriptionFilledDays: Int?,
    val tenantId: Long?,
    val createdAt: String,
    val classificationName: String?,
    val brandName: String?,
) : Parcelable