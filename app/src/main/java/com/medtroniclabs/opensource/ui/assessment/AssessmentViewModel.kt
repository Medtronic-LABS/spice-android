package com.medtroniclabs.opensource.ui.assessment

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
import com.medtroniclabs.opensource.data.assesssment.AssessmentPatientResponse
import com.medtroniclabs.opensource.data.model.LocalSpinnerResponse
import com.medtroniclabs.opensource.data.model.PatientDetailsModel
import com.medtroniclabs.opensource.data.model.RiskClassificationModel
import com.medtroniclabs.opensource.data.ui.SymptomModel
import com.medtroniclabs.opensource.db.tables.MedicalComplianceEntity
import com.medtroniclabs.opensource.db.tables.SymptomEntity
import com.medtroniclabs.opensource.di.IoDispatcher
import com.medtroniclabs.opensource.formgeneration.model.FormResponseRootModel
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.assessment.repo.AssessmentRepository
import com.medtroniclabs.opensource.ui.boarding.repo.OnBoardingRepository
import com.medtroniclabs.opensource.ui.screening.repo.ScreeningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.lang.reflect.Type
import javax.inject.Inject

@HiltViewModel
class AssessmentViewModel @Inject constructor(
    private val screeningRepository: ScreeningRepository,
    private val onBoardingRepo: OnBoardingRepository,
    private val assessmentRepository: AssessmentRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel() {
    var list = ArrayList<RiskClassificationModel>()
    var patientIdDetails = MutableLiveData<Resource<PatientDetailsModel>>()
    var bioDataMap: HashMap<String, Any>? = null
    val assessmentResponse = MutableLiveData<Resource<AssessmentPatientResponse>>()
    var mentalHealthQuestions = MutableLiveData<Resource<LocalSpinnerResponse>>()
    var formResponseLiveData = MutableLiveData<Resource<FormResponseRootModel>>()
    var medicationParentComplianceResponse = MutableLiveData<List<MedicalComplianceEntity>>()
    var medicationChildComplianceResponse = MutableLiveData<List<MedicalComplianceEntity>>()
    var symptomListResponse = MutableLiveData<List<SymptomEntity>>()
    var complianceMap: ArrayList<HashMap<String, Any>>? = null
    var selectedSymptoms = MutableLiveData<List<SymptomModel>>()
    var selectedMedication = MutableLiveData<MedicalComplianceEntity?>()
    var formResponseListLiveData = MutableLiveData<Resource<ArrayList<Pair<String,String>>>>()


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
        viewModelScope.launch (dispatcherIO) {
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

    fun createPatientAssessment(createRequest: JsonObject) {
        viewModelScope.launch(dispatcherIO) {
            try {
                assessmentResponse.postLoading()
                val response = assessmentRepository.createPatientAssessment(createRequest)
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true)
                        assessmentResponse.postSuccess(res.entity)
                    else
                        assessmentResponse.postError()
                } else
                    assessmentResponse.postError()
            } catch (e: Exception) {
                assessmentResponse.postError()
            }
        }
    }

    fun getMedicationParentComplianceList() {
        viewModelScope.launch(dispatcherIO) {
            try {
                medicationParentComplianceResponse.postValue(onBoardingRepo.getMedicationParentComplianceList())
            } catch (_: Exception) {
                //Exception - Catch block
            }
        }
    }

    fun getSymptomList() {
        viewModelScope.launch(dispatcherIO) {
            try {
                symptomListResponse.postValue(onBoardingRepo.getSymptomList())
            } catch (_: Exception) {
                //Exception - Catch block
            }
        }
    }

    fun getMedicationChildComplianceList(parentId: Long) {
        viewModelScope.launch(dispatcherIO) {
            try {
                medicationChildComplianceResponse.postValue(
                    onBoardingRepo.getMedicationChildComplianceList(
                        parentId
                    )
                )
            } catch (_: Exception) {
                //Exception - Catch block
            }
        }
    }


    fun fetchMentalHealthQuestions(id: String, type: String) {
        viewModelScope.launch(dispatcherIO) {
            mentalHealthQuestions.postLoading()
            try {
                val questions = onBoardingRepo.getMHQuestionsByType(type = type)
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
