package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class PrescriberModel(
    var _id: String?,
    var firstName: String?,
    var lastName: String?,
    var phoneNumber: String?,
    var countryCode: String?,
    @SerializedName("created_at")
    var createdAt: String?,
    @SerializedName("updated_at")
    var updatedAt: String?,
    @SerializedName("lastRefillDate")
    var lastRefilDate: String?,
    var lastRefillVisitId: String?
)