package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class PatientListReqModel(
    var skip: Int,
    var limit: Int,
    @SerializedName("tenant_id")
    var tenantId: Long? = null,
    @SerializedName("first_name")
    var firstName: String? = null,
    @SerializedName("last_name")
    var lastName: String? = null,
    @SerializedName("phone_number")
    var phoneNumber: String? = null,
    var sort: HashMap<String, Any>? = null,
    @SerializedName("operating_unit")
    var operatingUnit: String? = null,
    var filter: FilterModel? = null,
    @SerializedName("is_labtest_referred")
    var isLabtestReferred : Boolean? = null,
    @SerializedName("is_medication_prescribed")
    var isMedicationPrescribed : Boolean? = null,
)