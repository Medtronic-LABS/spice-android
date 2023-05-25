package com.medtroniclabs.opensource.ui.enrollment.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.postError
import com.medtroniclabs.opensource.appextensions.postLoading
import com.medtroniclabs.opensource.appextensions.postSuccess
import com.medtroniclabs.opensource.common.StringConverter
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.LocalSpinnerResponse
import com.medtroniclabs.opensource.data.model.PatientCreateResponse
import com.medtroniclabs.opensource.data.model.RiskClassificationModel
import com.medtroniclabs.opensource.di.IoDispatcher
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.model.FormResponseRootModel
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.boarding.repo.OnBoardingRepository
import com.medtroniclabs.opensource.ui.screening.repo.ScreeningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.lang.reflect.Type
import javax.inject.Inject

@HiltViewModel
class EnrollmentFormBuilderViewModel @Inject constructor(
    private val screeningRepository: ScreeningRepository,
    private val onBoardingRepo: OnBoardingRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) :
    ViewModel() {

    @Inject
    lateinit var connectivityManager: ConnectivityManager
    var formResponseLiveData = MutableLiveData<Resource<FormResponseRootModel>>()
    var formResponseListLiveData = MutableLiveData<Resource<ArrayList<Pair<String,String>>>>()
    var enrollPatientLiveData = MutableLiveData<Resource<PatientCreateResponse>>()
    var groupedEnrollmentHashMap = HashMap<String, Any>()
    var patient_track_id: Long? = null
    var assessment_required: Boolean = true
    var list = ArrayList<RiskClassificationModel>()
    var localDataCacheResponse = MutableLiveData<Resource<LocalSpinnerResponse>>()
    var mentalHealthQuestions = MutableLiveData<Resource<LocalSpinnerResponse>>()
    var programListResponse = MutableLiveData<Resource<LocalSpinnerResponse>>()
    var patientInitial: String = ""
    var isFromDirectEnrollment = false
    var screeningId:Long ?= null

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

    fun fetchWorkFlow(formTypeOne: String,formTypeTwo: String) {
        viewModelScope.launch(dispatcherIO) {
            try {
                formResponseListLiveData.postLoading()
                val formResponseList = screeningRepository.getFormBasedOnType(formTypeOne,formTypeTwo, SecuredPreference.getUserId())
                val list = ArrayList<Pair<String,String>>()
                formResponseList.forEach {
                    list.add(Pair(it.formType,it.formString))
                }
                formResponseListLiveData.postSuccess(list)
            } catch (e: Exception) {
                formResponseListLiveData.postError(e.message)
            }
        }
    }

    fun getRiskEntityList() {
        viewModelScope.launch (dispatcherIO){
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

    fun enrollPatient(context: Context, request: String) {
        val rootJson: JsonObject? = StringConverter.getJsonObject(request)

        rootJson?.let {
            viewModelScope.launch(dispatcherIO) {
                try {
                    if (connectivityManager.isNetworkAvailable()) {
                        enrollPatientLiveData.postLoading()
                        val response = onBoardingRepo.createPatient(it)
                        if (response.isSuccessful) {
                            if (response.body()?.status == true)
                                enrollPatientLiveData.postSuccess(response.body()?.entity)
                            else
                                enrollPatientLiveData.postError()
                        } else
                            enrollPatientLiveData.postError(StringConverter.getErrorMessage(response.errorBody()))
                    }
                    else
                        enrollPatientLiveData.postError(context.getString(R.string.no_internet_error))
                } catch (e: Exception) {
                    enrollPatientLiveData.postError()
                }
            }
        }
    }

    fun loadDataCacheByType(type: String, tag: String, selectedParent: Long?) {
        viewModelScope.launch(dispatcherIO) {
            try {
                when (type) {
                    DefinedParams.country -> {
                        localDataCacheResponse.postLoading()
                        val response = onBoardingRepo.getCountryList()
                        localDataCacheResponse.postValue(
                            Resource(
                                ResourceState.SUCCESS,
                                LocalSpinnerResponse(tag, response)
                            )
                        )
                    }
                    DefinedParams.subCounty -> {
                        localDataCacheResponse.postLoading()
                        val response = onBoardingRepo.getSubCountyList(selectedParent)
                        localDataCacheResponse.postValue(
                            Resource(
                                ResourceState.SUCCESS,
                                LocalSpinnerResponse(tag, response)
                            )
                        )
                    }
                    DefinedParams.county -> {
                        localDataCacheResponse.postLoading()
                        val response = onBoardingRepo.getCountyList(selectedParent)
                        localDataCacheResponse.postValue(
                            Resource(
                                ResourceState.SUCCESS,
                                LocalSpinnerResponse(tag, response)
                            )
                        )
                    }
                    DefinedParams.program -> {
                        programListResponse.postLoading()
                        val response = onBoardingRepo.getProgramList(selectedParent)
                        programListResponse.postValue(
                            Resource(
                                ResourceState.SUCCESS,
                                LocalSpinnerResponse(DefinedParams.program, response)
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                programListResponse.postError()
            }
        }
    }

    fun fetchMentalHealthQuestions(id: String, type: String)
    {
        viewModelScope.launch (dispatcherIO) {
            mentalHealthQuestions.postLoading()
            try {
                val questions = onBoardingRepo.getMHQuestionsByType(type = type)
                mentalHealthQuestions.postValue(Resource(ResourceState.SUCCESS, LocalSpinnerResponse(tag = id, response = questions)))
            }
            catch (e: Exception)
            {
                mentalHealthQuestions.postValue(Resource(ResourceState.ERROR))
            }
        }
    }

}