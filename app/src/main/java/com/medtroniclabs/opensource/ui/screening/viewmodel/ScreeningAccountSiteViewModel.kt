package com.medtroniclabs.opensource.ui.screening.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.opensource.appextensions.postLoading
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.db.tables.SiteEntity
import com.medtroniclabs.opensource.di.IoDispatcher
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.network.resource.ResourceState
import com.medtroniclabs.opensource.ui.screening.repo.ScreeningRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScreeningAccountSiteViewModel @Inject constructor(
    private val screeningRepository: ScreeningRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
    ) :
    ViewModel() {

    var accountSiteListLiveData = MutableLiveData<Resource<ArrayList<SiteEntity>>>()
    var SelectedSite:String ? = null

    init {
        fetchAccountSiteList()
    }

    private fun fetchAccountSiteList() {
        viewModelScope.launch(dispatcherIO) {
            accountSiteListLiveData.postLoading()
            try {
                val accountSiteList = screeningRepository.getAccountSiteList(SecuredPreference.getUserId())
                val siteList = Resource(ResourceState.SUCCESS, ArrayList(accountSiteList))
                accountSiteListLiveData.postValue(siteList)
            } catch (e: Exception) {
                accountSiteListLiveData.postValue(Resource(ResourceState.ERROR))
            }
        }
    }
}