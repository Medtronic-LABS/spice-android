package com.medtroniclabs.opensource.data.enrollment

data class EnrollmentSummaryResponse(
    val details: LinkedHashMap<String, Any>,
    val treatmentPlan: LinkedHashMap<String, Any>
)