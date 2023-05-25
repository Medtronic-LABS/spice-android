package com.medtroniclabs.opensource.common

import timber.log.Timber
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.InvalidKeyException
import java.security.NoSuchAlgorithmException
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

object EncryptionUtil {

    /**
     * Gets sha 256 secure password.
     *
     * @param passwordToHash the password to hash
     * @return sha 256 secure password
     */
    fun getSecurePassword(passwordToHash: String): String {
        try {
            val secretKeySpec = SecretKeySpec(
                AppConstants.SALT.toByteArray(StandardCharsets.UTF_8),
                AppConstants.SHA_MAC
            )
            val mac = Mac.getInstance(AppConstants.SHA_MAC)
            try {
                mac.init(secretKeySpec)
            } catch (e: InvalidKeyException) {
               Timber.d(e)
            }
            val bytes = mac.doFinal(passwordToHash.toByteArray(StandardCharsets.UTF_8))
            val hash = BigInteger(1, bytes)
            var result = hash.toString(16)
            if (result.length % 2 != 0) {
                result = "0$result"
            }
            return result
        } catch (e: NoSuchAlgorithmException) {
            return ""
        }
    }

}