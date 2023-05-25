package com.medtroniclabs.opensource.data.model

import com.medtroniclabs.opensource.db.tables.*

data class MedicalReviewStaticResponse(
    val comorbidity: ArrayList<ComorbidityEntity>,
    val complications: ArrayList<ComplicationEntity>,
    val currentMedication: ArrayList<CurrentMedicationEntity>,
    val physicalExamination: ArrayList<PhysicalExaminationEntity>,
    val lifestyle: ArrayList<LifestyleEntity>,
    val complaints: ArrayList<ComplaintsEntity>,
    val treatmentPlanFormData: TreatmentPlanModel? = null
)
