package com.medtroniclabs.opensource.data.model

import com.google.gson.annotations.SerializedName

data class EnrollmentResponse(
    @SerializedName("id")
    var _id: Long? = null,
    var firstName: String? = null,
    var middleName: String? = null,
    var lastName: String? = null,
    var enrollmentDate: String? = null,
    var age: Int? = null,
    var gender: String? = null,
    @SerializedName("virtualId")
    var programId: Long? = null,
    var nationalId: String? = null,
    var phoneNumber: String? = null,
    var siteName: String? = null
)
