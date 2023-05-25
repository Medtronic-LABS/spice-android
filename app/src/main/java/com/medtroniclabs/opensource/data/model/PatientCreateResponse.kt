package com.medtroniclabs.opensource.data.model

data class PatientCreateResponse(
    var enrollment: EnrollmentResponse? = EnrollmentResponse(),
    var glucoseLog: GlucoseLogResponse? = GlucoseLogResponse(),
    var bpLog: BPLogResponse? = BPLogResponse(),
    var phq4: PHQ4Response? = PHQ4Response(),
    val isConfirmDiagnosis: Boolean? = false,
    val confirmDiagnosis: ArrayList<String>? = null,
    val provisionalDiagnosis: ArrayList<String>? = null,
    var treatmentPlan: ArrayList<TreatmentPlanResponse>? = null
)