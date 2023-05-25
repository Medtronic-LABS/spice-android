package com.medtroniclabs.opensource.ui.boarding.viewmodel

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.opensource.R
import com.medtroniclabs.opensource.appextensions.postError
import com.medtroniclabs.opensource.appextensions.postLoading
import com.medtroniclabs.opensource.appextensions.postSuccess
import com.medtroniclabs.opensource.common.EncryptionUtil.getSecurePassword
import com.medtroniclabs.opensource.common.StringConverter.getErrorMessage
import com.medtroniclabs.opensource.custom.SecuredPreference
import com.medtroniclabs.opensource.data.model.APIResponse
import com.medtroniclabs.opensource.data.model.DeviceInfo
import com.medtroniclabs.opensource.data.model.LoginResponse
import com.medtroniclabs.opensource.di.IoDispatcher
import com.medtroniclabs.opensource.formgeneration.definedproperties.DefinedParams
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.boarding.repo.OnBoardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import okhttp3.MultipartBody
import retrofit2.Response
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val onBoardingRepo: OnBoardingRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
) :
    ViewModel() {


    @Inject
    lateinit var connectivityManager: ConnectivityManager

    var noInternetResponse = MutableLiveData<Boolean>()

    var loginResponse = MutableLiveData<Resource<LoginResponse>>()
    var appUpdateDiaglogue = MutableLiveData<String?>()

    fun doLogin(context: Context,username: String, password: String, deviceInfo: DeviceInfo) {
        viewModelScope.launch(dispatcherIO) {
            try {
                val securePassword = getSecurePassword(password)
                if (connectivityManager.isNetworkAvailable()) {
                    loginResponse.postLoading()
                    noInternetResponse.postValue(false)

                    val builder = MultipartBody.Builder()
                    builder.setType(MultipartBody.FORM)
                    builder.addFormDataPart(DefinedParams.Username, username)
                    builder.addFormDataPart(DefinedParams.Password, securePassword)
                    val response = onBoardingRepo.doLogin(builder.build())
                    if (response.isSuccessful) {
                        val loginResponseModel = response.body()
                        val headers = response.headers().toMultimap()
                        saveTokenInformation(headers)
                        loginResponseModel?.let { userResponse ->
                            SecuredPreference.putUserResponse(userResponse)
                            userResponse.cultureId?.let {
                                SecuredPreference.setUserPreference(it, DefinedParams.EN_Locale, false)
                            }
                        }
                        SecuredPreference.putString(
                            SecuredPreference.EnvironmentKey.USERNAME.name,
                            username
                        )
                        SecuredPreference.putString(
                            SecuredPreference.EnvironmentKey.PASSWORD.name,
                            securePassword
                        )
                        deviceInfo.tenantId = loginResponseModel?.tenantId
                        val versionCheck = onBoardingRepo.versionCheck()
                        versionCheck(versionCheck, deviceInfo, loginResponseModel)
                    } else {
                        loginResponse.postError(getErrorMessage(response.errorBody()))
                    }
                } else {
                    val isToShowAlert =
                        (username == SecuredPreference.getString(SecuredPreference.EnvironmentKey.USERNAME.name)
                                && getSecurePassword(password) == SecuredPreference.getString(
                            SecuredPreference.EnvironmentKey.PASSWORD.name
                        )) && SecuredPreference.getBoolean(SecuredPreference.EnvironmentKey.ISMETALOADED.name, false)

                    if (isToShowAlert)
                        noInternetResponse.postValue(isToShowAlert)
                    else
                        loginResponse.postError(context.getString(R.string.no_internet_error))
                }
            } catch (e: Exception) {
                loginResponse.postError()
            }
        }
    }

    private suspend fun versionCheck(
        versionCheck: Response<APIResponse<Boolean>>,
        deviceInfo: DeviceInfo,
        loginResponseModel: LoginResponse?
    ) {
        if (versionCheck.isSuccessful) {
            val response = versionCheck.body()
            response?.let {
                if (it.entity == true) {
                    saveDeviceDetail(deviceInfo, loginResponseModel)
                } else {
                    loginResponse.postError()
                    appUpdateDiaglogue.postValue(it.message)
                }
            } ?: kotlin.run {
                appUpdateDiaglogue.postValue(null)
                loginResponse.postError()
            }
        } else {
            appUpdateDiaglogue.postValue(null)
            loginResponse.postError()
        }
    }

    private suspend fun saveDeviceDetail(deviceInfo: DeviceInfo, loginResponseModel: LoginResponse?) {
        appUpdateDiaglogue.postValue(null)
        val deviceDetailsCall =
            onBoardingRepo.saveDeviceDetails(
                deviceInfo.tenantId ?: 0,
                deviceInfo
            )
        if (deviceDetailsCall.isSuccessful) {
            val responseBody = deviceDetailsCall.body()?.entity
            responseBody?.deviceInfoId?.let {
                SecuredPreference.saveDeviceDetails(it)
                loginResponse.postSuccess(loginResponseModel)
            } ?: kotlin.run {
                SecuredPreference.putBoolean(
                    SecuredPreference.EnvironmentKey.ISMETALOADED.name,
                    false
                )
                loginResponse.postError()
            }
        } else {
            SecuredPreference.putBoolean(
                SecuredPreference.EnvironmentKey.ISMETALOADED.name,
                false
            )
            loginResponse.postError()
        }
    }

    private fun saveTokenInformation(headers: Map<String, List<String>>) {
        if(headers.containsKey(DefinedParams.Authorization)
            && (headers[DefinedParams.Authorization]?.size
                ?: 0) > 0)
        {
            headers[DefinedParams.Authorization]?.get(0)?.let { token ->
                SecuredPreference.putString(
                    SecuredPreference.EnvironmentKey.TOKEN.name,
                    token
                )
            }
        }

    }

}