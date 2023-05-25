package com.medtroniclabs.opensource.data.model

data class PatientsDataModel(
    var skip: Int? = null,
    var limit: Int? = null,
    var tenantId: Long? = null,
    val searchId: String? = null,
    val isSearchUserOrgPatient: Boolean? = null,
    val operatingUnitId: Long? = null,
    val firstName: String? = null,
    val lastName: String? = null,
    val phoneNumber: String? = null,
    val isLabtestReferred: Boolean? = null,
    val isMedicationPrescribed: Boolean? = null,
    var patientSort: SortModel? = null,
    var patientFilter: FilterModel? = null
)