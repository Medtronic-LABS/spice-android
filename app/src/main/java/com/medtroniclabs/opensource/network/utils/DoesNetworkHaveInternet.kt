package com.medtroniclabs.opensource.network.utils

import com.medtroniclabs.opensource.BuildConfig
import java.io.IOException
import java.net.InetSocketAddress
import javax.net.SocketFactory

/**
 * Send a ping to googles primary DNS.
 * If successful, that means we have internet.
 */
object DoesNetworkHaveInternet {

    // Make sure to execute this on a background thread.
    fun execute(socketFactory: SocketFactory): Boolean {
        return try {
            val socket = socketFactory.createSocket() ?: throw IOException("Socket is null.")
            socket.connect(InetSocketAddress(BuildConfig.INTERNET_CHECK_URL, 53), 1500)
            socket.close()
            true
        } catch (e: IOException) {
            false
        }
    }
}