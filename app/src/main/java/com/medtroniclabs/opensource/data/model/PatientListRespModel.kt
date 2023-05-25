package com.medtroniclabs.opensource.data.model

data class PatientListRespModel(
    val id: Long? = null,
    val nationalId: String? = null,
    val programId: Long? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val gender: String? = null,
    val age: Int? = null,
    val patientStatus: String? = null,
    val enrollmentAt: String? = null,
    val patientId: Long? = null,
    val initialReview: Boolean,
    val redRiskPatient: Boolean? = null,
    val screeningLogId:Long ?= null
)