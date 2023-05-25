package com.medtroniclabs.opensource.data.model

data class LifeStyleManagement(
    var referredBy: Any? = null,
    var referredFor: String? = null,
    var referredByDisplay: String? = null,
    var clinicianNote: String? = null,
    var resultComments: String? = null,
    var lifestyleAssessment: String? = null,
    var otherNote: String? = null,
    var referredDate: String? = null,
    var assessedDate: String? = null,
    val id: Long? = null,
    var assessedBy: HashMap<String, String>? = null,
    var patientVisitId: Long? = null,
    var patientTrackId: Long? = null,
    var lifestyle: List<Any>? = null,
    var isExpanded: Boolean = false,
    var message: String? = null,
    var nutritionist: Boolean = false,
    var roleName: String? = null,
    var cultureValue:String? =null
)