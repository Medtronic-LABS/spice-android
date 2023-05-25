package com.medtroniclabs.opensource.data.model

data class SortModel(
    var isRedRisk: Boolean? = null,
    var isLatestAssessment: Boolean? = null,
    var isMedicalReviewDueDate: Boolean? = null,
    var isHighLowBp: Boolean? = null,
    var isHighLowBg: Boolean? = null,
    var isAssessmentDueDate: Boolean? = null,
    var isUpdated: Boolean? = null,
    var isCVDRisk: Boolean? = null
)