package com.medtroniclabs.opensource.ui.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.opensource.appextensions.postError
import com.medtroniclabs.opensource.appextensions.postLoading
import com.medtroniclabs.opensource.appextensions.postSuccess
import com.medtroniclabs.opensource.data.model.*
import com.medtroniclabs.opensource.di.IoDispatcher
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.ui.medicalreview.repo.MedicalReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicalReviewPatientHistoryViewModel @Inject constructor(
    private val medicalReviewRepo: MedicalReviewRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    var origin: String? = null
    var isFromCMR: Boolean = false
    var isMedicalReviewSummary = false
    val patientLabTestHistoryResponse =
        MutableLiveData<Resource<PatientLabTestHistoryResponse>>()

    val patientLabTestHistoryResponseByID =
        MutableLiveData<Resource<PatientLabTestHistoryResponse>>()

    var SelectedPatientId: Long? = null

    var SelectedPatientMedicalReviewId: Long? = null

    var SelectedPatientPrescription: Long? = null

    val patientMedicalHistoryListResponse = MutableLiveData<Resource<SummaryResponse>>()

    val patientMedicalHistoryListResponseByID = MutableLiveData<Resource<SummaryResponse>>()
    val reviewHistoryList = MutableLiveData<Resource<PatientMedicalReviewHistoryResponse>>()

    val patientPrescriptionHistoryResponse =
        MutableLiveData<Resource<PatientPrescriptionHistoryResponse>>()

    val patientPrescriptionHistoryResponseBYID =
        MutableLiveData<Resource<PatientPrescriptionHistoryResponse>>()

    fun getPatientLabTestHistory(request: PatientHistoryRequest) {
        viewModelScope.launch(dispatcherIO) {
            patientLabTestHistoryResponse.postLoading()
            try {
                val response = medicalReviewRepo.getPatientLabTestHistory(request)
                if (response.isSuccessful) {
                    val res = response.body()
                    if(res?.status == true)
                        patientLabTestHistoryResponse.postSuccess(res.entity)
                    else
                        patientLabTestHistoryResponse.postError()
                } else {
                    patientLabTestHistoryResponse.postError()
                }
            } catch (e: Exception) {
                patientLabTestHistoryResponse.postError()
            }
        }
    }

    fun getPatientLabTestHistoryById(request: PatientHistoryRequest) {
        viewModelScope.launch(dispatcherIO) {
            patientLabTestHistoryResponseByID.postLoading()
            try {
                val response = medicalReviewRepo.getPatientLabTestHistory(request)
                if (response.isSuccessful) {
                    val res = response.body()
                    if(res?.status == true)
                        patientLabTestHistoryResponseByID.postSuccess(res.entity)
                    else
                        patientLabTestHistoryResponseByID.postError()
                } else {
                    patientLabTestHistoryResponseByID.postError()
                }
            } catch (e: Exception) {
                patientLabTestHistoryResponseByID.postError()
            }
        }
    }

    fun getPatientMedicalHistory(request: MedicalReviewBaseRequest) {
        viewModelScope.launch(dispatcherIO) {
            patientMedicalHistoryListResponse.postLoading()
            try {
                val response = medicalReviewRepo.getPatientMedicalReviewSummary(request)
                if (response.isSuccessful) {
                    val res = response.body()
                    if(res?.status == true)
                        patientMedicalHistoryListResponse.postSuccess(res.entity)
                    else
                        patientMedicalHistoryListResponse.postError()
                } else {
                    patientMedicalHistoryListResponse.postError()
                }
            } catch (e: Exception) {
                patientMedicalHistoryListResponse.postError()
            }
        }
    }

    fun getPatientMedicalHistoryByID(request: MedicalReviewBaseRequest) {
        viewModelScope.launch(dispatcherIO) {
            patientMedicalHistoryListResponseByID.postLoading()
            try {
                val response = medicalReviewRepo.getPatientMedicalReviewSummary(request)
                if (response.isSuccessful) {
                    val res = response.body()
                    if(res?.status == true)
                        patientMedicalHistoryListResponseByID.postSuccess(res.entity)
                    else
                        patientMedicalHistoryListResponseByID.postError()
                } else {
                    patientMedicalHistoryListResponseByID.postError()
                }
            } catch (e: Exception) {
                patientMedicalHistoryListResponseByID.postError()
            }
        }
    }

    fun getPatientMedicalReviewHistoryList(request: MedicalReviewBaseRequest, canUpdateDate: Boolean = true) {
        viewModelScope.launch(dispatcherIO) {
            val datesList = reviewHistoryList.value?.data?.patientReviewDates
            reviewHistoryList.postLoading()
            try {
                val response = medicalReviewRepo.getPatientMedicalReviewHistoryList(request)
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true) {
                        if (res.entity != null) {
                            res.entity.let {
                                if (!canUpdateDate)
                                    it.patientReviewDates = datesList ?: ArrayList()
                                it.canUpdateDate = canUpdateDate
                                reviewHistoryList.postSuccess(it)
                            }
                        } else
                            reviewHistoryList.postSuccess(null)
                    } else {
                        reviewHistoryList.postError()
                    }
                } else {
                    reviewHistoryList.postError()
                }
            } catch (e: Exception) {
                reviewHistoryList.postError()
            }
        }
    }

    fun getPatientPrescriptionHistory(request: PatientHistoryRequest) {
        viewModelScope.launch(dispatcherIO) {
            patientPrescriptionHistoryResponse.postLoading()
            try {
                val response = medicalReviewRepo.getPatientPrescriptionHistoryList(request)
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true)
                        patientPrescriptionHistoryResponse.postSuccess(res.entity)
                    else
                        patientPrescriptionHistoryResponse.postError()
                } else {
                    patientPrescriptionHistoryResponse.postError()
                }
            } catch (e: Exception) {
                patientPrescriptionHistoryResponse.postError()
            }
        }
    }

    fun getPatientPrescriptionHistoryById(request: PatientHistoryRequest) {
        viewModelScope.launch(dispatcherIO) {
            patientPrescriptionHistoryResponseBYID.postLoading()
            try {
                val response = medicalReviewRepo.getPatientPrescriptionHistoryList(request)
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true)
                        patientPrescriptionHistoryResponseBYID.postSuccess(res.entity)
                    else
                        patientPrescriptionHistoryResponseBYID.postError()
                } else {
                    patientPrescriptionHistoryResponseBYID.postError()
                }
            } catch (e: Exception) {
                patientPrescriptionHistoryResponseBYID.postError()
            }
        }
    }

}