package com.medtroniclabs.opensource.data.model

data class CreateContinuousMedicalRequest(
    var complaints: ArrayList<Long>? = null,
    var physicalExams: ArrayList<Long>? = null,
    var physicalExamComments: String? = null,
    var clinicalNote: String? = null,
    var complaintComments: String? = null,
    var comorbidities: ArrayList<InitialComorbidities>? = null,
    var complications: ArrayList<InitialComplications>? = null
)