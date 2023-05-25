package com.medtroniclabs.opensource.ui.medicalreview.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.opensource.appextensions.postError
import com.medtroniclabs.opensource.appextensions.postLoading
import com.medtroniclabs.opensource.appextensions.postSuccess
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.assesssment.BPLogModel
import com.medtroniclabs.opensource.data.model.RiskClassificationModel
import com.medtroniclabs.opensource.di.IoDispatcher
import com.medtroniclabs.opensource.formgeneration.model.FormResponseRootModel
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.ui.medicalreview.repo.MedicalReviewRepository
import com.medtroniclabs.opensource.ui.screening.repo.ScreeningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.lang.reflect.Type
import javax.inject.Inject

@HiltViewModel
class AssessmentReadingViewModel @Inject constructor(
    private val screeningRepository: ScreeningRepository,
    private val medicalReviewRepo : MedicalReviewRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {

    var formResponseLiveData = MutableLiveData<Resource<FormResponseRootModel>>()
    var list = ArrayList<RiskClassificationModel>()
    val BPLogResponseLiveData = MutableLiveData<Resource<BPLogModel>>()

    fun fetchWorkFlow(formType: String) {
        viewModelScope.launch(dispatcherIO) {
            try {
                formResponseLiveData.postLoading()
                val response =
                    screeningRepository.getFormBasedOnType(formType, SecuredPreference.getUserId())
                val formModel =
                    Gson().fromJson(response.formString, FormResponseRootModel::class.java)
                formResponseLiveData.postSuccess(formModel)
            } catch (e: Exception) {
                formResponseLiveData.postError(e.message)
            }
        }
    }

    fun getRiskEntityList() {
        viewModelScope.launch(dispatcherIO) {
            val resultOne = screeningRepository.riskFactorListing()
            val baseType: Type = object : TypeToken<ArrayList<RiskClassificationModel>>() {}.type
            if (resultOne.isNotEmpty()) {
                val resultList = Gson().fromJson<ArrayList<RiskClassificationModel>>(
                    resultOne[0].nonLabEntity,
                    baseType
                )
                list.clear()
                list.addAll(resultList)
            }
        }
    }

    fun createNewBPLogForPatient(createBPLog: JsonObject) {
        viewModelScope.launch(dispatcherIO) {
            try {
                BPLogResponseLiveData.postLoading()
                val response = medicalReviewRepo.createPatientBPLog(createBPLog)
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true)
                        BPLogResponseLiveData.postSuccess(res.entity)
                    else
                        BPLogResponseLiveData.postError()
                } else {
                    BPLogResponseLiveData.postError()
                }
            } catch (e: Exception) {
                BPLogResponseLiveData.postError()
            }
        }
    }

    fun createNewBloodGlucoseForPatient(createBPLog: JsonObject) {
        viewModelScope.launch(dispatcherIO) {
            try {
                BPLogResponseLiveData.postLoading()
                val response = medicalReviewRepo.createBloodGlucoseBPLog(createBPLog)
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true)
                        BPLogResponseLiveData.postSuccess(res.entity)
                    else
                        BPLogResponseLiveData.postError()
                } else {
                    BPLogResponseLiveData.postError()
                }
            } catch (e: Exception) {
                BPLogResponseLiveData.postError()
            }
        }
    }
}