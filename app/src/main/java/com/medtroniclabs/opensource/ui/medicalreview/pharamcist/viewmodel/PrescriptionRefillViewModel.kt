package com.medtroniclabs.opensource.ui.medicalreview.pharamcist.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.postError
import com.medtroniclabs.opensource.appextensions.postLoading
import com.medtroniclabs.opensource.appextensions.postSuccess
import com.medtroniclabs.opensource.common.StringConverter
import com.medtroniclabs.opensource.data.model.*
import com.medtroniclabs.opensource.db.tables.ShortageReasonEntity
import com.medtroniclabs.opensource.di.IoDispatcher
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams.TYPE_DELETE
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams.TYPE_REFILL
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams.TYPE_TRANSFER
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.medicalreview.repo.MedicalReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrescriptionRefillViewModel @Inject constructor(
    private val medicalReviewRepo: MedicalReviewRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    var patient_track_id: Long? = null
    var patient_visit_id: Long? = null
    var last_refill_visit_id: String? = null
    var tenant_id: Long? = null

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    val patientRefillMedicationList =
        MutableLiveData<Resource<ArrayList<FillPrescriptionListResponse>>>()

    val fillPrescriptionUpdateRequest = MutableLiveData<Resource<FillPrescriptionResponse>>()

    val shortageReasonList = MutableLiveData<Resource<List<ShortageReasonEntity>>>()

    val patientRefillHistoryList =
        MutableLiveData<Resource<ArrayList<PrescriptionRefillHistoryResponse>>>()

    val transferReasonList = MutableLiveData<List<ShortageReasonEntity>>()
    val deleteReasonList = MutableLiveData<List<ShortageReasonEntity>>()

    var shortageReasonMap: ArrayList<FillMedicineResponse>? = null

    fun getPatientRefillMedicationList(request: FillPrescriptionRequest) {
        viewModelScope.launch(dispatcherIO) {
            try {
                patientRefillMedicationList.postLoading()
                val response = medicalReviewRepo.getPatientFillPrescriptionList(request)
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true)
                        patientRefillMedicationList.postSuccess(res.entityList ?: ArrayList())
                    else
                        patientRefillMedicationList.postError()
                } else {
                    patientRefillMedicationList.postError()
                }
            } catch (e: Exception) {
                patientRefillMedicationList.postError()
            }
        }
    }

    fun fillPrescriptionUpdate(
        context: Context,
        patientTrackId: Long,
        tenantId: Long,
        patientVisitId: Long,
        prescriptions: ArrayList<FillPrescription>
    ) {
        viewModelScope.launch(dispatcherIO) {
            try {
                if (connectivityManager.isNetworkAvailable()) {
                    val request = FillPrescriptionUpdateRequest(
                        patientTrackId,
                        tenantId,
                        patientVisitId,
                        prescriptions
                    )
                    fillPrescriptionUpdateRequest.postLoading()
                    val response = medicalReviewRepo.fillPrescriptionUpdate(request)
                    if (response.isSuccessful) {
                        val res = response.body()
                        if (res?.status == true)
                            fillPrescriptionUpdateRequest.postSuccess(res.entity)
                        else
                            fillPrescriptionUpdateRequest.postError()
                    } else {
                        fillPrescriptionUpdateRequest.postError(
                            StringConverter.getErrorMessage(
                                response.errorBody()
                            )
                        )
                    }
                } else {
                    fillPrescriptionUpdateRequest.postError(context.getString(R.string.no_internet_error))
                }
            } catch (e: Exception) {
                fillPrescriptionUpdateRequest.postError()
            }

        }
    }

    fun getFillPrescriptionList(): ArrayList<FillPrescription> {
        val list: ArrayList<FillPrescription> = ArrayList()
        patientRefillMedicationList.value?.data?.filter { it.prescriptionFilledDays != 0 }
            ?.forEach { data ->
                data.prescriptionFilledDays?.let { filledDays ->
                    list.add(
                        FillPrescription(
                            data.id,
                            data.prescription,
                            filledDays,
                            data.reason,
                            data.otherReasonDetail,
                            data.instructionModified ?: data.instructionNote,
                            data.instructionUpdated,
                            productNumber = data.productNumber,
                            dosageFrequencyName = data.dosageFrequencyName,
                            medicationName = data.medicationName
                        )
                    )
                }
            }
        return list
    }

    fun getShortageReasonList() {
        viewModelScope.launch(dispatcherIO) {
            try {
                shortageReasonList.postLoading()
                shortageReasonList.postSuccess(medicalReviewRepo.getShortageReasonList(TYPE_REFILL))
            } catch (e: Exception) {
                shortageReasonList.postError()
            }
        }
    }

    fun getPrescriptionRefillHistory(context: Context, request: PatientPrescriptionModel) {
        viewModelScope.launch(dispatcherIO) {
            try {
                if (connectivityManager.isNetworkAvailable()) {
                    patientRefillHistoryList.postLoading()
                    val response = medicalReviewRepo.getPrescriptionRefillHistory(request)
                    if (response.isSuccessful) {
                        val res = response.body()
                        if (res?.status == true)
                            patientRefillHistoryList.postSuccess(res.entityList ?: ArrayList())
                        else
                            patientRefillHistoryList.postError()
                    } else {
                        patientRefillHistoryList.postError()
                    }
                } else {
                    patientRefillHistoryList.postError(context.getString(R.string.no_internet_error))
                }
            } catch (e: Exception) {
                patientRefillHistoryList.postError()
            }
        }
    }

    fun getTransferReasonList() {
        viewModelScope.launch(dispatcherIO) {
            transferReasonList.postValue(medicalReviewRepo.getShortageReasonList(TYPE_TRANSFER))
        }
    }

    fun getDeleteReasonList() {
        viewModelScope.launch(dispatcherIO) {
            val deleteList = medicalReviewRepo.getShortageReasonList(TYPE_DELETE)
            val list = ArrayList(deleteList)
            if (list.isNotEmpty()) {
                val itemIndex =
                    list.indexOfFirst { it.reason.contains(DefinedParams.Other, ignoreCase = true) }
                if (itemIndex >= 0 && (itemIndex+1) != list.size) {
                    val item = list.removeAt(itemIndex)
                    list.add(item)
                }
            }
            deleteReasonList.postValue(list)
        }
    }
}