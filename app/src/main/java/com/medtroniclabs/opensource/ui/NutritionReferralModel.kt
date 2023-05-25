package com.medtroniclabs.opensource.ui

data class NutritionReferralModel(
    val referrals: List<String>,
    val dateReferred: String,
    val notes: String,
    val accessedOn: String,
    val nutritionist: String
)