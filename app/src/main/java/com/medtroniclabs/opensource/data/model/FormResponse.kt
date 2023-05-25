package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class FormResponse (
    @SerializedName("Input_form")
    val inputForm: String,
    @SerializedName("Consent_form")
    val consentForm: String )
