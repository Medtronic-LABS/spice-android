package com.medtroniclabs.opensource.ui.medicalreview

interface MedicationListener {
    fun openMedicalHistory(prescriptionId: Long?)
    fun updateView(isEmpty: Boolean)
    fun deleteMedication(pos: Int, prescriptionId: Long?)
}