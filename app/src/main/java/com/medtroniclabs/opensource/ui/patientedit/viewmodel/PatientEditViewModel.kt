package com.medtroniclabs.opensource.ui.patientedit.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.postError
import com.medtroniclabs.opensource.appextensions.postLoading
import com.medtroniclabs.opensource.appextensions.postSuccess
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.ui.PatientBasicRequest
import com.medtroniclabs.opensource.di.IoDispatcher
import com.medtroniclabs.opensource.formgeneration.model.FormResponseRootModel
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.screening.repo.ScreeningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientEditViewModel @Inject constructor(
    private val screeningRepository: ScreeningRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    var formResponseLiveData = MutableLiveData<Resource<FormResponseRootModel>>()
    var patientDetailMap = MutableLiveData<Resource<HashMap<String, Any>>>()
    var patientID: Long? = null
    var currentFragment = 1
    var updatePatientMap = MutableLiveData<Resource<HashMap<String, Any>>>()
    var patient_tracker_id:Long? = null
    var fromMedicalReview = false

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    fun fetchWorkFlow(formType: String) {
        viewModelScope.launch(dispatcherIO) {
            try {
                formResponseLiveData.postLoading()
                val response = screeningRepository.getFormBasedOnType(formType, SecuredPreference.getUserId())
                val formModel = Gson().fromJson(response.formString, FormResponseRootModel::class.java)
                formResponseLiveData.postSuccess(formModel)
            } catch (e: Exception) {
                formResponseLiveData.postError(e.message)
            }
        }
    }

    fun getPatientBasicDetail(id: Long) {
        viewModelScope.launch(dispatcherIO) {
            try {
                patientDetailMap.postLoading()
                val response = screeningRepository.getPatientBasicDetail(PatientBasicRequest(id))
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true)
                        patientDetailMap.postSuccess(res.entity)
                    else
                        patientDetailMap.postError()
                } else {
                    patientDetailMap.postError()
                }
            } catch (e: Exception) {
                patientDetailMap.postError()
            }
        }
    }

    fun updatePatientDetail(context: Context,request: HashMap<String, Any>) {
        viewModelScope.launch(dispatcherIO) {
            try {
                if (connectivityManager.isNetworkAvailable()) {
                    updatePatientMap.postLoading()
                    val response = screeningRepository.updatePatient(request)
                    if (response.isSuccessful){
                        val body = response.body()
                        if (body?.status == true) {
                            updatePatientMap.postSuccess(data = body.entity)
                        } else {
                            updatePatientMap.postError()
                        }
                    }else {
                        updatePatientMap.postError()
                    }
                } else {
                    updatePatientMap.postError(context.getString(R.string.no_internet_error))
                }
            } catch (e: Exception) {
                updatePatientMap.postError()
            }
        }
    }

}