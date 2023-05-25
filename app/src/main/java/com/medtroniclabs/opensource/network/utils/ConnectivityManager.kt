package com.medtroniclabs.opensource.network.utils

import android.app.Application
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectivityManager
@Inject
constructor(
    application: Application,
) {
    private val connectionLiveData = ConnectionLiveData(application)

    // observe this in ui
    private val isNetworkAvailable = MutableLiveData<Boolean>()

    fun registerConnectionObserver(lifecycleOwner: LifecycleOwner) {
        connectionLiveData.observe(lifecycleOwner, { isConnected ->
            isConnected?.let { isNetworkAvailable.value = it }
        })
        resetNetworkState()
    }

    fun unregisterConnectionObserver(lifecycleOwner: LifecycleOwner) {
        connectionLiveData.removeObservers(lifecycleOwner)
    }

    fun isNetworkAvailable(): Boolean {
        return connectionLiveData.value ?: false
    }

    fun resetNetworkState(){
        connectionLiveData.checkInitialNetwork()
    }

}