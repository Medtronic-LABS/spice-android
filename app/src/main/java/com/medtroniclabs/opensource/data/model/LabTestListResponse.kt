package com.medtroniclabs.opensource.data.model

data class LabTestListResponse(
    var patientLabTest: ArrayList<LabTestModel>? = null,
    var patientLabtestDates: ArrayList<HashMap<String, Any>>? = null
)