package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class OptionsModel(
    @SerializedName("is_roles_required")
    var isRolesRequired: Boolean = true
)