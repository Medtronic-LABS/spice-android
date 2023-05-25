package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class InitialEncounterRequest(
    var diagnosis: InitialDiagnosis? = null,
    var comorbidities: ArrayList<InitialComorbidities>? = null,
    var complications: ArrayList<InitialComplications>? = null,
    var isPregnant: Boolean? = null,
    var lifestyle: ArrayList<InitialLifeStyle>? = null,
    @SerializedName("currentMedications")
    var currentMedicationDetails: InitialCurrentMedicationDetails? = null
)

data class InitialDiagnosis(
    var isDiabetesDiagnosis: Boolean? = null,
    var diabetesPatientType: String? = null,
    var diabetesYearOfDiagnosis: String? = null,
    var isHtnDiagnosis: Boolean? = null,
    var htnYearOfDiagnosis: String? = null,
    var htnPatientType: String? = null,
    var diabetesDiagControlledType: String? = null,
    var diabetesDiagnosis: String? = null
)

data class InitialComorbidities(
    var comorbidityId: Long,
    var otherComorbidity: String? = null,
    var name: String
)

data class InitialComplications(
    var complicationId: Long,
    var otherComplication: String? = null,
    var name: String
)

data class InitialLifeStyle(
    @SerializedName("answer")
    var lifestyleAnswer: String? = null,
    var lifestyleId: Long? = null,
    var comments: String? = null,
    @SerializedName("is_answer_dependent")
    var isAnswerDependent: Boolean = false
)

data class InitialCurrentMedicationDetails(
    @SerializedName("adheringCurrentMed")
    var isAdheringCurrentMed: Boolean? = null,
    var adheringMedComment: String? = null,
    @SerializedName("drugAllergies")
    var isDrugAllergies: Boolean? = null,
    var allergiesComment: String? = null,
    var medications: ArrayList<CurrentMedicationRequest>? = null
)

data class CurrentMedicationRequest(
    @SerializedName("currentMedicationId")
    val currentmedicationId: Long,
    @SerializedName("otherMedication")
    var otherComplication: String? = null,
    var name: String,
    var type: String
)