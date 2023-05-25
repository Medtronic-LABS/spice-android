package com.medtroniclabs.opensource.data.assesssment

import com.google.gson.annotations.SerializedName

data class SearchRequest(val searchId: String,
                         @SerializedName("is_search_user_org_patient")
                         val isSearchUserOrgPatient: Boolean = false)