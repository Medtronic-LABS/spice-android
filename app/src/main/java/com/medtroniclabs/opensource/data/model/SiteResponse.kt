package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class SiteResponse(
    val name: String?,
    @SerializedName("id")
    val _id: Long,
    val roleName: List<String>,
    @SerializedName("displayName")
    val roleDisplayName: List<String>? = null,
    val tenantId: Long,
    val culture: Culture,
    @SerializedName("is_qualipharm_enabled_site")
    val isQualipharmEnabledSite: Boolean? = null
)

data class Culture(val id: Long)
