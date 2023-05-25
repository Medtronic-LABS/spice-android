package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class SummaryResponse(
    var patientDetails: Patient?,
    var reviewerDetails: ReviewerDetails?,
    var isSigned: Boolean = false,
    var medicalReviews: ArrayList<MedicalReview>,
    var prescriptions: ArrayList<SummaryPrescription>?,
    var investigations: ArrayList<LabTestHistory>,
    @SerializedName("patientReviewDates")
    var medicalReviewDates: ArrayList<VisitDateModel>? = null,
    var reviewedAt: String?=null,
    var medicalReviewFrequency: String? = null,
    var refreshPatientDetails: Boolean = false
)

data class Patient(
    var isConfirmDiagnosis: Boolean = false,
    var provisionalDiagnosis: ArrayList<String>? = null,
    var confirmDiagnosis: ArrayList<String>? = null
)

data class ReviewerDetails(
    var firstName: String,
    var lastName: String,
    var username: String? = null
)

data class MedicalReview(
    @SerializedName("id")
    var _id: Long,
    var patientVisitId: Long,
    var complaints: ArrayList<CultureLanguageModel>? = null,
    var physicalExams: ArrayList<CultureLanguageModel>? = null,
    var clinicalNote: String? = null,
    var reviewedAt: String? = null,
    var complaintComments: String? = null,
    var physicalExamComments: String? = null
)

data class SummaryPrescription(
    @SerializedName("id")
    var _id: Long,
    var medicationName: String,
    var dosageUnitValue: String,
    var dosageUnitName: String,
    var dosageFrequencyName: String,
    var prescribedDays: Int,
    var instructionNote: String,
    var dosageFormName: String
)