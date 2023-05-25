package com.medtroniclabs.opensource.data.model

data class RegionSiteResponse(
    val id: Long,
    val name: String,
    val tenantId: Long,
    val operatingUnit: OperatingUnitModel,
)

data class OperatingUnitModel(
    val tenantId: Long
)
