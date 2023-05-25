package com.medtroniclabs.opensource.data.assesssment

data class AssessmentPatientResponse(
    val patientDetails: PatientDetails?,
    val glucoseLog: GlucoseLogModel?,
    val bpLog: BPLogModel?,
    val phq4: PHQ4Model?,
    val confirmDiagnosis: ArrayList<String>? = null,
    val symptoms: ArrayList<SymptomResponse>? = null,
    val medicalCompliance: ArrayList<MedicalComplianceResponse>? = null,
    val riskLevel: String?,
    val riskMessage: String?
)

data class GlucoseLogModel(
    val glucoseValue: Double?,
    val glucoseType: String?,
    val glucoseUnit: String? = null
)

data class BPLogModel(
    val avgSystolic: Double,
    val avgDiastolic: Double,
    val bmi: Double,
    val cvdRiskScore: Double,
    val cvdRiskLevel: String
)

data class PatientDetails(
    val firstName: String,
    val lastName: String,
    val age: Double,
    val gender: String,
    val programId: Double?,
    val nationalId: String,
    val siteName: String
)

data class PHQ4Model(
    val phq4Score: Double,
    val phq4RiskLevel: String
)

data class SymptomResponse(
    val otherSymptom: String? = null,
    val name: String,
    val type: String? = null,
    val cultureValue:String ?= null
)

data class MedicalComplianceResponse(
    val name: String,
    val otherCompliance: String? = null,
    val cultureValue:String?= null
)