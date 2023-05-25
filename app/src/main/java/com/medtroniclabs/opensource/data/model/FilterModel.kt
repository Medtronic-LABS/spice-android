package com.medtroniclabs.opensource.data.model

data class FilterModel(
    var screeningReferral: Boolean? = null,
    var medicalReviewDate: String? = null,
    var isRedRiskPatient: Boolean? = null,
    var patientStatus: String? = null,
    var cvdRiskLevel: String? = null,
    var assessmentDate: String? = null,
    var labTestReferredDate: String? = null,
    var medicationPrescribedDate: String? = null
)