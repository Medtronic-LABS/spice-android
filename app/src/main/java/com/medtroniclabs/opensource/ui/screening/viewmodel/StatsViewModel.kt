package com.medtroniclabs.opensource.ui.screening.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.opensource.common.ViewUtil
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.di.IoDispatcher
import com.medtroniclabs.opensource.ui.screening.repo.ScreeningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val screeningRepository: ScreeningRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
    ) :
    ViewModel() {

    val screenedPatientCount = MutableLiveData<Long>()
    val referredPatientCount = MutableLiveData<Long>()
    var isStatsFromSummary: Boolean = false

    init {
        getScreenedPatientCount()
    }

    private fun getScreenedPatientCount() {
        viewModelScope.launch(dispatcherIO) {
            val count = screeningRepository.getScreenedPatientCount(
                ViewUtil.getStartDate(),
                ViewUtil.getEndDate(),
                SecuredPreference.getUserId()
            )
            screenedPatientCount.postValue(count)
            getScreenedPatientReferredCount(true)
        }
    }

    private suspend fun getScreenedPatientReferredCount(isReferred: Boolean) {
        val count = screeningRepository.getScreenedPatientReferredCount(
            ViewUtil.getStartDate(),
            ViewUtil.getEndDate(),
            SecuredPreference.getUserId(),
            isReferred
        )
        referredPatientCount.postValue(count)
    }

}