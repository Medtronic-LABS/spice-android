package com.medtroniclabs.opensource.network.utils

import android.content.Context
import android.content.Context.CONNECTIVITY_SERVICE
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET
import android.net.NetworkRequest
import androidx.lifecycle.LiveData
import kotlinx.coroutines.*

class ConnectionLiveData(
    context: Context,
    private val dispatchersIO: CoroutineDispatcher = Dispatchers.IO,
    private val dispatcherMain: CoroutineDispatcher = Dispatchers.Main
    ) : LiveData<Boolean>() {

    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private val cm = context.getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
    private val validNetworks: MutableSet<Network> = HashSet()

    init {
        checkInitialNetwork()
    }

    fun checkInitialNetwork() {
        cm.activeNetwork?.let {
            checkNetworkCapabilities(it)
        } ?: kotlin.run {
            validNetworks.clear()
            checkValidNetworks()
        }
    }

    private fun checkValidNetworks() {
        postValue(validNetworks.size > 0)
    }

    override fun onActive() {
        networkCallback = createNetworkCallback()
        val networkRequest = NetworkRequest.Builder()
            .addCapability(NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(networkRequest, networkCallback)
    }

    private fun createNetworkCallback() = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            checkNetworkCapabilities(network)
        }


        override fun onLost(network: Network) {
            super.onLost(network)
            validNetworks.remove(network)
            checkValidNetworks()
        }
    }

    private fun checkNetworkCapabilities(network: Network) {
        val networkCapabilities = cm.getNetworkCapabilities(network)
        val hasInternetCapability = networkCapabilities?.hasCapability(NET_CAPABILITY_INTERNET)
        if (hasInternetCapability == true) {
            CoroutineScope(dispatchersIO).launch {
                val hasInternet = DoesNetworkHaveInternet.execute(network.socketFactory)
                if (hasInternet) {
                    withContext(dispatcherMain) {
                        validNetworks.add(network)
                        checkValidNetworks()
                    }
                }
            }
        }
    }


    override fun onInactive() {
        cm.unregisterNetworkCallback(networkCallback)
    }


}