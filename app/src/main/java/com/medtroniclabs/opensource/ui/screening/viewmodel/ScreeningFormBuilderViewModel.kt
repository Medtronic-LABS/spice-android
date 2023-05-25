package com.medtroniclabs.opensource.ui.screening.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.opensource.appextensions.postError
import com.medtroniclabs.opensource.appextensions.postLoading
import com.medtroniclabs.opensource.appextensions.postSuccess
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.LocalSpinnerResponse
import com.medtroniclabs.opensource.data.model.RiskClassificationModel
import com.medtroniclabs.opensource.data.screening.SiteDetails
import com.medtroniclabs.opensource.db.tables.ScreeningEntity
import com.medtroniclabs.opensource.db.tables.SiteEntity
import com.medtroniclabs.opensource.di.IoDispatcher
import com.medtroniclabs.opensource.formgeneration.model.FormResponseRootModel
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.boarding.repo.OnBoardingRepository
import com.medtroniclabs.opensource.ui.screening.repo.ScreeningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import java.lang.reflect.Type
import javax.inject.Inject

@HiltViewModel
class ScreeningFormBuilderViewModel @Inject constructor(
    private val screeningRepository: ScreeningRepository,
    private val onBoardingRepository: OnBoardingRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) :
    ViewModel() {

    var formResponseLiveData = MutableLiveData<Resource<FormResponseRootModel>>()
    var formResponseListLiveData = MutableLiveData<Resource<ArrayList<Pair<String,String>>>>()
    var screeningLiveDate = MutableLiveData<Resource<Boolean>>()
    var screeningSaveResponse = MutableLiveData<Resource<Long>>()
    var screeningEntity = MutableLiveData<Resource<ScreeningEntity>>()
    var list = ArrayList<RiskClassificationModel>()
    var siteDetail: SiteDetails? = null
    var screeningEntityRowId: Long? = null
    var foregroundOnlyLocationServiceBound = false
    var accountSiteListLiveData = MutableLiveData<Resource<ArrayList<SiteEntity>>>()
    var mentalHealthQuestions = MutableLiveData<Resource<LocalSpinnerResponse>>()

    fun fetchWorkFlow(formType: String) {
        viewModelScope.launch(dispatcherIO) {
            try {
                formResponseLiveData.postLoading()
                val response =
                    screeningRepository.getFormBasedOnType(formType, SecuredPreference.getUserId())
                val formModel = Gson().fromJson(response.formString, FormResponseRootModel::class.java)
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

    fun savePatientScreeningInformation(
        screeningEntityRawString: String,
        generalDetail: String,
        isReferred: Boolean
    ) {

        viewModelScope.launch(dispatcherIO)
        {
            screeningSaveResponse.postLoading()
            try {
                val screeningEntity = ScreeningEntity(
                    screeningDetails = screeningEntityRawString,
                    generalDetails = generalDetail,
                    userId = SecuredPreference.getUserId(),
                    isReferred = isReferred
                )
                val rowId = screeningRepository.savePatientScreeningInformation(screeningEntity)
                screeningEntityRowId = rowId
                screeningSaveResponse.postSuccess(rowId)
            } catch (e: Exception) {
                screeningSaveResponse.postError()
            }
        }
    }

    fun updatePatientScreeningInformation(id: Long, generalDetail: String) {
        viewModelScope.launch(dispatcherIO) {
            screeningLiveDate.postLoading()
            try {
                screeningRepository.updateGeneralDetailsById(id, generalDetail)
                screeningLiveDate.postValue(Resource(ResourceState.SUCCESS, true))
            } catch (e: Exception) {
                screeningLiveDate.postValue(Resource(ResourceState.SUCCESS, false))
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

    fun getSavedSiteDetail(): String? {
        return try {
            Gson().toJson(siteDetail)
        } catch (e: Exception) {
            null
        }
    }

    fun getScreeningEntity(rowId: Long) {
        viewModelScope.launch(dispatcherIO) {
            try {
                screeningEntity.postLoading()
                val entity = screeningRepository.getScreeningRecordById(rowId)
                screeningEntity.postSuccess(entity)
            } catch (e: Exception) {
                screeningEntity.postError()
            }
        }
    }

    fun fetchAccountSiteList() {
        viewModelScope.launch(dispatcherIO) {
            accountSiteListLiveData.postLoading()
            try {
                val accountSiteList =
                    screeningRepository.getAccountSiteList(SecuredPreference.getUserId())
                val siteList = Resource(ResourceState.SUCCESS, ArrayList(accountSiteList))
                accountSiteListLiveData.postValue(siteList)
            } catch (e: Exception) {
                accountSiteListLiveData.postValue(Resource(ResourceState.ERROR))
            }
        }
    }

    fun fetchMentalHealthQuestions(id: String, type: String) {
        viewModelScope.launch(dispatcherIO) {
            mentalHealthQuestions.postLoading()
            try {
                val questions = onBoardingRepository.getMHQuestionsByType(type = type)
                mentalHealthQuestions.postValue(
                    Resource(
                        ResourceState.SUCCESS,
                        LocalSpinnerResponse(tag = id, response = questions)
                    )
                )
            } catch (e: Exception) {
                mentalHealthQuestions.postValue(Resource(ResourceState.ERROR))
            }
        }
    }

}