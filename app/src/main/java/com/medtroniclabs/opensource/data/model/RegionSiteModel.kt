package com.medtroniclabs.opensource.data.model

data class RegionSiteModel(
    val limit: Int? = null,
    val skip: Int? = null,
    val searchTerm: String? = null,
    val countryId: Long? = null,
    val tenantId: Long? = null
)
