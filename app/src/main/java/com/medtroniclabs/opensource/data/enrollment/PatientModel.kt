package com.medtroniclabs.opensource.data.enrollment

data class PatientModel(
    val patientId: Long,
    val patientName: String,
    val patientAge: Int,
    val patientSex: Int,
    val patientImg: String
) {
    fun getAgeAndSex(): String {
        val text: String = when (patientSex) {
            0 -> "M / $patientAge yrs"
            1 -> "F / $patientAge yrs"
            2 -> "T / $patientAge yrs"
            else -> "Unknown / $patientAge yrs"
        }
        return text
    }
}