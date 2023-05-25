package com.medtroniclabs.opensource.ui.medicalreview

import com.medtroniclabs.opensource.data.model.PatientListRespModel

interface PatientSelectionListener {
    fun onSelectedPatient(item: PatientListRespModel)
}