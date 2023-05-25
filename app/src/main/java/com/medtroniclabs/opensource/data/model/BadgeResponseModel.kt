package com.medtroniclabs.opensource.data.model

data class BadgeResponseModel(
    var prescriptionDaysCompletedCount: Int?,
    var nonReviewedTestCount: Int?,
    var nutritionLifestyleReviewedCount: Int?
)