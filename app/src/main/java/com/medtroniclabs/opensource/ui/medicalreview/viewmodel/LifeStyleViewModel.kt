package com.medtroniclabs.opensource.ui.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.opensource.appextensions.postError
import com.medtroniclabs.opensource.appextensions.postLoading
import com.medtroniclabs.opensource.appextensions.postSuccess
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.*
import com.medtroniclabs.opensource.db.tables.NutritionLifeStyle
import com.medtroniclabs.opensource.di.IoDispatcher
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.ui.medicalreview.repo.MedicalReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LifeStyleViewModel @Inject constructor(
    private val medicalReviewRepo: MedicalReviewRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
    ) :
    ViewModel() {

    var patientTrackId: Long = -1
    var patientVisitId: Long = -1L
    val patientLifeStyleResponse = MutableLiveData<Resource<ArrayList<PatientLifeStyle>>>()
    val createLifestyleResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    val removeLifestyleResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    val lifestyleList = MutableLiveData<List<NutritionLifeStyle>>()
    val lifeStyleReferredList = MutableLiveData<ArrayList<LifeStyleManagement>?>()
    val newReferralData = MutableLiveData<LifeStyleManagement?>()
    val patientLifestyleList = MutableLiveData<Resource<ArrayList<LifeStyleManagement>>>()
    val clearBadgeNotificationResponse = MutableLiveData<Resource<ResponseDataModel>>()

    fun getPatientLifeStyleDetails(requestPatient: PatientLifeStyleRequest) {
        viewModelScope.launch(dispatcherIO) {
            try {
                patientLifeStyleResponse.postLoading()
                val response = medicalReviewRepo.getPatientLifeStyleDetails(requestPatient)
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true)
                        patientLifeStyleResponse.postSuccess(res.entityList ?: ArrayList())
                    else
                        patientLifeStyleResponse.postError()
                } else
                    patientLifeStyleResponse.postError()
            } catch (e: Exception) {
                patientLifeStyleResponse.postError()
            }
        }
    }

    fun createPatientLifestyle(requestPatient: LifeStyleManagement) {
        SecuredPreference.getSelectedSiteEntity()?.let {
            requestPatient.roleName = it.role
        }
        viewModelScope.launch(dispatcherIO) {
            try {
                createLifestyleResponse.postLoading()
                val response = medicalReviewRepo.createPatientLifestyle(requestPatient)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body?.status == true) {
                        createLifestyleResponse.postSuccess(data = body.entity)
                    } else {
                        createLifestyleResponse.postError()
                    }
                } else
                    createLifestyleResponse.postError()
            } catch (e: Exception) {
                createLifestyleResponse.postError()
            }
        }
    }

    fun getLifeStyleList() {
        viewModelScope.launch(dispatcherIO) {
            lifestyleList.postValue(medicalReviewRepo.getLifestyleList())
        }
    }

    fun getPatientLifestyleList() {
        viewModelScope.launch(dispatcherIO) {
            try {
                val request = PatientLifestyleModel(
                    patientTrackId = patientTrackId,
                    tenantId = SecuredPreference.getSelectedSiteEntity()?.tenantId,
                    nutritionist = false
                )
                patientLifestyleList.postLoading()
                val response = medicalReviewRepo.getPatientLifestyleList(request)
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true)
                        patientLifestyleList.postSuccess(res.entityList ?: ArrayList())
                    else
                        patientLifestyleList.postError()
                } else {
                    patientLifestyleList.postError()
                }
            } catch (e: Exception) {
                patientLifestyleList.postError()
            }
        }
    }

    fun removePatientLifestyle(id: Long?) {
        viewModelScope.launch(dispatcherIO) {
            try {
                val request = PatientLifestyleModel(
                    patientTrackId = patientTrackId,
                    patientVisitId = patientVisitId,
                    id = id
                )
                removeLifestyleResponse.postLoading()
                val response = medicalReviewRepo.removePatientLifestyle(request)
                if (response.isSuccessful) {
                    if (response.body()?.status == true) {
                        val hashMap = HashMap<String, Any>()
                        hashMap[DefinedParams.ID] = id!!
                        removeLifestyleResponse.postSuccess(hashMap)
                    } else
                        removeLifestyleResponse.postError()
                } else {
                    removeLifestyleResponse.postError()
                }
            } catch (e: Exception) {
                removeLifestyleResponse.postError()
            }
        }
    }

    fun clearBadgeNotification() {
        viewModelScope.launch(dispatcherIO) {
            try {
                val request = PatientLifestyleModel(
                    patientTrackId = patientTrackId,
                    patientVisitId = patientVisitId,
                    tenantId = SecuredPreference.getLoginTenantId()
                )
                clearBadgeNotificationResponse.postLoading()
                val response = medicalReviewRepo.clearBadgeNotification(request)
                if (response.isSuccessful && response.body()?.status == true) {
                    clearBadgeNotificationResponse.postSuccess()
                } else {
                    clearBadgeNotificationResponse.postError()
                }
            } catch (e: Exception) {
                clearBadgeNotificationResponse.postError()
            }
        }
    }
}