package com.medtroniclabs.opensource.ui.medicalreview

import android.graphics.Bitmap

interface SignatureListener {
    fun applySignature(signature: Bitmap)
}