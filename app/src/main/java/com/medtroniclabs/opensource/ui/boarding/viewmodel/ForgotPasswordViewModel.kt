package com.medtroniclabs.opensource.ui.boarding.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.medtroniclabs.opensource.appextensions.postError
import com.medtroniclabs.opensource.appextensions.postLoading
import com.medtroniclabs.opensource.appextensions.postSuccess
import com.medtroniclabs.opensource.data.model.ForgotPasswordResponse
import com.medtroniclabs.opensource.di.IoDispatcher
import com.medtroniclabs.opensource.network.NetworkConstants
import com.medtroniclabs.opensource.network.resource.Resource
import com.medtroniclabs.opensource.network.utils.ConnectivityManager
import com.medtroniclabs.opensource.ui.boarding.repo.OnBoardingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val onBoardingRepo: OnBoardingRepository,
    @IoDispatcher private val dispatcherIO: CoroutineDispatcher
    ) :
    ViewModel() {

    var forgotPasswordResponse = MutableLiveData<Resource<ForgotPasswordResponse>>()

    @Inject
    lateinit var connectivityManager: ConnectivityManager


    fun resetPasswordFor(username: String) {
        viewModelScope.launch(dispatcherIO) {
            if (connectivityManager.isNetworkAvailable()) {
                try {
                    forgotPasswordResponse.postLoading()
                    val response = onBoardingRepo.resetPassword(username)
                    val res = response.body()
                    if (response.isSuccessful && res?.entity == true) {
                        forgotPasswordResponse.postSuccess(ForgotPasswordResponse(message = res.message ?: ""))
                    } else {
                        forgotPasswordResponse.postError(res?.message)
                    }
                } catch (e: Exception) {
                    forgotPasswordResponse.postError()
                }
            } else {
                forgotPasswordResponse.postError(NetworkConstants.NETWORK_ERROR)
            }
        }
    }
}