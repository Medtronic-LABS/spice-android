package com.medtroniclabs.opensource.ui.medicalreview.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.postError
import com.medtroniclabs.opensource.appextensions.postLoading
import com.medtroniclabs.opensource.appextensions.postSuccess
import com.medtroniclabs.opensource.common.StringConverter
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.*
import com.medtroniclabs.opensource.di.IoDispatcher
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.medicalreview.repo.MedicalReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LabTestViewModel @Inject constructor(
    private val medicalReviewRepo: MedicalReviewRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
    ) :
    ViewModel() {
    var editModel: LabTestModel? = null
    val labTestResultResponse = MutableLiveData<Resource<ArrayList<HashMap<String, Any>>>>()
    val searchResponse = MutableLiveData<Resource<ArrayList<LabTestSearchResponse>?>>()
    val labTestListResponse = MutableLiveData<Resource<LabTestListResponse>>()
    val referLabTestResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    val createResultResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    val resultDetailsResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    val removeLabTestResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    val reviewResultResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    var selectedLabTest: LabTestSearchResponse? = null
    var patientTrackId: Long = -1L
    var patientVisitId: Long = -1L
    val isToRefer = MutableLiveData(false)
    var labTestLists = ArrayList<LabTestModel>()
    var labTestUnitList = ArrayList<UnitMetricEntity>()

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    init {
        getLabTestUnitList()
    }

    fun searchLabTest(searchValue: String) {
        viewModelScope.launch(dispatcherIO) {
            searchResponse.postLoading()
            try {
                val request = SearchModel(searchValue = searchValue, country = SecuredPreference.getCountryID(), isActive = true)
                val response = medicalReviewRepo.searchLabTest(request)
                if (response.isSuccessful) {
                    val res = response.body()
                    if(res?.status == true)
                        searchResponse.postSuccess(data = res.entityList)
                    else
                        searchResponse.postError()
                } else
                    searchResponse.postError()
            } catch (e: Exception) {
                searchResponse.postError()
            }
        }
    }

    fun getLabTestResults(context: Context,labTestId: Long, labTestName: String?) {

        viewModelScope.launch(dispatcherIO) {
            if (connectivityManager.isNetworkAvailable()) {
                labTestResultResponse.postLoading()
                try {
                    val response = medicalReviewRepo.getLabTestResult(labTestId)
                    if (response.isSuccessful)
                    {
                        val res = response.body()
                        if(res?.status == true)
                            handleResponseList(res.entityList, labTestName)
                        else
                            labTestResultResponse.postError()
                    }
                    else
                        labTestResultResponse.postError()
                } catch (e: Exception) {
                    labTestResultResponse.postError()
                }
            } else
                labTestResultResponse.postError(context.getString(R.string.no_internet_error))
        }
    }

    private fun handleResponseList(list: ArrayList<HashMap<String, Any>>?, labTestName: String?) {
        val resultList = ArrayList<HashMap<String, Any>>()
        if (list.isNullOrEmpty()) {
            val resultMap = HashMap<String, Any>()
            resultMap[DefinedParams.NAME] = labTestName ?: ""
            resultMap[DefinedParams.Display_Order] = 1
            resultList.add(resultMap)
            editModel?.patientLabtestResults = resultList
            labTestResultResponse.postSuccess(resultList)
        } else {
            list.sortBy { if (it.containsKey(DefinedParams.Display_Order)) (it[DefinedParams.Display_Order] as Number).toInt() else null }
            editModel?.patientLabtestResults = list
            labTestResultResponse.postSuccess(list)
        }
    }

    fun getLabTestList(isFromTechnicianLogin: Boolean = false) {
        val request = HashMap<String, Any?>()
        request[DefinedParams.PatientTrackId] = patientTrackId
        SecuredPreference.getSelectedSiteEntity()?.let {
            request[DefinedParams.TenantId] = it.tenantId
            if (isFromTechnicianLogin) {
                request[DefinedParams.RoleName] = it.role
                request[DefinedParams.is_latest_required] = false
            }
        }
        viewModelScope.launch(dispatcherIO) {
            labTestListResponse.postLoading()
            try {
                val response = medicalReviewRepo.getPatientLabTests(request)
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true)
                        labTestListResponse.postSuccess(res.entity)
                    else
                        labTestListResponse.postError()
                } else
                    labTestListResponse.postError()
            } catch (e: Exception) {
                labTestListResponse.postError()
            }
        }
    }

    fun referLabTest(context: Context, labTestList: ArrayList<LabTestModel>) {
        val request = HashMap<String, Any>()
        request[DefinedParams.Lab_Test] = labTestList
        request[DefinedParams.PatientTrackId] = patientTrackId
        request[DefinedParams.Patient_Visit_Id] = patientVisitId
        SecuredPreference.getSelectedSiteEntity()?.tenantId?.let {
            request[DefinedParams.TenantId] = it
        }
        viewModelScope.launch(dispatcherIO) {
            if (connectivityManager.isNetworkAvailable()) {
                labTestListResponse.postLoading()
                try {
                    val response = medicalReviewRepo.referLabTest(request)
                    if (response.isSuccessful) {
                        val res = response.body()
                        if (res?.status == true) {
                            val resultMap = HashMap<String, Any>()
                            res.message?.let { successMessage ->
                                resultMap[DefinedParams.message] = successMessage
                            }
                            referLabTestResponse.postSuccess(resultMap)
                        } else
                            referLabTestResponse.postError()
                    } else
                        referLabTestResponse.postError(StringConverter.getErrorMessage(response.errorBody()))
                } catch (e: Exception) {
                    referLabTestResponse.postError()
                }
            } else {
                referLabTestResponse.postError(context.getString(R.string.no_internet_error))
            }
        }
    }

    fun createLabTestResult(context: Context, request: HashMap<String, Any>) {

        viewModelScope.launch(dispatcherIO) {
            if (connectivityManager.isNetworkAvailable()) {
                createResultResponse.postLoading()
                try {
                    val response = medicalReviewRepo.createLabTestResult(request)
                    if (response.isSuccessful) {
                        val res = response.body()
                        if(res?.status == true)
                            createResultResponse.postSuccess(res.entity)
                        else
                            createResultResponse.postError()
                    } else
                        createResultResponse.postError()
                } catch (e: Exception) {
                    createResultResponse.postError()
                }
            } else {
                createResultResponse.postError(context.getString(R.string.no_internet_error))
            }
        }
    }

    fun getResultDetails(labTestId: Long) {
        val request = HashMap<String, Any>()
        SecuredPreference.getSelectedSiteEntity()?.tenantId?.let {
            request[DefinedParams.TenantId] = it
        }
        request[DefinedParams.Patient_LabTest_Id] = labTestId
        viewModelScope.launch(dispatcherIO) {
            resultDetailsResponse.postLoading()
            try {
                val response = medicalReviewRepo.getLabTestResultDetails(request)
                if (response.isSuccessful) {
                    val res = response.body()
                    if(res?.status == true)
                        resultDetailsResponse.postSuccess(res.entity)
                    else
                        resultDetailsResponse.postError()
                } else
                    resultDetailsResponse.postError()
            } catch (e: Exception) {
                resultDetailsResponse.postError()
            }
        }
    }

    private fun getLabTestUnitList() {
        viewModelScope.launch(dispatcherIO) {
            try {
                val list = medicalReviewRepo.getUnitList(DefinedParams.LABTEST)
                list.let {
                    if(it.isNotEmpty())
                        labTestUnitList.addAll(it)
                }
            } catch (_: Exception) {
                //Exception - Catch block
            }
        }
    }

    fun removeLabTest(context: Context,request: HashMap<String, Any>, model: LabTestModel) {

        viewModelScope.launch(dispatcherIO) {
            if (connectivityManager.isNetworkAvailable()) {
                removeLabTestResponse.postLoading()
                try {
                    val response = medicalReviewRepo.removeLabTest(request)
                    if (response.isSuccessful) {
                        val res = response.body()
                        if(res?.status == true)
                        {
                            val entity = res.entity ?: HashMap()
                            entity[DefinedParams.Other] = model
                            removeLabTestResponse.postSuccess(entity)
                        }
                        else
                            removeLabTestResponse.postError()
                    } else
                        removeLabTestResponse.postError()
                } catch (e: Exception) {
                    removeLabTestResponse.postError()
                }
            } else {
                removeLabTestResponse.postError(context.getString(R.string.no_internet_error))
            }
        }
    }

    fun reviewLabTestResult(context: Context,request: HashMap<String, Any>) {

        viewModelScope.launch(dispatcherIO) {
            if (connectivityManager.isNetworkAvailable()) {
                reviewResultResponse.postLoading()
                try {
                    val response = medicalReviewRepo.reviewLabTestResult(request)
                    if (response.isSuccessful) {
                        val res = response.body()
                        if(res?.status == true)
                            reviewResultResponse.postSuccess(res.entity)
                        else
                            reviewResultResponse.postError()
                    } else
                        reviewResultResponse.postError()
                } catch (e: Exception) {
                    reviewResultResponse.postError()
                }
            } else {
                reviewResultResponse.postError(context.getString(R.string.no_internet_error))
            }
        }
    }
}