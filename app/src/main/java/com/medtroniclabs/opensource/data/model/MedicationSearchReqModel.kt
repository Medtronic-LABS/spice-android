package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName


data class MedicationSearchReqModel(
    val searchTerm: String,
    @SerializedName("is_qualipharm_enabled_site")
    val isQualipharmEnabledSite: Boolean ?= null
)