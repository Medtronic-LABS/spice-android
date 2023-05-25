package com.medtroniclabs.opensource.data.model

data class PatientPrescriptionModel(
    var id: Long? = null,
    var tenantId: Long? = null,
    var patientVisitId: Long? = null,
    var patientTrackId: Long? = null,
    var isDeleted: Boolean? = null,
    var prescriptionList: ArrayList<UpdateMedicationModel>? = null,
    var items: ArrayList<PrescriptionModel>? = null,
    var discontinuedReason: String? = null,
    var lastRefillVisitId: String? = null
)