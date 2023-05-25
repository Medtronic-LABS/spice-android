package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

/**
 * [tenantId] - Used only for API Headers
 **/
data class LoginResponse(
    val firstName: String?,
    val username: String?,
    val lastName: String?,
    val isActive: Boolean,
    val isBlocked: Boolean,
    val token: String,
    @SerializedName("id")
    val _id: Long,
    var deviceInfoId: Long?,
    val countryCode: String?,
    val country: CountryModel,
    var timezone: TimeZoneModel? = null,
    var tenantId: Long,
    var cultureId: Long? = null
)

data class TimeZoneModel(
    @SerializedName("id")
    val _id: Long,
    val offset: String? = null
)

data class CountryModel(
    val id: Long,
    val name: String,
    val countryCode: String? = null
)