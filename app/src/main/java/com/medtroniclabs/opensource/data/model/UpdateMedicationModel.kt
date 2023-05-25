package com.medtroniclabs.opensource.data.model

data class UpdateMedicationModel(
    var id: Long? = null,
    var isDeleted: Boolean? = null,
    var medicationId: Long? = null,
    var dosageForm: String? = null,
    var dosageUnitId: Long? = null,
    var dosageFrequencyId: Long? = null,
    var prescribedDays: Int? = null,
    var prescriptionRemainingDays: Int? = null,
    var prescribedSince: String? = null,
    var endDate: String? = null,
    var medicationName: String? = null,
    var dosageName: String? = null,
    var classification: String? = null,
    var classificationName: String? = null,
    var brandName: String? = null,
    var dosageUnitValue: String? = null,
    var dosageUnitName: String? = null,
    var dosageFrequencyName: String? = null,
    var instructionNote: String? = null,
    var discontinuedOn: String? = null,
    var dosageFormName: String? = null
)