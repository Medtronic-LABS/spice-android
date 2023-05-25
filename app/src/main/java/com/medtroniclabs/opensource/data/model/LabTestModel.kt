package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class LabTestModel(
    var testName: String? = null,
    var referredBy: Any? = null,
    var referredByDisplay: String? = null,
    var comment: String? = null,
    var resultComments: String? = null,
    var isReviewed: Boolean? = false,
    var patientLabtestResults: ArrayList<HashMap<String, Any>>? = null,
    var labTestId: Long? = null,
    var labTestName: String? = null,
    var referredDate: String? = null,
    var resultDate: String? = null,
    @SerializedName("id")
    val _id: Long? = null,
    var resultUpdateBy: HashMap<String, String>? = null,
    var patientVisitId: Long? = null,
    var resultDetails: ArrayList<Map<String, Any>>? = null,
    var referDateDisplay : String? =null,
    var isAbnormal: Boolean? = false
) : Serializable
