package com.medtroniclabs.opensource.data.model

    import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class PatientDetailsModel(
    @SerializedName("id")
    val _id: Long,
    val tenantId: Long? = null,
    val firstName: String? = null,
    val middleName: String? = null,
    val lastName: String? = null,
    val age: Double? = null,
    val gender: String? = null,
    val enrollmentAt: String? = null,
    val programId: Long? = null,
    val nationalId: String? = null,
    val phoneNumber: String? = null,
    val lastAssessmentDate: String? = null,
    val cvdRiskLevel: String? = null,
    val cvdRiskScore: Double? = null,
    val avgSystolic: Double? = null,
    val avgDiastolic: Double? = null,
    val bmi: Double? = null,
    val phq4Score: Int? = null,
    val glucoseValue: Double? = null,
    val glucoseUnit:String?= null,
    val provisionalDiagnosis: ArrayList<String>? = null,
    val isRegularSmoker: Boolean? = null,
    val confirmDiagnosis: ArrayList<String>? = null,
    val isConfirmDiagnosis: Boolean = false,
    val height: Double? = null,
    val weight: Double? = null,
    val phq9Score: Int? = null,
    val dateOfBirth: String? = null,
    val message: String? = null,
    val assessmentRequired: Boolean? = true,
    val patientTrackId: Long? = null,
    val landmark: String? = null,
    val phoneNumberCategory: String? = null,
    @SerializedName("assessmentDataRequired")
    var isAssessmentDataRequired: Boolean = false,
    @SerializedName("prescriberRequired")
    var isPrescriberRequired: Boolean = false,
    var prescriberDetails: PrescriberModel? = null,
    val gad7Score: Int? = null,
    val isPhq9: Boolean = false,
    val isGad7: Boolean = false,
    @SerializedName("redRiskPatient")
    var isRedRiskPatient: Boolean = false,
    var phq4RiskLevel: String? = null,
    var phq9RiskLevel: String? = null,
    var gad7RiskLevel: String? = null,
    var isPregnant: Boolean = false,
    var diagnosisComments: String? = null,
    @SerializedName("lifeStyleRequired")
    var isLifestyleRequired: Boolean = false,
    var patientLifestyles: ArrayList<LifestyleModel>? = null,
    var screeningLogId: Long? = null,
    var screeningId: Long? = null,
    var patientId:Long? = null,
    val isDiabetesDiagnosis: Boolean = false,
    val isHtnDiagnosis: Boolean = false,
    var lastMenstrualPeriodDate: String? = null,
    var estimatedDeliveryDate: String? = null
) : Serializable

data class PregnancyDetails(
    var lastMenstrualPeriodDate: String? = null,
    var estimatedDeliveryDate: String? = null
): Serializable

data class LifestyleModel(
    var comments: String? = null,
    var lifestyleAnswer: String? = null,
    var lifestyle: String? = null,
    var lifestyleType: String? = null
) : Serializable