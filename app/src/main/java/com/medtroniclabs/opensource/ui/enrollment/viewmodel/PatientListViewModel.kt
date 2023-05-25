package com.medtroniclabs.opensource.ui.enrollment.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.postError
import com.medtroniclabs.opensource.appextensions.postLoading
import com.medtroniclabs.opensource.appextensions.postSuccess
import com.medtroniclabs.opensource.common.FilterEnum
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.*
import com.medtroniclabs.opensource.di.IoDispatcher
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.network.ApiHelper
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.LIST_LIMIT
import com.medtroniclabs.opensource.ui.PatientsDataSource
import com.medtroniclabs.opensource.ui.RoleConstant
import com.medtroniclabs.opensource.ui.UIConstants
import com.medtroniclabs.opensource.ui.medicalreview.GetPatientsCount
import com.medtroniclabs.opensource.ui.medicalreview.repo.MedicalReviewRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PatientListViewModel @Inject constructor(
    private val apiHelper: ApiHelper,
    private val medicalReviewRepo: MedicalReviewRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) : ViewModel(),
    GetPatientsCount {

    //Origin
    var origin = ""

    //Patient list - Grid count
    var spanCount: Int = DefinedParams.span_count_1

    //Total patient count
    var totalPatientCount = MutableLiveData<String>()

    //Create visit response
    val patientVisitResponse = MutableLiveData<Resource<PatientDetails>>()

    //Search keys
    var searchPatientId = ""
    var firstName: String? = null
    var lastName: String? = null
    var mobileNumber: String? = null

    //Patient List (or) Search
    var isPatientSearch = false

    //Sort and Filter
    var sort: SortModel? = null
    var filter: FilterModel? = null
    var applySortFilter = MutableLiveData<Boolean>()

    var isFilterReset = false

    @Inject
    lateinit var connectivityManager: ConnectivityManager

    val patientsDataSource =
        Pager(config = PagingConfig(pageSize = LIST_LIMIT), pagingSourceFactory = {
            PatientsDataSource(
                isPatientSearch = isPatientSearch,
                isSiteBasedSearch = isSiteBasedSearch(),
                searchModel = PatientsDataModel(
                    searchId = searchPatientId,
                    firstName = firstName,
                    lastName = lastName,
                    phoneNumber = mobileNumber,
                    operatingUnitId = if (origin == UIConstants.AssessmentUniqueID) SecuredPreference.getString(
                        SecuredPreference.EnvironmentKey.OPERATING_UNIT.name
                    )
                        ?.toLong() else null,
                    isLabtestReferred = isLabTestReferred(),
                    isMedicationPrescribed = isMedicationPrescribed(),
                    patientSort = getSortBy(),
                    patientFilter = getFilterBy()
                ),
                apiHelper = apiHelper,
                getPatientsCount = this
            )
        }).flow

    override fun patientsCount(count: String) {
        totalPatientCount.postValue(count)
    }

    fun createPatientVisit(
        context: Context,
        request: MedicalReviewBaseRequest,
        initialReview: Boolean,
        patientID: Long
    ) {
        if (connectivityManager.isNetworkAvailable()) {
            patientVisitResponse.postLoading()
            viewModelScope.launch(dispatcherIO) {
                try {
                    val response = medicalReviewRepo.createPatientVisit(request)
                    if (response.isSuccessful) {
                        val res = response.body()
                        if (res?.status == true && res.entity != null)
                            patientVisitResponse.postSuccess(
                                PatientDetails(
                                    res.entity.id,
                                    initialReview,
                                    patientID
                                )
                            )
                        else
                            patientVisitResponse.postError()
                    } else {
                        patientVisitResponse.postError()
                    }
                } catch (e: Exception) {
                    patientVisitResponse.postError()
                }
            }
        } else {
            patientVisitResponse.postError(context.getString(R.string.no_internet_error))
        }
    }

    private fun getSortBy(): SortModel {
        return sort ?: SortModel().apply {
            when (origin) {
                UIConstants.myPatientsUniqueID -> this.isRedRisk = true
                UIConstants.investigation, UIConstants.lifestyle, UIConstants.PrescriptionUniqueID -> this.isUpdated =
                    true
                else -> this.isCVDRisk = true
            }
        }
    }

    private fun getFilterBy(): FilterModel? {
        if (origin == UIConstants.enrollmentUniqueID) {
            return FilterModel(patientStatus = FilterEnum.NOT_ENROLLED.title)
        }

        if (origin == UIConstants.myPatientsUniqueID && SecuredPreference.getSelectedSiteEntity()?.role == RoleConstant.HRIO) {
            return FilterModel(patientStatus = FilterEnum.ENROLLED.title)
        }

        return if (filter == null) FilterModel(screeningReferral = isPatientListRequired()) else filter?.apply {
            screeningReferral = isPatientListRequired()
        }
    }

    fun isPatientListRequired(): Boolean {
        return when (origin) {
            UIConstants.myPatientsUniqueID,
            UIConstants.PrescriptionUniqueID,
            UIConstants.investigation,
            UIConstants.lifestyle -> true
            else -> false
        }
    }

    private fun isLabTestReferred(): Boolean? {
        return when (origin) {
            UIConstants.investigation -> true
            else -> null
        }
    }

    private fun isMedicationPrescribed(): Boolean? {
        return when (origin) {
            UIConstants.PrescriptionUniqueID -> true
            else -> null
        }
    }

    private fun isSiteBasedSearch(): Boolean {
        return when (origin) {
            UIConstants.enrollmentUniqueID,
            UIConstants.AssessmentUniqueID -> false
            else -> true
        }
    }

    fun filterCount(): Int {
        var count = 0
        filter?.let { filter ->
            if (filter.medicalReviewDate != null)
                count++
            if (filter.isRedRiskPatient == true)
                count++
            if (filter.patientStatus != null)
                count++
            if (filter.cvdRiskLevel != null)
                count++
            if (filter.assessmentDate != null)
                count++
            if (filter.labTestReferredDate != null)
                count++
            if (filter.medicationPrescribedDate != null)
                count++
        }
        return count
    }

    fun sortCount(): Int {
        var isSortApplied = false
        sort?.let { sort ->
            isSortApplied = sort.isRedRisk != null ||
                    sort.isLatestAssessment != null ||
                    sort.isMedicalReviewDueDate != null ||
                    sort.isHighLowBp != null ||
                    sort.isHighLowBg != null ||
                    sort.isAssessmentDueDate != null
        }
        return if (isSortApplied) 1 else 0
    }
}