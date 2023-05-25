package com.medtroniclabs.opensource.ui.assessment.repo

import com.google.gson.JsonObject
import com.medtroniclabs.opensource.network.ApiHelper
import javax.inject.Inject

class AssessmentRepository @Inject constructor(
    private var apiHelper: ApiHelper
) {
    suspend fun createPatientAssessment(createRequest: JsonObject) =
        apiHelper.createPatientAssessment(createRequest)
}