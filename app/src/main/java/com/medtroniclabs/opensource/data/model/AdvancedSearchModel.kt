package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class AdvancedSearchModel(
    @SerializedName("tenant_id")
    var tenantId: Long,
    @SerializedName("first_name")
    var firstName: String,
    @SerializedName("last_name")
    var lastName: String,
    @SerializedName("phone_number")
    var phoneNumber: String,
    var sort: LinkedHashMap<String, Any>? = null
)