package com.medtroniclabs.opensource.data.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

data class PatientPrescriptionHistoryResponse(
    val patientPrescription: ArrayList<PatientPrescription>,
    val prescriptionHistoryDates: ArrayList<VisitDateModel>
)
@Parcelize
data class PatientPrescription(
    val medicationName: String?,
    val dosageUnitValue: String?,
    val dosageUnitName: String?,
    val dosageFrequencyName: String?,
    val patientVisitId: Long?,
    val createdAt: String,
    val prescribedSince: String?,
    val dosageFormName: String?,
    val prescribedDays: Int?,
    val instructionNote: String?,
) : Parcelable