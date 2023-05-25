package com.medtroniclabs.opensource.ui.medicalreview.nutritionist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.opensource.appextensions.postError
import com.medtroniclabs.opensource.appextensions.postLoading
import com.medtroniclabs.opensource.appextensions.postSuccess
import com.medtroniclabs.opensource.data.model.LifeStyleManagement
import com.medtroniclabs.opensource.data.model.PatientLifestyleModel
import com.medtroniclabs.opensource.di.IoDispatcher
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.medicalreview.repo.MedicalReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NutritionistViewModel @Inject constructor(
    private val medicalReviewRepo: MedicalReviewRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    var patient_track_id: Long? = null
    var patient_visit_id: Long? = null
    var tenant_id: Long? = null

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    val patientLifestyleList = MutableLiveData<Resource<ArrayList<LifeStyleManagement>>>()
    val updatePatientLifestyle = MutableLiveData<Resource<LifeStyleManagement>>()
    val historyLifestyleList = MutableLiveData<Resource<ArrayList<LifeStyleManagement>>>()

    fun getPatientLifestyleList(request: PatientLifestyleModel) {
        viewModelScope.launch(dispatcherIO) {
            try {
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

    fun updatePatientLifestyle(request: PatientLifestyleModel) {
        viewModelScope.launch(dispatcherIO) {
            try {
                updatePatientLifestyle.postLoading()
                val response = medicalReviewRepo.updatePatientLifestyle(request)
                if (response.isSuccessful) {
                    if (response.body()?.status == true)
                        updatePatientLifestyle.postSuccess()
                    else
                        updatePatientLifestyle.postError()
                } else {
                    updatePatientLifestyle.postError()
                }
            } catch (e: Exception) {
                updatePatientLifestyle.postError()
            }
        }
    }

    fun getHistoryLifestyleList(request: PatientLifestyleModel) {
        viewModelScope.launch(dispatcherIO) {
            try {
                historyLifestyleList.postLoading()
                val response = medicalReviewRepo.getPatientLifestyleList(request)
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true)
                        historyLifestyleList.postSuccess(res.entityList ?: ArrayList())
                    else
                        historyLifestyleList.postError()
                } else {
                    historyLifestyleList.postError()
                }
            } catch (e: Exception) {
                historyLifestyleList.postError()
            }
        }
    }
}