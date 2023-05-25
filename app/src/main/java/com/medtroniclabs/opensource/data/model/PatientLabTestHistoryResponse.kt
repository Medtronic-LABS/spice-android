package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class PatientLabTestHistoryResponse(
    val total: Int,
    val patientLabTest: ArrayList<LabTestHistory>,
    val patientLabtestDates: ArrayList<VisitDateModel>
)

data class LabTestHistory(
    @SerializedName("labTestName")
    val labtestName: String,
    val referredDate: String,
    val patientVisitId: Long
)
