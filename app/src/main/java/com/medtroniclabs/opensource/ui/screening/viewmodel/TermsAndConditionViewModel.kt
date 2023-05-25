package com.medtroniclabs.opensource.ui.screening.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.opensource.appextensions.postError
import com.medtroniclabs.opensource.appextensions.postLoading
import com.medtroniclabs.opensource.appextensions.postSuccess
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.db.tables.ConsentEntity
import com.medtroniclabs.opensource.di.IoDispatcher
import com.medtroniclabs.opensource.formgeneration.formsupport.CommonUtils
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.ui.UIConstants
import com.medtroniclabs.opensource.ui.screening.repo.ScreeningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TermsAndConditionViewModel @Inject constructor(
    private val screeningRepository: ScreeningRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
    ) :
    ViewModel() {

    var enrollmentConsent = false
    var isFromScreening = false
    var isFromSummaryPage = false
    var isFromDirectEnrollment = false
    var consentDoneLiveDate = MutableLiveData<Resource<String>>()
    var patientInitial = MutableLiveData<String?>()

    fun fetchConsentRawHTML() {
        viewModelScope.launch(dispatcherIO) {
            try {
                consentDoneLiveDate.postLoading()
                val response:ConsentEntity = if (enrollmentConsent){
                    screeningRepository.getConsentHtmlRawString(UIConstants.enrollmentUniqueID,SecuredPreference.getUserId())
                }else{
                    screeningRepository.getConsentHtmlRawString(
                        UIConstants.screeningUniqueID,
                        SecuredPreference.getUserId()
                    )
                }
                consentDoneLiveDate.postSuccess(CommonUtils.formatConsent(response.consentHtmlRaw))
            } catch (e: Exception) {
                consentDoneLiveDate.postError(e.message)
            }
        }
    }

}