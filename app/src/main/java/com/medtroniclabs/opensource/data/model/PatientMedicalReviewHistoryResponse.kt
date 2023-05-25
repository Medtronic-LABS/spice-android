package com.medtroniclabs.opensource.data.model

data class PatientMedicalReviewHistoryResponse(
    val patientMedicalReview: ArrayList<MedicalReview>,
    var patientReviewDates: ArrayList<VisitDateModel>,
    var canUpdateDate: Boolean = false
)