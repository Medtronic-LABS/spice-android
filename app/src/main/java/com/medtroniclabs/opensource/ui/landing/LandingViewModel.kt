package com.medtroniclabs.opensource.ui.landing

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.opensource.appextensions.postError
import com.medtroniclabs.opensource.appextensions.postLoading
import com.medtroniclabs.opensource.appextensions.postSuccess
import com.medtroniclabs.opensource.common.StringConverter
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.*
import com.medtroniclabs.opensource.db.tables.CulturesEntity
import com.medtroniclabs.opensource.db.tables.MenuEntity
import com.medtroniclabs.opensource.db.tables.SiteEntity
import com.medtroniclabs.opensource.di.IoDispatcher
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.ui.boarding.repo.OnBoardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import javax.inject.Inject

@HiltViewModel
class LandingViewModel @Inject constructor(
    private val onBoardingRepo: OnBoardingRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) :
    ViewModel() {

    var transferPatientViewId: Long? = null
    val siteListLiveData = MutableLiveData<Resource<List<SiteEntity>>>()
    val siteStoredLiveData = MutableLiveData<Boolean>()
    val menuList = MutableLiveData<Resource<List<MenuEntity>>>()
    val userLogout = MutableLiveData<Resource<LogoutResponse>>()
    val dataCount = MutableLiveData<Long>()
    val switchOrganizationResponse = MutableLiveData<Resource<DeviceInfo>>()
    var selectedSiteEntity: SiteEntity? = null
    val sessionResponse = MutableLiveData<Resource<ResponseBody>>()
    val offlineScreenedData = MutableLiveData<Long>()
    val patientTransferNotificationCountResponse =
        MutableLiveData<Resource<PatientTransferNotificationCountResponse>>()
    val patientListResponse = MutableLiveData<Resource<PatientTransferListResponse>>()
    val patientUpdateResponse = MutableLiveData<Resource<PatientTransferUpdateResponse>>()
    val culturesListLiveData = MutableLiveData<Resource<List<CulturesEntity>>>()
    val cultureUpdateResponse = MutableLiveData<Resource<HashMap<String, Any>>>()

    fun getSiteList(userSite: Boolean) {
        viewModelScope.launch(dispatcherIO) {
            try {
                siteListLiveData.postLoading()
                val siteList = onBoardingRepo.getSiteList(userSite, SecuredPreference.getUserId())
                siteListLiveData.postSuccess(siteList)
            } catch (e: Exception) {
                siteListLiveData.postError()
            }
        }
    }

    fun storeSelectedEntity(siteEntity: SiteEntity) {
        viewModelScope.launch(dispatcherIO) {
            try {
                onBoardingRepo.storeSelectedSiteEntity(siteEntity)
                siteStoredLiveData.postValue(true)
            } catch (e: Exception) {
                siteStoredLiveData.postValue(false)
            }
        }
    }

    fun getUserMenuListByRole(role: String) {
        viewModelScope.launch(dispatcherIO) {
            try {
                menuList.postLoading()
                val menuListData =
                    onBoardingRepo.getMenuListByRole(role, SecuredPreference.getUserId())
                menuList.postSuccess(menuListData)
            } catch (e: Exception) {
                menuList.postError()
            }
        }
    }

    fun userLogout(context: Context) {
        viewModelScope.launch(dispatcherIO) {
            try {
                userLogout.postLoading()
                val response = onBoardingRepo.userLogout()
                if (response.isSuccessful) {
                    SecuredPreference.clear(context)
                    userLogout.postSuccess()
                } else {
                    userLogout.postError(StringConverter.getErrorMessage(response.errorBody()))
                }
            } catch (e: Exception) {
                userLogout.postError()
            }
        }
    }

    fun checkOfflineDataCount() {
        viewModelScope.launch(dispatcherIO) {
            dataCount.postValue(onBoardingRepo.getUnSyncedDataCount())
        }
    }

    fun switchOrganization(request: DeviceInfo, newTenantId: Long) {
        viewModelScope.launch(dispatcherIO) {
            try {
                switchOrganizationResponse.postLoading()
                val response = onBoardingRepo.saveDeviceDetails(newTenantId, request)
                if (response.isSuccessful) {
                    SecuredPreference.changeTenantId(newTenantId)
                    switchOrganizationResponse.postSuccess(response.body()?.entity)
                } else {
                    switchOrganizationResponse.postError()
                }
            } catch (e: Exception) {
                switchOrganizationResponse.postError()
            }
        }
    }

    fun validateSession() {
        viewModelScope.launch(dispatcherIO) {
            try {
                sessionResponse.postLoading()
                val session = onBoardingRepo.validateSession()
                sessionResponse.postSuccess(session.body())
            } catch (e: Exception) {
                sessionResponse.postError()
            }
        }
    }

    fun fetchOfflineScreenedCount() {
        viewModelScope.launch(dispatcherIO) {
            offlineScreenedData.postValue(onBoardingRepo.getUnSyncedDataCount())
        }
    }

    fun patientTransferNotificationCount(request: PatientTransferNotificationCountRequest) {
        viewModelScope.launch(dispatcherIO) {
            try {
                patientTransferNotificationCountResponse.postLoading()
                val response = onBoardingRepo.patientTransferNotificationCount(request)
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true)
                        patientTransferNotificationCountResponse.postSuccess(res.entity)
                    else
                        patientTransferNotificationCountResponse.postError()
                } else
                    patientTransferNotificationCountResponse.postError()
            } catch (e: Exception) {
                patientTransferNotificationCountResponse.postError()
            }
        }
    }

    fun getPatientListTransfer(request: PatientTransferNotificationCountRequest) {
        viewModelScope.launch(dispatcherIO) {
            try {
                patientListResponse.postLoading()
                val response = onBoardingRepo.patientTransferListResponse(request)
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true)
                        patientListResponse.postSuccess(res.entity)
                    else
                        patientListResponse.postError()
                } else {
                    patientListResponse.postError()
                }
            } catch (e: Exception) {
                patientListResponse.postError()
            }
        }
    }

    fun patientTransferUpdate(request: PatientTransferUpdateRequest) {
        viewModelScope.launch(dispatcherIO) {
            try {
                patientUpdateResponse.postLoading()
                val response = onBoardingRepo.patientTransferUpdate(request)
                if (response.isSuccessful) {
                    val res = response.body()
                    if (res?.status == true) {
                        var resultMap: PatientTransferUpdateResponse? = null
                        res.message?.let { successMessage ->
                            resultMap = PatientTransferUpdateResponse(successMessage)
                        }
                        patientUpdateResponse.postSuccess(resultMap)
                    } else
                        patientUpdateResponse.postError()
                } else {
                    patientUpdateResponse.postError()
                }
            } catch (e: Exception) {
                patientUpdateResponse.postError()
            }
        }
    }

    fun getCulturesList() {
        viewModelScope.launch(dispatcherIO) {
            try {
                culturesListLiveData.postLoading()
                val culturesList = onBoardingRepo.getCulturesList()
                culturesListLiveData.postSuccess(culturesList)
            } catch (e: Exception) {
                culturesListLiveData.postError()
            }
        }
    }

    fun cultureLocaleUpdate(request: CultureLocaleModel) {
        viewModelScope.launch(dispatcherIO) {
            try {
                cultureUpdateResponse.postLoading()
                val response = onBoardingRepo.cultureLocaleUpdate(request)
                if (response.isSuccessful) {
                    val res  = response.body()
                    if(res?.status == true){
                        cultureUpdateResponse.postSuccess(res.entityList)
                    }else{
                        cultureUpdateResponse.postError()
                    }
                } else {
                    cultureUpdateResponse.postError()
                }
            } catch (e: Exception) {
                cultureUpdateResponse.postError()
            }
        }
    }
}