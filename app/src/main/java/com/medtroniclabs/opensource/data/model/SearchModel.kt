package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class SearchModel(
    @SerializedName("searchTerm")
    val searchValue: String? = null,
    @SerializedName("countryId")
    val country: Long? = null,
    val isActive: Boolean? = null
)