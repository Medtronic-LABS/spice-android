package com.medtroniclabs.opensource.data.screening

import com.google.gson.annotations.SerializedName

data class SiteDetails(
    var category: String = "",
    @SerializedName("category_type")
    var categoryType: String? = null,
    @SerializedName("site_name")
    var siteName: String = "",
    @SerializedName("site_id")
    var siteId: Long? = null,
    @SerializedName("tenant_id")
    var tenantId: Long? = null,
    @SerializedName("category_display_type")
    var categoryDisplayType: String? = null,
    var categoryDisplayName:String ?= null
)