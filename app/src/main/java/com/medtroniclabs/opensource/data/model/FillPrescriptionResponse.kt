package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class FillPrescriptionResponse (
    var message : String,
    var data: ArrayList<FillMedicineResponse>? = null
    )

data class FillMedicineResponse(
    @SerializedName("medication_name")
    var medicationName:String,
    @SerializedName("distrubuted_quantity")
    var distrubutedQuantity: Int,
    @SerializedName("prescription_filled_days")
    var prescriptionFilled_days: Int
)
