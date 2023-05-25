package com.medtroniclabs.opensource.ui.enrollment.repo

import com.medtroniclabs.opensource.network.ApiHelper
import javax.inject.Inject

class PatientFilterRepository @Inject constructor(
    private var apiHelper: ApiHelper
) {
    fun getWardsList() = wardListTemp
    fun getDiagnosisList() = diagnosisListTemp

    private val wardListTemp = arrayListOf(
        mapOf("name" to "Ward1"),
        mapOf("name" to "Ward2"),
        mapOf("name" to "Ward3"),
        mapOf("name" to "Ward4"),
        mapOf("name" to "Ward5")
    )

    private val diagnosisListTemp = arrayListOf(
        mapOf("name" to "Diagnosis1"),
        mapOf("name" to "Diagnosis2"),
        mapOf("name" to "Diagnosis3"),
        mapOf("name" to "Diagnosis4"),
        mapOf("name" to "Diagnosis5")
    )

}