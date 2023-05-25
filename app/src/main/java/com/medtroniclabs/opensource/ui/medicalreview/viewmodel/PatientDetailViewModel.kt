package com.medtroniclabs.opensource.ui.medicalreview.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.postError
import com.medtroniclabs.opensource.appextensions.postLoading
import com.medtroniclabs.opensource.appextensions.postSuccess
import com.medtroniclabs.opensource.appextensions.setError
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.*
import com.medtroniclabs.opensource.db.tables.DiagnosisEntity
import com.medtroniclabs.opensource.di.IoDispatcher
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.formgeneration.model.FormLayout
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.medicalreview.repo.MedicalReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import retrofit2.Response
import java.lang.reflect.Type
import javax.inject.Inject

@HiltViewModel
class PatientDetailViewModel @Inject constructor(
    private val medicalReviewRepo: MedicalReviewRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
    ) :
    ViewModel() {

    val patientDetailsResponse = MutableLiveData<Resource<PatientDetailsModel>>()
    val assessmentPatientDetails = MutableLiveData<Resource<PatientDetailsModel>>()
    val patientBPLogListResponse = MutableLiveData<Resource<BPLogListResponse>>()
    val patientBPLogGraphListResponse = MutableLiveData<Resource<BPLogListResponse>>()
    val patientBloodGlucoseListResponse = MutableLiveData<Resource<BloodGlucoseListResponse>>()
    var patientId: Long? = null
    var patientVisitId: Long? = null
    var origin: String? = null
    var patientPregnancyId: Long? = null
    val searchResponse = MutableLiveData<Resource<ArrayList<LabTestSearchResponse>?>>()
    var selectedLabTest: LabTestSearchResponse? = null
    val medicationSearchResponse = MutableLiveData<Resource<ArrayList<PrescriptionModel>>>()
    var selectedMedication: PrescriptionModel? = null
    val medicalReviewSummaryResponse = MutableLiveData<Resource<SummaryResponse>>()
    val pregnancyDetails = MutableLiveData<String>()
    var allowDismiss: Boolean = true
    var patientTrackId: Long = -1
    var mentalHealthQuestions = MutableLiveData<Resource<LocalSpinnerResponse>>()
    var mentalHealthCreateResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    var pregnancyCreateResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    var patientPregnancyDetailResponse = MutableLiveData<Resource<PregnancyCreateRequest>>()
    val confirmDiagnosisRequestData = ConfirmDiagnosesRequest()
    val confirmDiagnosisRequest = MutableLiveData<Resource<HashMap<String, Any>>>()
    var topCardShow = true
    var showMentalHealthCard = false
    var isFromCMR = false
    var isMentalHealthUpdated = MutableLiveData<Boolean>()
    var mentalHealthDetails = MutableLiveData<Resource<HashMap<String, Any>>>()
    val treatmentPlanDetailsResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    val updateTreatmentPlanResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    val refreshMedicalReview = MutableLiveData<Resource<Boolean>>()
    val treatmentPlanData = MutableLiveData<Resource<HashMap<String, Any>>>()
    var treatmentPlanResultMap = HashMap<String, Any>()
    val screeningDetailResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    var totalBGCount: Int = 0
    var totalBPCount: Int = 0
    var totalBPTotalCount: Int? = null
    var latestBpLogResponse: BPResponse ? = null
    var selectedBGDropDown = MutableLiveData<Int>()
    var screening_id: Long? = null
    var patientTrackerID:String? = null
    var patientDetails: PatientDetailsModel? = null
    val createBPvitalResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    val createBloodGlucoseResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    var height: FormLayout? = null
    var weight: FormLayout? = null
    var bpLog: FormLayout? = null
    var bloodGlucose: FormLayout? = null
    var hbA1c: FormLayout? = null
    val bgResultHashMap = HashMap<String, Any>()
    val resultHashMap = HashMap<String, Any>()
    var isSummaryDetailsLoaded = false
    val refreshGraphDetails = MutableLiveData<Resource<Boolean>>()
    val patientRemoveResponse = MutableLiveData<Resource<HashMap<String,Any>>>()
    var diagnosisList = ArrayList<DiagnosisEntity>()

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    fun getPatientDetails(
        context: Context,
        isAssessmentDataRequired: Boolean,
        isPrescriberRequired: Boolean = false,
        isLifestyleRequired: Boolean = false,
        showLoader: Boolean = true
    ) {
        patientId?.let {
            val request =
                PatientDetailsModel(
                    it,
                    isAssessmentDataRequired = isAssessmentDataRequired,
                    isPrescriberRequired = isPrescriberRequired,
                    isLifestyleRequired = isLifestyleRequired
                )
            if (connectivityManager.isNetworkAvailable())
                fetchPatientDetails(request, showLoader)
            else if (showLoader)
                patientDetailsResponse.setError(context.getString(R.string.no_internet_error))
            ""
        }
    }

    private fun fetchPatientDetails(request: PatientDetailsModel, showLoader: Boolean) {
        viewModelScope.launch(dispatcherIO) {
            if (showLoader)
                patientDetailsResponse.postLoading()
            try {
                val response = medicalReviewRepo.getPatientDetails(request)
                if (response.isSuccessful) {
                    val entity = response.body()?.entity
                    if (entity == null)
                        patientDetailsResponse.postError()
                    else {
                        screening_id = entity.screeningLogId
                        patientDetailsResponse.postSuccess(data = entity)
                    }
                } else if (showLoader)
                    patientDetailsResponse.postError()
            } catch (e: Exception) {
                if (showLoader)
                    patientDetailsResponse.postError()
            }
        }
    }

    fun getPatientBPLogList(context:Context, request: AssessmentListRequest) {
        if (connectivityManager.isNetworkAvailable()) {
            viewModelScope.launch(dispatcherIO) {
                patientBPLogListResponse.postLoading()
                try {
                    val response = medicalReviewRepo.getPatientBPLogList(request)
                    if (response.isSuccessful) {
                        val res = response.body()
                        if (res?.status == true)
                            patientBPLogListResponse.postSuccess(res.entity)
                        else
                            patientBPLogListResponse.postError()
                    } else
                        patientBPLogListResponse.postError()
                } catch (e: Exception) {
                    patientBPLogListResponse.postError()
                }
            }
        } else {
            patientBPLogListResponse.setError(context.getString(R.string.no_internet_error))
        }
    }
    fun getPatientBPLogListForGraph(context: Context, request: AssessmentListRequest, forward: Boolean? = null) {
        if (connectivityManager.isNetworkAvailable()) {
            viewModelScope.launch(dispatcherIO) {
                patientBPLogGraphListResponse.postLoading()
                try {
                    val response = medicalReviewRepo.getPatientBPLogList(request)
                    if (response.isSuccessful) {
                        val res = response.body()
                        if (res?.status == true)
                            patientBPLogGraphListResponse.postSuccess(res.entity, forward)
                        else
                            patientBPLogGraphListResponse.postError()
                    } else
                        patientBPLogGraphListResponse.postError()
                } catch (e: Exception) {
                    patientBPLogGraphListResponse.postError()
                }
            }
        } else {
            patientBPLogGraphListResponse.setError(context.getString(R.string.no_internet_error))
        }
    }

    fun getPatientBloodGlucoseList(
        context: Context,
        request: AssessmentListRequest
    ) {
        if (connectivityManager.isNetworkAvailable()) {
            viewModelScope.launch(dispatcherIO) {
                try {
                    patientBloodGlucoseListResponse.postLoading()
                    val response = medicalReviewRepo.getPatientBloodGlucoseList(request)
                    if (response.isSuccessful) {
                        val res = response.body()
                        if (res?.status == true)
                            patientBloodGlucoseListResponse.postSuccess(res.entity)
                        else
                            patientBloodGlucoseListResponse.postError()
                    } else
                        patientBloodGlucoseListResponse.postError()
                } catch (e: Exception) {
                    patientBloodGlucoseListResponse.postError()
                }
            }
        } else {
            patientBloodGlucoseListResponse.setError(context.getString(R.string.no_internet_error))
        }
    }

    fun fetchMentalHealthQuestions(id: String, type: String) {

        viewModelScope.launch(dispatcherIO) {
            mentalHealthQuestions.postLoading()
            try {
                val questions = medicalReviewRepo.getMHQuestionsByType(type = type)
                mentalHealthQuestions.postSuccess(
                    LocalSpinnerResponse(
                        tag = id,
                        response = questions
                    )
                )
            } catch (e: Exception) {
                mentalHealthQuestions.postError()
            }
        }
    }

    fun createOrUpdateMentalHealth(context: Context,request: HashMap<String, Any>, isUpdate: Boolean) {
        if (connectivityManager.isNetworkAvailable()) {
            viewModelScope.launch(dispatcherIO) {
                mentalHealthCreateResponse.postLoading()
                try {
                    val response: Response<APIResponse<HashMap<String, Any>>> = if (isUpdate)
                        medicalReviewRepo.updateMentalHealth(request)
                    else
                        medicalReviewRepo.createMentalHealth(request)
                    if (response.isSuccessful) {
                        val res = response.body()
                        if (res?.status == true) {
                            isMentalHealthUpdated.postValue(true)
                            mentalHealthCreateResponse.postSuccess(res.entity)
                        } else
                            mentalHealthCreateResponse.postError()
                    } else
                        mentalHealthCreateResponse.postError()
                } catch (e: Exception) {
                    mentalHealthCreateResponse.postError()
                }
            }
        } else {
            mentalHealthCreateResponse.setError(context.getString(R.string.no_internet_error))
        }
    }

    fun searchMedication(context: Context, requestModel: MedicationSearchReqModel) {
        if (connectivityManager.isNetworkAvailable()) {
            viewModelScope.launch(dispatcherIO) {
                medicationSearchResponse.postLoading()
                try {
                    val response = medicalReviewRepo.searchMedication(requestModel)
                    if (response.isSuccessful) {
                        val res = response.body()
                        if (res?.status == true)
                            medicationSearchResponse.postSuccess(res.entityList ?: ArrayList())
                        else
                            medicationSearchResponse.postError()
                    } else
                        medicationSearchResponse.postError()
                } catch (e: Exception) {
                    medicationSearchResponse.postError()
                }
            }
        } else {
            medicationSearchResponse.setError(context.getString(R.string.no_internet_error))
        }
    }


    fun createPregnancy(context: Context,request: HashMap<String, Any>, isFromUpdate: Boolean) {
        if (connectivityManager.isNetworkAvailable()) {
            viewModelScope.launch(dispatcherIO) {
                pregnancyCreateResponse.postLoading()
                try {
                    val gson = Gson()
                    val json = gson.toJson(request)
                    val type: Type = object : TypeToken<PregnancyCreateRequest>() {}.type
                    val pregnancyCreateRequest = gson.fromJson<PregnancyCreateRequest>(json, type)
                    if (!isFromUpdate)
                        pregnancyCreate(pregnancyCreateRequest)
                    else
                        pregnancyUpdate(pregnancyCreateRequest)
                } catch (e: Exception) {
                    pregnancyCreateResponse.postError()
                }
            }
        } else
            pregnancyCreateResponse.setError(context.getString(R.string.no_internet_error))
    }

    private suspend fun pregnancyUpdate(pregnancyCreateRequest: PregnancyCreateRequest) {
        val response = medicalReviewRepo.updatePregnancy(pregnancyCreateRequest)
        if (response.isSuccessful) {
            val res = response.body()
            if (res?.status == true)
                pregnancyCreateResponse.postSuccess(res.entity)
            else
                pregnancyCreateResponse.postError()
        } else
            pregnancyCreateResponse.postError()
    }

    private suspend fun pregnancyCreate(pregnancyCreateRequest: PregnancyCreateRequest) {
        val response = medicalReviewRepo.createPregnancy(pregnancyCreateRequest)
        if (response.isSuccessful) {
            val res = response.body()
            if (res?.status == true)
                pregnancyCreateResponse.postSuccess(res.entity)
            else
                pregnancyCreateResponse.postError()
        } else
            pregnancyCreateResponse.postError()
    }

    fun getPatientPregnancyDetails(context: Context,request: PatientPregnancyModel) {
        if (connectivityManager.isNetworkAvailable()) {
            viewModelScope.launch(dispatcherIO) {
                patientPregnancyDetailResponse.postLoading()
                try {
                    val response = medicalReviewRepo.getPatientPregnancyDetails(request)
                    if (response.isSuccessful) {
                        val res = response.body()
                        if (res?.status == true)
                            patientPregnancyDetailResponse.postSuccess(res.entity)
                        else
                            patientPregnancyDetailResponse.postError()
                    } else
                        patientPregnancyDetailResponse.postError()
                } catch (e: Exception) {
                    patientPregnancyDetailResponse.postError()
                }
            }
        } else {
            patientPregnancyDetailResponse.setError(context.getString(R.string.no_internet_error))
        }
    }

    fun confirmDiagnosis(context: Context,request: ConfirmDiagnosesRequest) {
        try {
            viewModelScope.launch(dispatcherIO) {
                try {
                    if (connectivityManager.isNetworkAvailable()) {
                        confirmDiagnosisRequest.postLoading()
                        val response = medicalReviewRepo.confirmDiagnosis(request)
                        if (response.isSuccessful) {
                            val res = response.body()
                            if (res?.status == true)
                                confirmDiagnosisRequest.postSuccess(res.entity)
                            else
                                confirmDiagnosisRequest.postError()
                        } else {
                            confirmDiagnosisRequest.postError()
                        }
                    } else {
                        confirmDiagnosisRequest.postError(context.getString(R.string.no_internet_error))
                    }
                } catch (e: Exception) {
                    confirmDiagnosisRequest.postError()
                }
            }
        } catch (e: Exception) {
            confirmDiagnosisRequest.postError()
        }
    }

    fun getMentalHealthDetails(context: Context,request: HashMap<String, Any>) {
        if (connectivityManager.isNetworkAvailable()) {
            viewModelScope.launch(dispatcherIO) {
                mentalHealthDetails.postLoading()
                try {
                    val response = medicalReviewRepo.getMentalHealthDetails(request)
                    if (response.isSuccessful) {
                        val res = response.body()
                        if (res?.status == true)
                            mentalHealthDetails.postSuccess(res.entity)
                        else
                            mentalHealthDetails.postError()
                    } else {
                        mentalHealthDetails.postError()
                    }
                } catch (e: Exception) {
                    mentalHealthDetails.postError()
                }
            }
        } else {
            mentalHealthDetails.setError(context.getString(R.string.no_internet_error))
        }
    }

    fun getPatientMedicalReviewSummary(
        context: Context,
        request: MedicalReviewBaseRequest,
        refreshPatientDetails: Boolean = false
    ) {
        if (connectivityManager.isNetworkAvailable()) {
            viewModelScope.launch(dispatcherIO) {
                try {
                    medicalReviewSummaryResponse.postLoading()
                    val response = medicalReviewRepo.getPatientMedicalReviewSummary(request)
                    if (response.isSuccessful) {
                        val res = response.body()?.entity
                        res?.refreshPatientDetails = refreshPatientDetails
                        medicalReviewSummaryResponse.postSuccess(res)
                    } else {
                        medicalReviewSummaryResponse.postError()
                    }
                } catch (e: Exception) {
                    medicalReviewSummaryResponse.postError()
                }
            }
        } else {
            medicalReviewSummaryResponse.setError(context.getString(R.string.no_internet_error))
        }
    }

    fun treatmentPlanDetails(context: Context) {
        try {
            val request = HashMap<String, Any>()
            SecuredPreference.getSelectedSiteEntity()?.tenantId?.let {
                request[DefinedParams.TenantId] = it
            }
            request[DefinedParams.PatientTrackId] = patientTrackId

            if (connectivityManager.isNetworkAvailable()) {
                viewModelScope.launch(dispatcherIO) {
                    try {
                        treatmentPlanDetailsResponse.postLoading()
                        val response = medicalReviewRepo.treatmentPlanDetails(request)
                        if (response.isSuccessful) {
                            val res = response.body()
                            if (res?.status == true)
                                treatmentPlanDetailsResponse.postSuccess(res.entity)
                            else
                                treatmentPlanDetailsResponse.postError()
                        } else
                            treatmentPlanDetailsResponse.postError()
                    } catch (e: Exception) {
                        treatmentPlanDetailsResponse.postError()
                    }
                }
            } else {
                treatmentPlanDetailsResponse.setError(context.getString(R.string.no_internet_error))
            }
        } catch (e: Exception) {
            treatmentPlanDetailsResponse.postError()
        }
    }

    fun updateTreatmentPlan(context: Context) {
        try {
            if (connectivityManager.isNetworkAvailable()) {
                viewModelScope.launch(dispatcherIO) {
                    updateTreatmentPlanResponse.postLoading()
                    try {
                        val response = medicalReviewRepo.updateTreatmentPlan(treatmentPlanResultMap)
                        if (response.isSuccessful) {
                            val res = response.body()
                            if (res?.status == true) {
                                val resultMap = HashMap<String, Any>()
                                res.message?.let { successMessage ->
                                    resultMap[DefinedParams.message] = successMessage
                                }
                                updateTreatmentPlanResponse.postSuccess(resultMap)
                            }
                            else
                                updateTreatmentPlanResponse.postError()
                        } else
                            updateTreatmentPlanResponse.postError()
                    } catch (e: Exception) {
                        updateTreatmentPlanResponse.postError()
                    }
                }
            } else {
                updateTreatmentPlanResponse.setError(context.getString(R.string.no_internet_error))
            }
        } catch (e: Exception) {
            updateTreatmentPlanResponse.postError()
        }
    }

    fun canShowDiagnosisAlert(
        data: InitialDiagnosis?,
        confirmedList: ArrayList<String>?
    ): Pair<Int, Boolean> {
        val confirmedDiagnosisList = if(confirmedList.isNullOrEmpty()) ArrayList<String>() else ArrayList(confirmedList)
        var statusCode = -1
        var showDiagnosis = false
        data?.apply {
            if (!diabetesPatientType.equals(
                    DefinedParams.KNOWN,
                    ignoreCase = true
                ) && !htnPatientType.equals(DefinedParams.KNOWN, ignoreCase = true)
            ) {
                return Pair(statusCode, false)
            }

            if(confirmedDiagnosisList.isEmpty())
                return Pair(statusCode, true)

            if (diabetesPatientType == DefinedParams.KNOWN) {
                if (diabetesDiagControlledType == DefinedParams.PreDiabetic) {
                    statusCode = updateListItem(confirmedDiagnosisList, DefinedParams.PreDiabetic)
                    showDiagnosis = confirmedDiagnosisList.contains(DefinedParams.PreDiabetic) == false
                } else {
                    when (diabetesDiagnosis) {
                        DefinedParams.typeOne -> {
                            statusCode = updateListItem(confirmedDiagnosisList, DefinedParams.typeOne)
                            showDiagnosis = confirmedDiagnosisList.contains(DefinedParams.dmtOne) == false
                        }
                        DefinedParams.typeTwo -> {
                            statusCode = updateListItem(confirmedDiagnosisList, DefinedParams.typeTwo)

                            showDiagnosis =
                                confirmedDiagnosisList.contains(DefinedParams.dmtTwo) == false
                        }
                        DefinedParams.GestationalDiabetes -> {
                            statusCode = updateListItem(confirmedDiagnosisList, DefinedParams.GestationalDiabetes)
                            showDiagnosis =
                                confirmedDiagnosisList.contains(DefinedParams.GestationalDiabetes) == false
                        }
                    }
                }
            }
            if (!showDiagnosis && htnPatientType == DefinedParams.KNOWN) {
                showDiagnosis =
                    (confirmedDiagnosisList.contains(DefinedParams.Pre_Hypertension) || confirmedDiagnosisList.contains(DefinedParams.Hypertension)) == false
                if (showDiagnosis)
                    statusCode = 1
            }
        }

        return Pair(statusCode, showDiagnosis)
    }

    private fun updateListItem(confirmedDiagnosisList: ArrayList<String>, type: String): Int {
        when(type)
        {
            DefinedParams.PreDiabetic -> {
                if (confirmedDiagnosisList.contains(DefinedParams.dmtOne))
                {
                    confirmedDiagnosisList.remove(DefinedParams.dmtOne)
                    return 1
                }
                else if (confirmedDiagnosisList.contains(DefinedParams.dmtTwo))
                {
                    confirmedDiagnosisList.remove(DefinedParams.dmtTwo)
                    return 1
                }
                else if (confirmedDiagnosisList.contains(DefinedParams.GestationalDiabetes))
                {
                    confirmedDiagnosisList.remove(DefinedParams.GestationalDiabetes)
                    return 1
                }
                else if(!confirmedDiagnosisList.contains(DefinedParams.PreDiabetic))
                    return 1
            }

            DefinedParams.typeOne -> {
                if (confirmedDiagnosisList.contains(DefinedParams.dmtTwo))
                {
                    confirmedDiagnosisList.remove(DefinedParams.dmtTwo)
                    return 1
                }
                else if (confirmedDiagnosisList.contains(DefinedParams.GestationalDiabetes))
                {
                    confirmedDiagnosisList.remove(DefinedParams.GestationalDiabetes)
                    return 1
                }
                else if (confirmedDiagnosisList.contains(DefinedParams.PreDiabetic))
                {
                    confirmedDiagnosisList.remove(DefinedParams.PreDiabetic)
                    return 1
                }
                else if(!confirmedDiagnosisList.contains(DefinedParams.dmtOne))
                    return 1
            }

            DefinedParams.typeTwo -> {
                if (confirmedDiagnosisList.contains(DefinedParams.dmtOne))
                {
                    confirmedDiagnosisList.remove(DefinedParams.dmtOne)
                    return 1
                }
                else if (confirmedDiagnosisList.contains(DefinedParams.GestationalDiabetes))
                {
                    confirmedDiagnosisList.remove(DefinedParams.GestationalDiabetes)
                    return 1
                }
                else if (confirmedDiagnosisList.contains(DefinedParams.PreDiabetic))
                {
                    confirmedDiagnosisList.remove(DefinedParams.PreDiabetic)
                    return 1
                }
                else if(!confirmedDiagnosisList.contains(DefinedParams.dmtTwo))
                    return 1
            }

            DefinedParams.GestationalDiabetes -> {
                if (confirmedDiagnosisList.contains(DefinedParams.dmtOne))
                {
                    confirmedDiagnosisList.remove(DefinedParams.dmtOne)
                    return 1
                }
                else if (confirmedDiagnosisList.contains(DefinedParams.dmtTwo))
                {
                    confirmedDiagnosisList.remove(DefinedParams.dmtTwo)
                    return 1
                }
                else if (confirmedDiagnosisList.contains(DefinedParams.PreDiabetic))
                {
                    confirmedDiagnosisList.remove(DefinedParams.PreDiabetic)
                    return 1
                }
                else if(!confirmedDiagnosisList.contains(DefinedParams.GestationalDiabetes))
                    return 1
            }
        }

        return -1
    }

    fun getTreatmentPlanData(context: Context) {
        try {
            if (connectivityManager.isNetworkAvailable()) {
                viewModelScope.launch(dispatcherIO) {
                    treatmentPlanData.postLoading()
                    val response = medicalReviewRepo.getTreatmentPlanData()
                    val map = LinkedHashMap<String, Any>()
                    response.forEach {
                        map[it.frequencyKey] = it.value
                    }
                    treatmentPlanData.postSuccess(data = map)
                }
            } else {
                treatmentPlanData.setError(context.getString(R.string.no_internet_error))
            }
        } catch (e: Exception) {
            treatmentPlanData.postError()
        }
    }

    fun getScreeningDetails(request: ScreeningDetail) {
        try {
            if (connectivityManager.isNetworkAvailable()) {
                viewModelScope.launch(dispatcherIO) {
                    screeningDetailResponse.postLoading()
                    val response = medicalReviewRepo.getScreeningDetails(request)
                    if (response.isSuccessful) {
                        val res = response.body()
                        if (res?.status == true)
                            screeningDetailResponse.postSuccess(res.entity)
                        else
                            screeningDetailResponse.postError()
                    } else
                        screeningDetailResponse.postError()
                }
            } else {
                screeningDetailResponse.postError()
            }
        } catch (e: Exception) {
            screeningDetailResponse.postError()
        }
    }

    fun getPatientTenantId(): Long {
        return (patientDetailsResponse.value?.data?.tenantId) ?: -1
    }

    fun createBpLog(request: HashMap<String, Any>) {
        viewModelScope.launch(dispatcherIO) {
            createBPvitalResponse.postLoading()
            try {
                val response = medicalReviewRepo.createBpLog(request)
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true)
                        createBPvitalResponse.postSuccess(res.entity)
                    else
                        createBPvitalResponse.postError()
                } else {
                    createBPvitalResponse.postError()
                }
            } catch (e: Exception) {
                createBPvitalResponse.postError()
            }
        }
    }

    fun createGlucoseLog(request: HashMap<String, Any>) {
        viewModelScope.launch(dispatcherIO) {
            createBloodGlucoseResponse.postLoading()
            try {
                val response = medicalReviewRepo.createGlucoseLog(request)
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true)
                        createBloodGlucoseResponse.postSuccess(res.entity)
                    else
                        createBloodGlucoseResponse.postError()
                } else {
                    createBloodGlucoseResponse.postError()
                }
            } catch (e: Exception) {
                createBloodGlucoseResponse.postError()
            }
        }
    }

    fun patientRemove(context: Context,request:PatientRemoveRequest) {
        viewModelScope.launch(dispatcherIO) {
            patientRemoveResponse.postLoading()
            try {
                if (connectivityManager.isNetworkAvailable()) {
                    val response = medicalReviewRepo.patientRemove(request)
                    if (response.isSuccessful) {
                        val res = response.body()
                        if (res?.status == true) {
                            val resultMap = HashMap<String, Any>()
                            res.message?.let { successMessage ->
                                resultMap[DefinedParams.message] = successMessage
                            }
                            patientRemoveResponse.postSuccess(resultMap)
                        } else
                            patientRemoveResponse.postError()
                    } else {
                        patientRemoveResponse.postError()
                    }
                } else {
                    patientRemoveResponse.postError(context.getString(R.string.no_internet_error))
                }
            }catch (e:Exception){
                patientRemoveResponse.postError()
            }
        }
    }

    fun getAssessmentPatientDetails(context: Context) {
        if (connectivityManager.isNetworkAvailable()) {
            viewModelScope.launch(dispatcherIO) {
                try {
                    patientId?.let {
                        assessmentPatientDetails.postLoading()
                        val apiRequest =
                            PatientDetailsModel(
                                it,
                                isAssessmentDataRequired = false,
                                isPrescriberRequired = false,
                                isLifestyleRequired = false
                            )
                        val response = medicalReviewRepo.getPatientDetails(apiRequest)
                        if (response.isSuccessful) {
                            val entity = response.body()?.entity
                            if (entity == null)
                                assessmentPatientDetails.postError()
                            else
                                assessmentPatientDetails.postSuccess(data = entity)
                        } else
                            assessmentPatientDetails.postError()
                    }
                } catch (e: Exception) {
                    assessmentPatientDetails.postError()
                }
            }
        } else {
            assessmentPatientDetails.setError(context.getString(R.string.no_internet_error))
        }
    }

    fun getDiagnosisList() {
        try {
            viewModelScope.launch(dispatcherIO) {
                val res = medicalReviewRepo.getDiagnosisList()
                if (res.isNotEmpty())
                    diagnosisList = ArrayList(res)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}