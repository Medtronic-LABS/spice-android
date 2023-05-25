package com.medtroniclabs.opensource.ui.medicalreview.labTechnician

import com.medtroniclabs.opensource.data.model.LabTestModel

interface LabResultInterface{
    fun selectedLabResult(result: LabTestModel)
}