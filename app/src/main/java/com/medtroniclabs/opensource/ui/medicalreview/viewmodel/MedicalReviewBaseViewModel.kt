package com.medtroniclabs.opensource.ui.medicalreview.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.postError
import com.medtroniclabs.opensource.appextensions.postLoading
import com.medtroniclabs.opensource.appextensions.postSuccess
import com.medtroniclabs.opensource.appextensions.setError
import com.medtroniclabs.opensource.common.StringConverter
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.*
import com.medtroniclabs.opensource.db.tables.*
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
class MedicalReviewBaseViewModel @Inject constructor(
    private val medicalReviewRepo: MedicalReviewRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
    ) :
    ViewModel() {

    @Inject
    lateinit var connectivityManager: ConnectivityManager
    var origin: String? = null
    val comorbidityListResponse = MutableLiveData<List<ComorbidityEntity>>()
    val complicationListResponse = MutableLiveData<List<ComplicationEntity>>()
    val lifeStyleListResponse = MutableLiveData<List<LifestyleEntity>>()
    var lifeStyleListUIModel: List<LifeStyleUIModel>? = null
    val currentMedicationResponse = MutableLiveData<List<CurrentMedicationEntity>>()
    val physicalExaminationResponse = MutableLiveData<List<PhysicalExaminationEntity>>()
    val chiefCompliants = MutableLiveData<List<ComplaintsEntity>>()
    val continuousMedicalReviewResponse = MutableLiveData<Resource<InitialEncounterResponse>>()
    val initialEncounterRequest = InitialEncounterRequest()
    val continuousMedicalRequest = CreateContinuousMedicalRequest()
    val diagnosisListResponse = MutableLiveData<List<DiagnosisEntity>>()
    var ShowContinuousMedicalReview: Boolean = false
    var medicalReViewRequest: MedicalReviewBaseRequest? = null
    val badgeCountResponse = MutableLiveData<Resource<BadgeResponseModel>>()
    val medicalReviewEditModel : MedicalReviewEditModel = MedicalReviewEditModel()
    var complaintsList: ArrayList<String> = ArrayList()
    var physicalExamsList: ArrayList<String> = ArrayList()
    var patientTrackId: Long = -1
    val searchSiteResponse = MutableLiveData<Resource<ArrayList<RegionSiteResponse>>>()
    val searchRoleUserResponse = MutableLiveData<Resource<ArrayList<SiteRoleResponse>>>()
    val patientTransferResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    val validateTransferResponse = MutableLiveData<Resource<HashMap<String, Any>>>()
    var isTransferDialogVisible = false
    var diagnosisList = MutableLiveData<List<DiagnosisEntity>>()

    fun getComorbidityList() {
        viewModelScope.launch(dispatcherIO) {
            comorbidityListResponse.postValue(medicalReviewRepo.getComorbidity())
        }
    }

    fun getComplicationList() {
        viewModelScope.launch(dispatcherIO) {
            complicationListResponse.postValue(medicalReviewRepo.getComplication())
        }
    }

    fun getLifeStyleList() {
        viewModelScope.launch(dispatcherIO) {
            lifeStyleListResponse.postValue(medicalReviewRepo.getLifeStyle())
        }
    }

    fun getConfirmDiagnosisList(gender: ArrayList<String>, type: ArrayList<String>) {
        viewModelScope.launch(dispatcherIO) {
            diagnosisListResponse.postValue(medicalReviewRepo.getDiagnosis(gender, type))
        }
    }

    fun getCurrentMedicationList() {
        viewModelScope.launch(dispatcherIO) {
            currentMedicationResponse.postValue(medicalReviewRepo.getCurrentMedicationList())
        }
    }

    fun getCurrentMedicationList(type: String) {
        viewModelScope.launch(dispatcherIO) {
            currentMedicationResponse.postValue(medicalReviewRepo.getCurrentMedicationList(type))
        }
    }

    fun getPhysicalExaminationEntity() {
        viewModelScope.launch(dispatcherIO) {
            physicalExaminationResponse.postValue(medicalReviewRepo.getPhysicalExaminationList())
        }
    }

    fun getComplaints() {
        viewModelScope.launch(dispatcherIO) {
            chiefCompliants.postValue(medicalReviewRepo.getChiefComplaints())
        }
    }

    fun createContinuousMedicalReview(context: Context,request: MedicalReviewEditModel) {
        if (connectivityManager.isNetworkAvailable()) {
            continuousMedicalReviewResponse.postLoading()
            viewModelScope.launch(dispatcherIO) {
                try {
                    val response = medicalReviewRepo.createContinuousMedicalReview(request)
                    if (response.isSuccessful) {
                        val res = response.body()
                        if (res?.status == true)
                            continuousMedicalReviewResponse.postSuccess(res.entity)
                        else
                            continuousMedicalReviewResponse.postError()
                    } else {
                        continuousMedicalReviewResponse.postError()
                    }
                } catch (e: Exception) {
                    continuousMedicalReviewResponse.postError()
                }
            }
        } else {
            continuousMedicalReviewResponse.postError(context.getString(R.string.no_internet_error))
        }
    }

    fun getBadgeCount() {
        viewModelScope.launch(dispatcherIO) {
            badgeCountResponse.postLoading()
            try {
                val response = medicalReviewRepo.getBadgeCount(
                    BadgeModel(
                        patientTrackId
                    )
                )
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true)
                        badgeCountResponse.postSuccess(res.entity)
                    else
                        badgeCountResponse.postError()
                } else
                    badgeCountResponse.postError()
            } catch (e: Exception) {
                badgeCountResponse.postError()
            }
        }
    }

    fun searchSite(context: Context, searchValue: String) {
        if (connectivityManager.isNetworkAvailable()) {
            viewModelScope.launch(dispatcherIO) {
                try {
                    searchSiteResponse.postLoading()
                    val request = RegionSiteModel(
                        searchTerm = searchValue,
                        countryId = SecuredPreference.getCountryID(),
                        tenantId = SecuredPreference.getSelectedSiteEntity()?.tenantId
                    )
                    val response = medicalReviewRepo.searchSite(request)
                    if (response.isSuccessful) {
                        val res = response.body()
                        if (res?.status == true)
                            searchSiteResponse.postSuccess(res.entityList)
                        else
                            searchSiteResponse.postError()
                    } else {
                        searchSiteResponse.postError()
                    }
                } catch (e: Exception) {
                    searchSiteResponse.postError()
                }
            }
        } else {
            searchSiteResponse.setError(context.getString(R.string.no_internet_error))
        }
    }

    fun searchRoleUser(context: Context,tenant: Long, searchValue: String) {
        if (connectivityManager.isNetworkAvailable()) {
            viewModelScope.launch(dispatcherIO) {
                try {
                    searchRoleUserResponse.postLoading()
                    val request = SiteRoleModel(tenantId = tenant, searchTerm = searchValue)
                    val response = medicalReviewRepo.searchRoleUser(request)
                    if (response.isSuccessful) {
                        val res = response.body()
                        if (res?.status == true)
                            searchRoleUserResponse.postSuccess(res.entityList)
                        else
                            searchRoleUserResponse.postError()
                    } else {
                        searchRoleUserResponse.postError()
                    }
                } catch (e: Exception) {
                    searchRoleUserResponse.postError()
                }
            }
        }else {
            searchRoleUserResponse.setError(context.getString(R.string.no_internet_error))
        }
    }

    fun createPatientTransfer(context: Context,request: TransferCreateRequest) {
        if(connectivityManager.isNetworkAvailable()) {
            viewModelScope.launch(dispatcherIO) {
                try {
                    patientTransferResponse.postLoading()
                    val response = medicalReviewRepo.createPatientTransfer(request)
                    if (response.isSuccessful) {
                        val res = response.body()
                        if (res?.status == true) {
                            isTransferDialogVisible = false
                            val resultMap = HashMap<String, Any>()
                            res.message?.let { successMessage ->
                                resultMap[DefinedParams.message] = successMessage
                            }
                            patientTransferResponse.postSuccess(resultMap)
                        } else
                            patientTransferResponse.postError()
                    } else {
                        patientTransferResponse.postError(StringConverter.getErrorMessage(response.errorBody()))
                    }
                } catch (e: Exception) {
                    patientTransferResponse.postError()
                }
            }
        }else{
            patientTransferResponse.setError(context.getString(R.string.no_internet_error))
        }
    }

    fun validatePatientTransfer(context: Context,request: FillPrescriptionRequest) {
        if (connectivityManager.isNetworkAvailable()) {
            viewModelScope.launch(dispatcherIO) {
                try {
                    validateTransferResponse.postLoading()
                    val response = medicalReviewRepo.validatePatientTransfer(request)
                    if (response.isSuccessful) {
                        val res = response.body()
                        if (res?.status == true)
                            validateTransferResponse.postSuccess(res.entity)
                        else
                            validateTransferResponse.postError()
                    } else {
                        validateTransferResponse.postError(StringConverter.getErrorMessage(response.errorBody()))
                    }
                } catch (e: Exception) {
                    validateTransferResponse.postError()
                }
            }
        } else {
            validateTransferResponse.setError(context.getString(R.string.no_internet_error))
        }
    }

    fun getDiagnosisList() {
        viewModelScope.launch(dispatcherIO) {
            diagnosisList.postValue(medicalReviewRepo.getDiagnosisList())
        }
    }
}