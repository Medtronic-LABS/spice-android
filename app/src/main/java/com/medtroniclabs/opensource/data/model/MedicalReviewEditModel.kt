package com.medtroniclabs.opensource.data.model

data class MedicalReviewEditModel(
    var initialMedicalReview: InitialEncounterRequest? = null,
    var continuousMedicalReview: CreateContinuousMedicalRequest? = null,
    var patientTrackId: Long? = -1,
    var patientVisitId: Long? = null,
    var tenantId: Long? = -1
)